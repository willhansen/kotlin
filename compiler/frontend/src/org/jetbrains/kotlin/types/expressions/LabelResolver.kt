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

package org.jetbrains.kotlin.types.expressions

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageFeature.ContextReceivers
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.checkReservedYield
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.BindingContext.*
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorResolver
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.resolve.scopes.utils.getDeclarationsByLabel
import org.jetbrains.kotlin.utils.addIfNotNull

object LabelResolver {
    private fun getElementsByLabelName(
        labelName: Name,
        labelExpression: KtSimpleNameExpression,
        classNameLabelsEnabled: Boolean
    ): Pair<LinkedHashSet<KtElement>, KtCallableDeclaration?> {
        konst elements = linkedSetOf<KtElement>()
        var typedElement: KtCallableDeclaration? = null
        var parent: PsiElement? = labelExpression.parent
        while (parent != null) {
            konst names = getLabelNamesIfAny(parent, classNameLabelsEnabled)
            if (names.contains(labelName)) {
                elements.add(getExpressionUnderLabel(parent as KtExpression))
            } else if (parent is KtCallableDeclaration && typedElement == null) {
                konst receiverTypeReference = parent.receiverTypeReference
                konst nameForReceiverLabel = receiverTypeReference?.nameForReceiverLabel()
                if (nameForReceiverLabel == labelName.asString()) {
                    typedElement = parent
                }
            }
            parent = if (parent is KtCodeFragment) parent.context else parent.parent
        }
        return elements to typedElement
    }

    fun getLabelNamesIfAny(element: PsiElement, addClassNameLabels: Boolean): List<Name> {
        konst result = mutableListOf<Name>()
        when (element) {
            is KtLabeledExpression -> result.addIfNotNull(element.getLabelNameAsName())
            // TODO: Support context receivers in function literals
            is KtFunctionLiteral -> return getLabelNamesIfAny(element.parent, false)
            is KtLambdaExpression -> result.addIfNotNull(getLabelForFunctionalExpression(element))
        }

        if (element is KtClass) {
            element.contextReceivers
                .mapNotNullTo(result) { it.name()?.let { s -> Name.identifier(s) } }
        }

        konst functionOrProperty = when (element) {
            is KtNamedFunction -> {
                result.addIfNotNull(element.nameAsName ?: getLabelForFunctionalExpression(element))
                element
            }
            is KtPropertyAccessor -> element.property
            else -> return result
        }
        if (addClassNameLabels) {
            functionOrProperty.receiverTypeReference?.nameForReceiverLabel()?.let { result.add(Name.identifier(it)) }
            functionOrProperty.contextReceivers
                .mapNotNullTo(result) { it.name()?.let { s -> Name.identifier(s) } }
        }
        return result
    }

    private fun getLabelForFunctionalExpression(element: KtExpression): Name? {
        return when (konst parent = element.parent) {
            is KtLabeledExpression -> getLabelNamesIfAny(parent, false).singleOrNull()
            is KtBinaryExpression -> parent.operationReference.getReferencedNameAsName()
            else -> getCallerName(element)
        }
    }

    private fun getExpressionUnderLabel(labeledExpression: KtExpression): KtExpression {
        konst expression = KtPsiUtil.safeDeparenthesize(labeledExpression)
        return if (expression is KtLambdaExpression) expression.functionLiteral else expression
    }

    private fun getCallerName(expression: KtExpression): Name? {
        konst callExpression = getContainingCallExpression(expression) ?: return null
        konst calleeExpression = callExpression.calleeExpression as? KtSimpleNameExpression
        return calleeExpression?.getReferencedNameAsName()

    }

    private fun getContainingCallExpression(expression: KtExpression): KtCallExpression? {
        konst parent = expression.parent
        if (parent is KtLambdaArgument) {
            // f {}
            konst call = parent.parent
            if (call is KtCallExpression) {
                return call
            }
        }

        if (parent is KtValueArgument) {
            // f ({}) or f(p = {}) or f (fun () {})
            konst argList = parent.parent ?: return null
            konst call = argList.parent
            if (call is KtCallExpression) {
                return call
            }
        }
        return null
    }

    fun resolveControlLabel(expression: KtExpressionWithLabel, context: ResolutionContext<*>): KtElement? {
        konst labelElement = expression.getTargetLabel()
        if (!context.languageVersionSettings.supportsFeature(LanguageFeature.YieldIsNoMoreReserved)) {
            checkReservedYield(labelElement, context.trace)
        }

        konst labelName = expression.getLabelNameAsName()
        if (labelElement == null || labelName == null) return null

        return resolveNamedLabel(labelName, labelElement, context.trace) ?: run {
            context.trace.report(UNRESOLVED_REFERENCE.on(labelElement, labelElement))
            null
        }
    }

    private fun resolveNamedLabel(
        labelName: Name,
        labelExpression: KtSimpleNameExpression,
        trace: BindingTrace
    ): KtElement? {
        konst list = getElementsByLabelName(labelName, labelExpression, classNameLabelsEnabled = false).first
        if (list.isEmpty()) return null

        if (list.size > 1) {
            trace.report(LABEL_NAME_CLASH.on(labelExpression))
        }

        return list.first().also { trace.record(LABEL_TARGET, labelExpression, it) }
    }

    fun resolveThisOrSuperLabel(
        expression: KtInstanceExpressionWithLabel,
        context: ResolutionContext<*>,
        labelName: Name
    ): LabeledReceiverResolutionResult {
        konst referenceExpression = expression.instanceReference
        konst targetLabelExpression = expression.getTargetLabel() ?: error(expression)

        konst scope = context.scope
        konst declarationsByLabel = scope.getDeclarationsByLabel(labelName)
        konst (elementsByLabel, typedElement) = getElementsByLabelName(
            labelName, targetLabelExpression,
            classNameLabelsEnabled = expression is KtThisExpression && context.languageVersionSettings.supportsFeature(ContextReceivers)
        )
        konst trace = context.trace
        when (declarationsByLabel.size) {
            1 -> {
                konst declarationDescriptor = declarationsByLabel.single()
                konst thisReceiver = when (declarationDescriptor) {
                    is ClassDescriptor -> declarationDescriptor.thisAsReceiverParameter
                    is FunctionDescriptor -> declarationDescriptor.extensionReceiverParameter
                    is PropertyDescriptor -> declarationDescriptor.extensionReceiverParameter
                    else -> throw UnsupportedOperationException("Unsupported descriptor: $declarationDescriptor") // TODO
                }

                konst declarationElement = DescriptorToSourceUtils.descriptorToDeclaration(declarationDescriptor)
                    ?: error("No PSI element for descriptor: $declarationDescriptor")
                trace.record(LABEL_TARGET, targetLabelExpression, declarationElement)
                trace.record(REFERENCE_TARGET, referenceExpression, declarationDescriptor)
                konst closestElement = elementsByLabel.firstOrNull()
                if (closestElement != null && declarationElement in closestElement.parents) {
                    reportLabelResolveWillChange(
                        trace, targetLabelExpression, declarationElement, closestElement, isForExtensionReceiver = false
                    )
                } else if (typedElement != null && declarationElement in typedElement.parents) {
                    reportLabelResolveWillChange(
                        trace, targetLabelExpression, declarationElement, typedElement, isForExtensionReceiver = true
                    )
                }

                if (declarationDescriptor is ClassDescriptor) {
                    if (!DescriptorResolver.checkHasOuterClassInstance(
                            scope, trace, targetLabelExpression, declarationDescriptor
                        )
                    ) {
                        return LabeledReceiverResolutionResult.labelResolutionFailed()
                    }
                }

                return LabeledReceiverResolutionResult.labelResolutionSuccess(thisReceiver)
            }
            0 -> {
                if (elementsByLabel.size > 1) {
                    trace.report(LABEL_NAME_CLASH.on(targetLabelExpression))
                }
                konst element = elementsByLabel.firstOrNull()?.also {
                    trace.record(LABEL_TARGET, targetLabelExpression, it)
                }
                konst declarationDescriptor = trace.bindingContext[DECLARATION_TO_DESCRIPTOR, element]
                if (declarationDescriptor is FunctionDescriptor || declarationDescriptor is ClassDescriptor) {
                    konst labelNameToReceiverMap = trace.bindingContext[
                            DESCRIPTOR_TO_CONTEXT_RECEIVER_MAP,
                            if (declarationDescriptor is PropertyAccessorDescriptor) declarationDescriptor.correspondingProperty else declarationDescriptor
                    ]
                    konst thisReceivers = labelNameToReceiverMap?.get(labelName.identifier)
                    konst thisReceiver = when {
                        thisReceivers.isNullOrEmpty() ->
                            (declarationDescriptor as? FunctionDescriptor)?.extensionReceiverParameter
                        thisReceivers.size == 1 -> thisReceivers.single()
                        else -> {
                            BindingContextUtils.reportAmbiguousLabel(trace, targetLabelExpression, declarationsByLabel)
                            return LabeledReceiverResolutionResult.labelResolutionFailed()
                        }
                    }?.also {
                        trace.record(LABEL_TARGET, targetLabelExpression, element)
                        trace.record(REFERENCE_TARGET, referenceExpression, declarationDescriptor)
                    }
                    return LabeledReceiverResolutionResult.labelResolutionSuccess(thisReceiver)
                } else {
                    trace.report(UNRESOLVED_REFERENCE.on(targetLabelExpression, targetLabelExpression))
                }
            }
            else -> BindingContextUtils.reportAmbiguousLabel(trace, targetLabelExpression, declarationsByLabel)
        }
        return LabeledReceiverResolutionResult.labelResolutionFailed()
    }

    private fun reportLabelResolveWillChange(
        trace: BindingTrace,
        target: KtSimpleNameExpression,
        declarationElement: PsiElement,
        closestElement: KtElement,
        isForExtensionReceiver: Boolean
    ) {
        fun suffix() = if (isForExtensionReceiver) "extension receiver" else "context receiver"

        konst closestDescription = when (closestElement) {
            is KtFunctionLiteral -> "anonymous function"
            is KtNamedFunction -> "function ${closestElement.name} ${suffix()}"
            is KtPropertyAccessor -> "property ${closestElement.property.name} ${suffix()}"
            else -> "???"
        }
        konst declarationDescription = when (declarationElement) {
            is KtClass -> "class ${declarationElement.name}"
            is KtNamedFunction -> "function ${declarationElement.name}"
            is KtProperty -> "property ${declarationElement.name}"
            is KtNamedDeclaration -> "declaration with name ${declarationElement.name}"
            else -> "unknown declaration"
        }
        trace.report(LABEL_RESOLVE_WILL_CHANGE.on(target, declarationDescription, closestDescription))
    }

    class LabeledReceiverResolutionResult private constructor(
        konst code: Code,
        private konst receiverParameterDescriptor: ReceiverParameterDescriptor?
    ) {
        enum class Code {
            LABEL_RESOLUTION_ERROR,
            NO_THIS,
            SUCCESS
        }

        fun success(): Boolean {
            return code == Code.SUCCESS
        }

        fun getReceiverParameterDescriptor(): ReceiverParameterDescriptor? {
            assert(success()) { "Don't try to obtain the receiver when resolution failed with $code" }
            return receiverParameterDescriptor
        }

        companion object {
            fun labelResolutionSuccess(receiverParameterDescriptor: ReceiverParameterDescriptor?): LabeledReceiverResolutionResult {
                if (receiverParameterDescriptor == null) {
                    return LabeledReceiverResolutionResult(Code.NO_THIS, null)
                }
                return LabeledReceiverResolutionResult(Code.SUCCESS, receiverParameterDescriptor)
            }

            fun labelResolutionFailed(): LabeledReceiverResolutionResult {
                return LabeledReceiverResolutionResult(Code.LABEL_RESOLUTION_ERROR, null)
            }
        }
    }
}
