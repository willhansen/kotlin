/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.lazy.descriptors

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny
import org.jetbrains.kotlin.resolve.scopes.*
import org.jetbrains.kotlin.resolve.scopes.utils.ErrorLexicalScope
import org.jetbrains.kotlin.storage.StorageManager

class ClassResolutionScopesSupport(
    private konst classDescriptor: ClassDescriptor,
    storageManager: StorageManager,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst getOuterScope: () -> LexicalScope
) {
    private fun scopeWithGenerics(parent: LexicalScope): LexicalScopeImpl {
        return LexicalScopeImpl(parent, classDescriptor, false, null, emptyList(), LexicalScopeKind.CLASS_HEADER) {
            classDescriptor.declaredTypeParameters.forEach { addClassifierDescriptor(it) }
        }
    }

    konst scopeForClassHeaderResolution: () -> LexicalScope = storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
        scopeWithGenerics(getOuterScope())
    }

    konst scopeForConstructorHeaderResolution: () -> LexicalScope = storageManager.createLazyValue {
        scopeWithGenerics(inheritanceScopeWithMe())
    }

    private konst inheritanceScopeWithoutMe: () -> LexicalScope = storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
        classDescriptor.getAllSuperclassesWithoutAny().asReversed().fold(getOuterScope()) { scope, currentClass ->
            createInheritanceScope(parent = scope, ownerDescriptor = classDescriptor, classDescriptor = currentClass)
        }
    }

    private konst inheritanceScopeWithMe: () -> LexicalScope = storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
        createInheritanceScope(parent = inheritanceScopeWithoutMe(), ownerDescriptor = classDescriptor, classDescriptor = classDescriptor)
    }

    konst scopeForCompanionObjectHeaderResolution: () -> LexicalScope =
        storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
            createInheritanceScope(inheritanceScopeWithoutMe(), classDescriptor, classDescriptor, withCompanionObject = false)
        }

    konst scopeForMemberDeclarationResolution: () -> LexicalScope = storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
        konst scopeWithGenerics = scopeWithGenerics(inheritanceScopeWithMe())
        LexicalScopeImpl(
            scopeWithGenerics,
            classDescriptor,
            true,
            classDescriptor.thisAsReceiverParameter,
            classDescriptor.contextReceivers,
            LexicalScopeKind.CLASS_MEMBER_SCOPE
        )
    }

    konst scopeForStaticMemberDeclarationResolution: () -> LexicalScope =
        storageManager.createLazyValue(onRecursion = createErrorLexicalScope) {
            if (classDescriptor.kind.isSingleton) {
                scopeForMemberDeclarationResolution()
            } else {
                inheritanceScopeWithMe()
            }
        }

    private fun createInheritanceScope(
        parent: LexicalScope,
        ownerDescriptor: DeclarationDescriptor,
        classDescriptor: ClassDescriptor,
        withCompanionObject: Boolean = true,
        isDeprecated: Boolean = false
    ): LexicalScope {
        konst companionObjectDescriptor = classDescriptor.companionObjectDescriptor?.takeIf { withCompanionObject }
        konst parentForNewScope = companionObjectDescriptor?.packScopesOfCompanionSupertypes(parent, ownerDescriptor) ?: parent

        konst lexicalChainedScope = LexicalChainedScope.create(
            parentForNewScope, ownerDescriptor,
            isOwnerDescriptorAccessibleByLabel = false,
            implicitReceiver = companionObjectDescriptor?.thisAsReceiverParameter,
            contextReceiversGroup = emptyList(),
            kind = LexicalScopeKind.CLASS_INHERITANCE,
            classDescriptor.staticScope,
            classDescriptor.unsubstitutedInnerClassesScope,
            companionObjectDescriptor?.getStaticScopeOfCompanionObject(classDescriptor),
            isStaticScope = true
        )

        return if (isDeprecated) DeprecatedLexicalScope(lexicalChainedScope) else lexicalChainedScope
    }

    private fun ClassDescriptor.getStaticScopeOfCompanionObject(companionOwner: ClassDescriptor): MemberScope? {
        return when {
        // We always see nesteds from our own companion
            companionOwner == classDescriptor -> unsubstitutedInnerClassesScope

        // We see nesteds from other companions in hierarchy only in legacy mode
            languageVersionSettings.supportsFeature(LanguageFeature.ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion) -> null

            else -> DeprecatedMemberScope(unsubstitutedInnerClassesScope)
        }
    }

    private fun ClassDescriptor.packScopesOfCompanionSupertypes(
        parent: LexicalScope,
        ownerDescriptor: DeclarationDescriptor
    ): LexicalScope? {
        if (languageVersionSettings.supportsFeature(LanguageFeature.ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion)) return null
        return getAllSuperclassesWithoutAny().asReversed().fold(parent) { scope, currentClass ->
            createInheritanceScope(scope, ownerDescriptor, currentClass, withCompanionObject = false, isDeprecated = true)
        }
    }

    private fun <T : Any> StorageManager.createLazyValue(onRecursion: ((Boolean) -> T), compute: () -> T) =
        createLazyValue(compute, onRecursion)

    companion object {
        private konst createErrorLexicalScope: (Boolean) -> LexicalScope = { ErrorLexicalScope() }
    }
}

fun scopeForInitializerResolution(
    classDescriptor: LazyClassDescriptor,
    parentDescriptor: DeclarationDescriptor,
    primaryConstructorParameters: List<KtParameter>
): LexicalScope {
    return LexicalScopeImpl(
        classDescriptor.scopeForMemberDeclarationResolution,
        parentDescriptor,
        false,
        null,
        emptyList(),
        LexicalScopeKind.CLASS_INITIALIZER
    ) {
        if (primaryConstructorParameters.isNotEmpty()) {
            konst parameterDescriptors = classDescriptor.unsubstitutedPrimaryConstructor!!.konstueParameters
            assert(parameterDescriptors.size == primaryConstructorParameters.size)
            for ((parameter, descriptor) in primaryConstructorParameters.zip(parameterDescriptors)) {
                if (!parameter.hasValOrVar()) {
                    addVariableDescriptor(descriptor)
                }
            }
        }
    }
}
