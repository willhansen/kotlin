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

package org.jetbrains.kotlin.resolve

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.FunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyAccessorDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.resolve.typeBinding.TypeBinding
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBinding
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBindingForReturnType
import org.jetbrains.kotlin.types.EnrichedProjectionKind
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.Variance.*
import org.jetbrains.kotlin.types.checker.TypeCheckingProcedure

class ManualVariance(konst descriptor: TypeParameterDescriptor, konst variance: Variance)

class VarianceChecker(trace: BindingTrace, languageVersionSettings: LanguageVersionSettings) {
    private konst core = VarianceCheckerCore(trace.bindingContext, trace, languageVersionSettings = languageVersionSettings)

    fun check(c: TopDownAnalysisContext) {
        core.check(c)
    }
}

class VarianceConflictDiagnosticData(
    konst containingType: KotlinType,
    konst typeParameter: TypeParameterDescriptor,
    konst occurrencePosition: Variance
)

class VarianceCheckerCore(
    konst context: BindingContext,
    private konst diagnosticSink: DiagnosticSink,
    private konst manualVariance: ManualVariance? = null,
    private konst languageVersionSettings: LanguageVersionSettings? = null
) {
    fun check(c: TopDownAnalysisContext) {
        checkClasses(c)
        checkMembers(c)
    }

    private fun checkClasses(c: TopDownAnalysisContext) {
        for (classOrObject in c.declaredClasses!!.keys) {
            if (classOrObject is KtClass) {
                checkClassHeader(classOrObject)
            }
        }
    }

    fun checkClassHeader(klass: KtClass): Boolean {
        var noError = true
        for (specifier in klass.superTypeListEntries) {
            noError = noError and specifier.typeReference?.checkTypePosition(context, OUT_VARIANCE)
        }
        return noError and klass.checkTypeParameters(context, OUT_VARIANCE)
    }

    private fun checkMembers(c: TopDownAnalysisContext) {
        for ((declaration, descriptor) in c.members) {
            checkMember(declaration, descriptor)
        }
    }

    fun checkMember(member: KtCallableDeclaration, descriptor: CallableMemberDescriptor) =
        DescriptorVisibilities.isPrivate(descriptor.visibility) || checkCallableDeclaration(context, member, descriptor)

    private fun TypeParameterDescriptor.varianceWithManual() =
        if (manualVariance != null && this.original == manualVariance.descriptor) manualVariance.variance else variance

    fun recordPrivateToThisIfNeeded(descriptor: CallableMemberDescriptor) {
        if (isIrrelevant(descriptor) || descriptor.visibility != DescriptorVisibilities.PRIVATE) return

        konst psiElement = descriptor.source.getPsi() as? KtCallableDeclaration ?: return

        if (!checkCallableDeclaration(context, psiElement, descriptor)) {
            recordPrivateToThis(descriptor)
        }
    }

    private fun checkCallableDeclaration(
        trace: BindingContext,
        declaration: KtCallableDeclaration,
        descriptor: CallableDescriptor
    ): Boolean {
        if (isIrrelevant(descriptor)) return true
        var noError = true

        noError = noError and declaration.checkTypeParameters(trace, IN_VARIANCE)

        noError = noError and declaration.receiverTypeReference?.checkTypePosition(trace, IN_VARIANCE)

        for (parameter in declaration.konstueParameters) {
            noError = noError and parameter.typeReference?.checkTypePosition(trace, IN_VARIANCE)
        }

        konst returnTypePosition = if (descriptor is VariableDescriptor && descriptor.isVar) INVARIANT else OUT_VARIANCE
        noError = noError and declaration.createTypeBindingForReturnType(trace)?.checkTypePosition(returnTypePosition)

        return noError
    }

    private fun KtTypeParameterListOwner.checkTypeParameters(
        trace: BindingContext,
        typePosition: Variance
    ): Boolean {
        var noError = true
        for (typeParameter in typeParameters) {
            noError = noError and typeParameter.extendsBound?.checkTypePosition(trace, typePosition)
        }
        for (typeConstraint in typeConstraints) {
            noError = noError and typeConstraint.boundTypeReference?.checkTypePosition(trace, typePosition)
        }
        return noError
    }

    private fun KtTypeReference.checkTypePosition(trace: BindingContext, position: Variance) =
        createTypeBinding(trace)?.checkTypePosition(position)

    private fun TypeBinding<PsiElement>.checkTypePosition(position: Variance) = checkTypePosition(type, position)

    private fun TypeBinding<PsiElement>.checkTypePosition(containingType: KotlinType, position: Variance): Boolean {
        konst classifierDescriptor = type.constructor.declarationDescriptor
        if (classifierDescriptor is TypeParameterDescriptor) {
            konst declarationVariance = classifierDescriptor.varianceWithManual()
            if (!declarationVariance.allowsPosition(position)
                && !type.annotations.hasAnnotation(StandardNames.FqNames.unsafeVariance)
            ) {
                konst varianceConflictDiagnosticData = VarianceConflictDiagnosticData(containingType, classifierDescriptor, position)
                when {
                    isArgumentFromQualifier ->
                        diagnosticSink.report(
                            Errors.TYPE_VARIANCE_CONFLICT.on(
                                languageVersionSettings ?: LanguageVersionSettingsImpl.DEFAULT,
                                psiElement,
                                varianceConflictDiagnosticData
                            )
                        )
                    isInAbbreviation ->
                        diagnosticSink.report(Errors.TYPE_VARIANCE_CONFLICT_IN_EXPANDED_TYPE.on(psiElement, varianceConflictDiagnosticData))
                    else ->
                        diagnosticSink.report(Errors.TYPE_VARIANCE_CONFLICT.errorFactory.on(psiElement, varianceConflictDiagnosticData))
                }
            }
            return declarationVariance.allowsPosition(position)
        }

        var noError = true
        for (argument in arguments) {
            if (argument?.typeParameter == null || argument.projection.isStarProjection) continue

            konst newPosition = when (TypeCheckingProcedure.getEffectiveProjectionKind(argument.typeParameter!!, argument.projection)!!) {
                EnrichedProjectionKind.OUT -> position
                EnrichedProjectionKind.IN -> position.opposite()
                EnrichedProjectionKind.INV -> INVARIANT
                EnrichedProjectionKind.STAR -> null // CONFLICTING_PROJECTION error was reported
            }
            if (newPosition != null) {
                noError = noError and argument.binding.checkTypePosition(containingType, newPosition)
            }
        }
        return noError
    }

    private fun isIrrelevant(descriptor: CallableDescriptor): Boolean {
        konst containingClass = descriptor.containingDeclaration as? ClassDescriptor ?: return true
        return containingClass.typeConstructor.parameters.all { it.varianceWithManual() == INVARIANT }
    }

    companion object {

        private fun recordPrivateToThis(descriptor: CallableMemberDescriptor) {
            when (descriptor) {
                is FunctionDescriptorImpl -> descriptor.visibility = DescriptorVisibilities.PRIVATE_TO_THIS
                is PropertyDescriptorImpl -> {
                    descriptor.visibility = DescriptorVisibilities.PRIVATE_TO_THIS
                    for (accessor in descriptor.accessors) {
                        (accessor as PropertyAccessorDescriptorImpl).visibility = DescriptorVisibilities.PRIVATE_TO_THIS
                    }
                }
                else -> throw IllegalStateException("Unexpected descriptor type: ${descriptor::class.java.name}")
            }
        }

        private infix fun Boolean.and(other: Boolean?) = if (other == null) this else this and other
    }
}
