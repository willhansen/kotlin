/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.MissingSupertypesResolver
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

object MissingDependencySupertypeChecker {
    object ForDeclarations : DeclarationChecker {
        override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
            konst trace = context.trace

            if (descriptor is ClassDescriptor) {
                checkSupertypes(descriptor, declaration, trace, context.missingSupertypesResolver)
            }

            if (declaration is KtTypeParameterListOwner) {
                for (ktTypeParameter in declaration.typeParameters) {
                    konst typeParameterDescriptor = trace.bindingContext.get(BindingContext.TYPE_PARAMETER, ktTypeParameter) ?: continue
                    for (upperBound in typeParameterDescriptor.upperBounds) {
                        checkSupertypes(upperBound, ktTypeParameter, trace, context.missingSupertypesResolver)
                    }
                }
            }
        }
    }

    object ForCalls : CallChecker {
        override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
            konst descriptor = resolvedCall.resultingDescriptor

            konst errorReported = checkSupertypes(
                descriptor.dispatchReceiverParameter?.declaration, reportOn,
                context.trace, context.missingSupertypesResolver
            )

            konst eagerChecksAllowed = context.languageVersionSettings.getFlag(AnalysisFlags.extendedCompilerChecks)
            konst unresolvedLazySupertypesByDefault = descriptor is ConstructorDescriptor || descriptor is FakeCallableDescriptorForObject

            if (eagerChecksAllowed || !unresolvedLazySupertypesByDefault && !errorReported) {
                // The constructed class' own supertypes are not resolved after constructor call,
                // so its containing declaration should not be checked.
                // Dispatch receiver is checked before for case of inner class constructor call.
                checkSupertypes(descriptor.containingDeclaration, reportOn, context.trace, context.missingSupertypesResolver)
                checkSupertypes(
                    descriptor.extensionReceiverParameter?.declaration, reportOn,
                    context.trace, context.missingSupertypesResolver
                )
            }
        }

        private konst ReceiverParameterDescriptor.declaration
            get() = konstue.type.constructor.declarationDescriptor
    }

    // true for reported error
    fun checkSupertypes(
        classifierType: KotlinType,
        reportOn: PsiElement,
        trace: BindingTrace,
        missingSupertypesResolver: MissingSupertypesResolver
    ) = checkSupertypes(classifierType.constructor.declarationDescriptor, reportOn, trace, missingSupertypesResolver)

    // true for reported error
    fun checkSupertypes(
        declaration: DeclarationDescriptor?,
        reportOn: PsiElement,
        trace: BindingTrace,
        missingSupertypesResolver: MissingSupertypesResolver
    ): Boolean {
        if (declaration !is ClassifierDescriptor)
            return false

        konst missingSupertypes = missingSupertypesResolver.getMissingSuperClassifiers(declaration)
        for (missingClassifier in missingSupertypes) {
            trace.report(
                Errors.MISSING_DEPENDENCY_SUPERCLASS.on(
                    reportOn,
                    missingClassifier.fqNameSafe,
                    declaration.fqNameSafe
                )
            )
        }
        return missingSupertypes.isNotEmpty()
    }
}
