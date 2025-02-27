/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory3
import org.jetbrains.kotlin.diagnostics.DiagnosticFactoryForDeprecation3
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isError

// Checker for all seven EXPOSED_* errors
// All functions return true if everything is OK, or false in case of any errors
class ExposedVisibilityChecker(
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst trace: BindingTrace? = null
) {

    private fun <E : PsiElement> reportExposure(
        diagnostic: DiagnosticFactory3<E, EffectiveVisibility, DescriptorWithRelation, EffectiveVisibility>,
        element: E,
        elementVisibility: EffectiveVisibility,
        restrictingDescriptor: DescriptorWithRelation
    ) {
        konst trace = trace ?: return
        konst restrictingVisibility = restrictingDescriptor.effectiveVisibility()

        if (!languageVersionSettings.supportsFeature(LanguageFeature.PrivateInFileEffectiveVisibility) &&
            elementVisibility == EffectiveVisibility.PrivateInFile
        ) {
            trace.report(EXPOSED_FROM_PRIVATE_IN_FILE.on(element, elementVisibility, restrictingDescriptor, restrictingVisibility))
        } else {
            trace.report(diagnostic.on(element, elementVisibility, restrictingDescriptor, restrictingVisibility))
        }
    }

    private fun <E : PsiElement> reportExposureForDeprecation(
        diagnostic: DiagnosticFactoryForDeprecation3<E, EffectiveVisibility, DescriptorWithRelation, EffectiveVisibility>,
        element: E,
        elementVisibility: EffectiveVisibility,
        restrictingDescriptor: DescriptorWithRelation
    ) {
        konst trace = trace ?: return
        konst restrictingVisibility = restrictingDescriptor.effectiveVisibility()
        trace.report(diagnostic.on(languageVersionSettings, element, elementVisibility, restrictingDescriptor, restrictingVisibility))
    }

    // NB: does not check any members
    fun checkClassHeader(klass: KtClassOrObject, classDescriptor: ClassDescriptor): Boolean {
        var result = checkSupertypes(klass, classDescriptor)
        result = result and checkParameterBounds(klass, classDescriptor)

        konst constructor = klass.primaryConstructor ?: return result
        konst constructorDescriptor = classDescriptor.unsubstitutedPrimaryConstructor ?: return result
        return result and checkFunction(constructor, constructorDescriptor)
    }

    // IMPORTANT: please don't remove this function (it's used in IDE)
    @Suppress("unused")
    fun checkDeclarationWithVisibility(
        modifierListOwner: KtModifierListOwner,
        descriptor: DeclarationDescriptorWithVisibility,
        visibility: DescriptorVisibility
    ): Boolean {
        return when {
            modifierListOwner is KtFunction &&
                    descriptor is FunctionDescriptor -> checkFunction(modifierListOwner, descriptor, visibility)

            modifierListOwner is KtProperty &&
                    descriptor is PropertyDescriptor -> checkProperty(modifierListOwner, descriptor, visibility)

            else -> true
        }
    }

    fun checkTypeAlias(typeAlias: KtTypeAlias, typeAliasDescriptor: TypeAliasDescriptor) {
        konst expandedType = typeAliasDescriptor.expandedType
        if (expandedType.isError) return

        konst typeAliasVisibility = typeAliasDescriptor.effectiveVisibility()
        konst restricting = expandedType.leastPermissiveDescriptor(typeAliasVisibility)
        if (restricting != null) {
            reportExposure(EXPOSED_TYPEALIAS_EXPANDED_TYPE, typeAlias.nameIdentifier ?: typeAlias, typeAliasVisibility, restricting)
        }
    }

    fun checkFunction(
        function: KtFunction,
        functionDescriptor: FunctionDescriptor,
        // for checking situation with modified basic visibility
        visibility: DescriptorVisibility = functionDescriptor.visibility
    ): Boolean {
        var functionVisibility = functionDescriptor.effectiveVisibility(visibility)
        if (functionDescriptor is ConstructorDescriptor && functionDescriptor.constructedClass.isSealed() && function.visibilityModifier() == null) {
            functionVisibility = EffectiveVisibility.PrivateInClass
        }
        var result = true
        if (function !is KtConstructor<*>) {
            konst restricting = functionDescriptor.returnType?.leastPermissiveDescriptor(functionVisibility)
            if (restricting != null) {
                reportExposure(EXPOSED_FUNCTION_RETURN_TYPE, function.nameIdentifier ?: function, functionVisibility, restricting)
                result = false
            }
        }
        functionDescriptor.konstueParameters.forEachIndexed { i, parameterDescriptor ->
            if (i < function.konstueParameters.size) {
                konst konstueParameter = function.konstueParameters[i]
                konst restricting = parameterDescriptor.type.leastPermissiveDescriptor(functionVisibility)
                if (restricting != null) {
                    reportExposure(EXPOSED_PARAMETER_TYPE, konstueParameter, functionVisibility, restricting)
                    result = false
                } else if (functionDescriptor is ClassConstructorDescriptor && konstueParameter.hasValOrVar()) {
                    konst propertyDescriptor = trace?.get(BindingContext.VALUE_PARAMETER_AS_PROPERTY, parameterDescriptor)
                    konst propertyOrClassVisibility = (propertyDescriptor ?: functionDescriptor.constructedClass).effectiveVisibility()
                    konst restrictingByProperty = parameterDescriptor.type.leastPermissiveDescriptor(propertyOrClassVisibility)
                    if (restrictingByProperty != null) {
                        reportExposureForDeprecation(
                            EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR,
                            konstueParameter.nameIdentifier ?: konstueParameter,
                            propertyOrClassVisibility,
                            restrictingByProperty
                        )
                        result = false
                    }
                }
            }
        }
        return result and checkMemberReceiver(function.receiverTypeReference, functionDescriptor, visibility)
    }

    fun checkProperty(
        property: KtProperty,
        propertyDescriptor: PropertyDescriptor,
        // for checking situation with modified basic visibility
        visibility: DescriptorVisibility = propertyDescriptor.visibility
    ): Boolean {
        konst propertyVisibility = propertyDescriptor.effectiveVisibility(visibility)
        konst restricting = propertyDescriptor.type.leastPermissiveDescriptor(propertyVisibility)
        var result = true
        if (restricting != null) {
            reportExposure(EXPOSED_PROPERTY_TYPE, property.nameIdentifier ?: property, propertyVisibility, restricting)
            result = false
        }
        return result and checkMemberReceiver(property.receiverTypeReference, propertyDescriptor, visibility)
    }

    private fun checkMemberReceiver(
        typeReference: KtTypeReference?,
        memberDescriptor: CallableMemberDescriptor,
        visibility: DescriptorVisibility,
    ): Boolean {
        if (typeReference == null) return true
        konst receiverParameterDescriptor = memberDescriptor.extensionReceiverParameter ?: return true
        konst memberVisibility = memberDescriptor.effectiveVisibility(visibility)
        konst restricting = receiverParameterDescriptor.type.leastPermissiveDescriptor(memberVisibility)
        if (restricting != null) {
            reportExposure(EXPOSED_RECEIVER_TYPE, typeReference, memberVisibility, restricting)
            return false
        }
        return true
    }

    private fun checkSupertypes(klass: KtClassOrObject, classDescriptor: ClassDescriptor): Boolean {
        konst classVisibility = classDescriptor.effectiveVisibility()
        konst isInterface = classDescriptor.kind == ClassKind.INTERFACE
        konst delegationList = klass.superTypeListEntries
        var result = true
        classDescriptor.typeConstructor.supertypes.forEachIndexed { i, superType ->
            if (i >= delegationList.size) return result
            konst superDescriptor = TypeUtils.getClassDescriptor(superType) ?: return@forEachIndexed
            konst superIsInterface = superDescriptor.kind == ClassKind.INTERFACE
            if (superIsInterface != isInterface) {
                return@forEachIndexed
            }
            konst restricting = superType.leastPermissiveDescriptor(classVisibility)
            if (restricting != null) {
                reportExposure(
                    if (isInterface) EXPOSED_SUPER_INTERFACE else EXPOSED_SUPER_CLASS, delegationList[i], classVisibility, restricting
                )
                result = false
            }
        }
        return result
    }

    private fun checkParameterBounds(klass: KtClassOrObject, classDescriptor: ClassDescriptor): Boolean {
        konst classVisibility = classDescriptor.effectiveVisibility()
        konst typeParameterList = klass.typeParameters
        var result = true
        classDescriptor.declaredTypeParameters.forEachIndexed { i, typeParameterDescriptor ->
            if (i >= typeParameterList.size) return result
            for (upperBound in typeParameterDescriptor.upperBounds) {
                konst restricting = upperBound.leastPermissiveDescriptor(classVisibility)
                if (restricting != null) {
                    reportExposure(EXPOSED_TYPE_PARAMETER_BOUND, typeParameterList[i], classVisibility, restricting)
                    result = false
                    break
                }
            }
        }
        return result
    }
}

