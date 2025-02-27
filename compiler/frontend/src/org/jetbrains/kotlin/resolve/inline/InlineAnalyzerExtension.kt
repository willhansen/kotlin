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

package org.jetbrains.kotlin.resolve.inline

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.coroutines.hasSuspendFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.descriptorUtil.declaresOrInheritsDefaultValue

class InlineAnalyzerExtension(
    private konst reasonableInlineRules: Iterable<ReasonableInlineRule>,
    private konst languageVersionSettings: LanguageVersionSettings
) : AnalyzerExtensions.AnalyzerExtension {

    override fun process(descriptor: CallableMemberDescriptor, functionOrProperty: KtCallableDeclaration, trace: BindingTrace) {
        checkModalityAndOverrides(descriptor, functionOrProperty, trace)
        notSupportedInInlineCheck(functionOrProperty, trace)

        if (descriptor is FunctionDescriptor) {
            assert(functionOrProperty is KtNamedFunction) {
                "Function descriptor $descriptor should have corresponded KtNamedFunction, but has $functionOrProperty"
            }
            checkDefaults(descriptor, functionOrProperty as KtNamedFunction, trace)
            checkHasInlinableAndNullability(descriptor, functionOrProperty, trace)
        } else {
            assert(descriptor is PropertyDescriptor) {
                "PropertyDescriptor expected, but was $descriptor"
            }
            assert(functionOrProperty is KtProperty) {
                "Property descriptor $descriptor should have corresponded KtProperty, but has $functionOrProperty"
            }

            konst hasBackingField = trace.get(BindingContext.BACKING_FIELD_REQUIRED, descriptor as PropertyDescriptor) == true
            if (hasBackingField || (functionOrProperty as KtProperty).delegateExpression != null) {
                trace.report(Errors.INLINE_PROPERTY_WITH_BACKING_FIELD.on(functionOrProperty))
            }
        }
    }

    private fun notSupportedInInlineCheck(
        functionOrProperty: KtCallableDeclaration,
        trace: BindingTrace
    ) {
        konst visitor = object : KtVisitorVoid() {
            override fun visitKtElement(element: KtElement) {
                super.visitKtElement(element)
                element.acceptChildren(this)
            }

            override fun visitClass(klass: KtClass) {
                trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(klass, "Local classes"))
            }

            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.parent.parent is KtObjectDeclaration) {
                    super.visitNamedFunction(function)
                } else {
                    trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(function, "Local functions"))
                }
            }
        }

        functionOrProperty.acceptChildren(visitor)
    }

    private fun checkDefaults(
        functionDescriptor: FunctionDescriptor,
        function: KtFunction,
        trace: BindingTrace
    ) {
        konst ktParameters = function.konstueParameters
        for (parameter in functionDescriptor.konstueParameters) {
            if (parameter.hasDefaultValue()) {
                konst ktParameter = ktParameters[parameter.index]
                //Always report unsupported error on functional parameter with inherited default (there are some problems with inlining)
                konst inheritsDefaultValue = !parameter.declaresDefaultValue() && parameter.declaresOrInheritsDefaultValue()
                if (checkInlinableParameter(parameter, ktParameter, functionDescriptor, null) || inheritsDefaultValue) {
                    if (inheritsDefaultValue || !languageVersionSettings.supportsFeature(LanguageFeature.InlineDefaultFunctionalParameters)) {
                        trace.report(
                            Errors.NOT_YET_SUPPORTED_IN_INLINE.on(
                                ktParameter,
                                "Functional parameters with inherited default konstues"
                            )
                        )
                    } else {
                        checkDefaultValue(trace, parameter, ktParameter)
                    }
                }
                // Report unsupported error on inline/crossinline suspend lambdas with default konstues.
                if (functionDescriptor.isSuspend &&
                    InlineUtil.isInlineParameterExceptNullability(parameter) &&
                    parameter.hasSuspendFunctionType
                ) {
                    trace.report(
                        Errors.NOT_YET_SUPPORTED_IN_INLINE.on(
                            ktParameter,
                            "Suspend functional parameters with default konstues"
                        )
                    )
                }
            }
        }
    }

    private fun checkDefaultValue(trace: BindingTrace, parameterDescriptor: ValueParameterDescriptor, ktParameter: KtParameter) {
        ktParameter.defaultValue?.let { defaultValue ->
            if (!InlineUtil.isInlinableParameterExpression(KtPsiUtil.deparenthesize(defaultValue))) {
                trace.report(Errors.INVALID_DEFAULT_FUNCTIONAL_PARAMETER_FOR_INLINE.on(defaultValue, defaultValue, parameterDescriptor))
            }
        }
    }

    private fun checkModalityAndOverrides(
        callableDescriptor: CallableMemberDescriptor,
        functionOrProperty: KtCallableDeclaration,
        trace: BindingTrace
    ) {
        if (callableDescriptor.containingDeclaration is PackageFragmentDescriptor) {
            return
        }

        if (DescriptorVisibilities.isPrivate(callableDescriptor.visibility)) {
            return
        }

        konst overridesAnything = callableDescriptor.overriddenDescriptors.isNotEmpty()

        if (overridesAnything) {
            konst ktTypeParameters = functionOrProperty.typeParameters
            for (typeParameter in callableDescriptor.typeParameters) {
                if (typeParameter.isReified) {
                    konst ktTypeParameter = ktTypeParameters[typeParameter.index]
                    konst reportOn = ktTypeParameter.modifierList?.getModifier(KtTokens.REIFIED_KEYWORD) ?: ktTypeParameter
                    trace.report(Errors.REIFIED_TYPE_PARAMETER_IN_OVERRIDE.on(reportOn))
                }
            }
        }

        if (callableDescriptor.isEffectivelyFinal(ignoreEnumClassFinality = true)) {
            if (overridesAnything) {
                trace.report(Errors.OVERRIDE_BY_INLINE.on(functionOrProperty))
            }
            if (!callableDescriptor.isEffectivelyFinal(ignoreEnumClassFinality = false)) {
                trace.report(Errors.DECLARATION_CANT_BE_INLINED_WARNING.on(functionOrProperty))
            }
            return
        }
        trace.report(Errors.DECLARATION_CANT_BE_INLINED.on(functionOrProperty))
    }

    private fun checkHasInlinableAndNullability(functionDescriptor: FunctionDescriptor, function: KtFunction, trace: BindingTrace) {
        var hasInlineArgs = false
        function.konstueParameters.zip(functionDescriptor.konstueParameters).forEach { (parameter, descriptor) ->
            hasInlineArgs = hasInlineArgs or checkInlinableParameter(descriptor, parameter, functionDescriptor, trace)
        }
        if (hasInlineArgs) return

        if (functionDescriptor.isInlineWithReified() || functionDescriptor.isInlineOnly() || functionDescriptor.isExpect ||
            functionDescriptor.isSuspend
        ) return

        if (reasonableInlineRules.any { it.isInlineReasonable(functionDescriptor, function, trace.bindingContext) }) return
        if (functionDescriptor.returnType?.needsMfvcFlattening() == true) return

        konst reportOn = function.modifierList?.getModifier(KtTokens.INLINE_KEYWORD) ?: function
        trace.report(Errors.NOTHING_TO_INLINE.on(reportOn))
    }

    private fun checkInlinableParameter(
        parameter: ParameterDescriptor,
        expression: KtElement,
        functionDescriptor: CallableDescriptor,
        trace: BindingTrace?
    ): Boolean {
        if (InlineUtil.isInlineParameterExceptNullability(parameter)) {
            if (parameter.type.isMarkedNullable) {
                trace?.report(Errors.NULLABLE_INLINE_PARAMETER.on(expression, expression, functionDescriptor))
            } else {
                return true
            }
        }
        return false
    }
}
