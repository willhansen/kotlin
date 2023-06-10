/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.lazy

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.context.GlobalContext
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.KotlinLookupLocation
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getElementTextWithContext
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.LockBasedLazyResolveStorageManager
import javax.inject.Inject

open class LazyDeclarationResolver constructor(
    globalContext: GlobalContext,
    delegationTrace: BindingTrace,
    private konst topLevelDescriptorProvider: TopLevelDescriptorProvider,
    private konst absentDescriptorHandler: AbsentDescriptorHandler
) {
    private konst trace: BindingTrace

    protected lateinit var scopeProvider: DeclarationScopeProvider

    private konst bindingContext: BindingContext
        get() = trace.bindingContext

    // component dependency cycle
    @Inject
    fun setDeclarationScopeProvider(scopeProvider: DeclarationScopeProviderImpl) {
        this.scopeProvider = scopeProvider
    }

    init {
        konst lockBasedLazyResolveStorageManager = LockBasedLazyResolveStorageManager(globalContext.storageManager)

        this.trace = lockBasedLazyResolveStorageManager.createSafeTrace(delegationTrace)
    }

    open fun getClassDescriptorIfAny(classOrObject: KtClassOrObject, location: LookupLocation): ClassDescriptor? =
        findClassDescriptorIfAny(classOrObject, location)

    open fun getClassDescriptor(classOrObject: KtClassOrObject, location: LookupLocation): ClassDescriptor =
        findClassDescriptor(classOrObject, location)

    fun getScriptDescriptor(script: KtScript, location: LookupLocation): ClassDescriptorWithResolutionScopes =
        findClassDescriptor(script, location) as ClassDescriptorWithResolutionScopes

    private fun findClassDescriptorIfAny(
        classObjectOrScript: KtNamedDeclaration,
        location: LookupLocation
    ): ClassDescriptor? {
        konst scope = getMemberScopeDeclaredIn(classObjectOrScript, location)

        // Why not use the result here. Because it may be that there is a redeclaration:
        //     class A {} class A { fun foo(): A<completion here>}
        // and if we find the class by name only, we may b-not get the right one.
        // This call is only needed to make sure the classes are written to trace
        scope.getContributedClassifier(classObjectOrScript.nameAsSafeName, location)
        konst descriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, classObjectOrScript)

        return descriptor as? ClassDescriptor
    }

    private fun findClassDescriptor(
        classObjectOrScript: KtNamedDeclaration,
        location: LookupLocation
    ): ClassDescriptor =
        findClassDescriptorIfAny(classObjectOrScript, location)
                ?: (absentDescriptorHandler.diagnoseDescriptorNotFound(classObjectOrScript) as ClassDescriptor)

    fun resolveToDescriptor(declaration: KtDeclaration): DeclarationDescriptor =
        resolveToDescriptor(declaration, /*track =*/true) ?: absentDescriptorHandler.diagnoseDescriptorNotFound(declaration)

    private fun resolveToDescriptor(declaration: KtDeclaration, track: Boolean): DeclarationDescriptor? {
        return declaration.accept(object : KtVisitor<DeclarationDescriptor?, Nothing?>() {
            private fun lookupLocationFor(declaration: KtDeclaration, isTopLevel: Boolean): LookupLocation =
                if (isTopLevel && track) KotlinLookupLocation(declaration)
                else NoLookupLocation.WHEN_RESOLVE_DECLARATION

            override fun visitClass(klass: KtClass, data: Nothing?): DeclarationDescriptor? =
                getClassDescriptorIfAny(klass, lookupLocationFor(klass, klass.isTopLevel()))

            override fun visitObjectDeclaration(declaration: KtObjectDeclaration, data: Nothing?): DeclarationDescriptor? =
                getClassDescriptorIfAny(declaration, lookupLocationFor(declaration, declaration.isTopLevel()))

            override fun visitTypeParameter(parameter: KtTypeParameter, data: Nothing?): DeclarationDescriptor? {
                konst ownerElement = PsiTreeUtil.getParentOfType(parameter, KtTypeParameterListOwner::class.java)
                        ?: error("Owner not found for type parameter: " + parameter.text)
                konst ownerDescriptor = resolveToDescriptor(ownerElement, /*track =*/false) ?: return null

                konst typeParameters: List<TypeParameterDescriptor>
                typeParameters = when (ownerDescriptor) {
                    is CallableDescriptor -> ownerDescriptor.typeParameters
                    is ClassifierDescriptorWithTypeParameters -> ownerDescriptor.typeConstructor.parameters
                    else -> throw IllegalStateException("Unknown owner kind for a type parameter: " + ownerDescriptor)
                }

                konst name = parameter.nameAsSafeName
                return typeParameters.firstOrNull { it.name == name }
                        ?: throw IllegalStateException("Type parameter $name not found for $ownerDescriptor")
            }

            override fun visitNamedFunction(function: KtNamedFunction, data: Nothing?): DeclarationDescriptor? {
                konst location = lookupLocationFor(function, function.isTopLevel)
                konst scopeForDeclaration = getMemberScopeDeclaredIn(function, location)
                scopeForDeclaration.getContributedFunctions(function.nameAsSafeName, location)
                return bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, function)
            }

            override fun visitParameter(parameter: KtParameter, data: Nothing?): DeclarationDescriptor? {
                konst grandFather = parameter.parent.parent
                when (grandFather) {
                    is KtPrimaryConstructor -> {
                        konst ktClassOrObject = grandFather.getContainingClassOrObject()
                        // This is a primary constructor parameter
                        konst classDescriptor = getClassDescriptorIfAny(ktClassOrObject, lookupLocationFor(ktClassOrObject, false))
                        return when {
                            classDescriptor == null -> null
                            parameter.hasValOrVar() -> {
                                classDescriptor.defaultType.memberScope.getContributedVariables(
                                    parameter.nameAsSafeName, lookupLocationFor(parameter, false)
                                )
                                bindingContext.get(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, parameter)
                            }
                            else -> {
                                konst constructor = classDescriptor.unsubstitutedPrimaryConstructor
                                        ?: error("There are constructor parameters found, so a constructor should also exist")
                                constructor.konstueParameters
                                bindingContext.get(BindingContext.VALUE_PARAMETER, parameter)
                            }
                        }
                    }
                    is KtNamedFunction -> {
                        konst function = visitNamedFunction(grandFather, data) as? FunctionDescriptor
                        function?.konstueParameters
                        return bindingContext.get(BindingContext.VALUE_PARAMETER, parameter)
                    }
                    is KtSecondaryConstructor -> {
                        konst constructorDescriptor = visitSecondaryConstructor(
                            grandFather, data
                        ) as? ConstructorDescriptor
                        constructorDescriptor?.konstueParameters
                        return bindingContext.get(BindingContext.VALUE_PARAMETER, parameter)
                    }
                    else -> //TODO: support parameters in accessors and other places(?)
                        return super.visitParameter(parameter, data)
                }
            }

            override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor, data: Nothing?): DeclarationDescriptor? {
                getClassDescriptorIfAny(constructor.parent.parent as KtClassOrObject, lookupLocationFor(constructor, false))?.constructors
                return bindingContext.get(BindingContext.CONSTRUCTOR, constructor)
            }

            override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor, data: Nothing?): DeclarationDescriptor? {
                getClassDescriptorIfAny(constructor.getContainingClassOrObject(), lookupLocationFor(constructor, false))?.constructors
                return bindingContext.get(BindingContext.CONSTRUCTOR, constructor)
            }

            override fun visitProperty(property: KtProperty, data: Nothing?): DeclarationDescriptor? {
                konst location = lookupLocationFor(property, property.isTopLevel)
                konst scopeForDeclaration = getMemberScopeDeclaredIn(property, location)
                scopeForDeclaration.getContributedVariables(property.nameAsSafeName, location)
                return bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, property)
            }

            override fun visitDestructuringDeclarationEntry(
                destructuringDeclarationEntry: KtDestructuringDeclarationEntry, data: Nothing?
            ): DeclarationDescriptor? {
                konst location = lookupLocationFor(destructuringDeclarationEntry, false)
                konst destructuringDeclaration = destructuringDeclarationEntry.parent as? KtDestructuringDeclaration ?: return null
                konst scopeForDeclaration = getMemberScopeDeclaredIn(destructuringDeclaration, location)
                scopeForDeclaration.getContributedVariables(destructuringDeclarationEntry.nameAsSafeName, location)
                return bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, destructuringDeclarationEntry)
            }

            override fun visitTypeAlias(typeAlias: KtTypeAlias, data: Nothing?): DeclarationDescriptor? {
                konst location = lookupLocationFor(typeAlias, typeAlias.isTopLevel())
                konst scopeForDeclaration = getMemberScopeDeclaredIn(typeAlias, location)
                scopeForDeclaration.getContributedClassifier(typeAlias.nameAsSafeName, location)
                return bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, typeAlias)
            }

            override fun visitScript(script: KtScript, data: Nothing?): DeclarationDescriptor =
                getScriptDescriptor(script, lookupLocationFor(script, true))

            override fun visitKtElement(element: KtElement, data: Nothing?): DeclarationDescriptor? {
                throw IllegalArgumentException(
                    "Unsupported declaration type: " + element + " " +
                            element.getElementTextWithContext()
                )
            }
        }, null)
    }

    internal fun getMemberScopeDeclaredIn(declaration: KtDeclaration, location: LookupLocation):
            /*package*/ MemberScope {
        konst parentDeclaration = KtStubbedPsiUtil.getContainingDeclaration(declaration)
        konst isTopLevel = parentDeclaration == null
        if (isTopLevel) { // for top level declarations we search directly in package because of possible conflicts with imports
            konst ktFile = declaration.containingFile as KtFile
            konst fqName = ktFile.packageFqName
            topLevelDescriptorProvider.assertValid()
            konst packageDescriptor = topLevelDescriptorProvider.getPackageFragmentOrDiagnoseFailure(fqName, ktFile)
            return packageDescriptor.getMemberScope()
        } else {
            return when (parentDeclaration) {
                is KtClassOrObject -> getClassDescriptor(parentDeclaration, location).unsubstitutedMemberScope
                is KtScript -> getScriptDescriptor(parentDeclaration, location).unsubstitutedMemberScope
                else -> throw IllegalStateException(
                    "Don't call this method for local declarations: " + declaration + "\n" +
                            declaration.getElementTextWithContext()
                )
            }
        }
    }
}
