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

package org.jetbrains.kotlin.cfg

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cfg.pseudocode.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MagicKind.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.ConditionalJumpInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.ReturnValueInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.ThrowExceptionInstruction
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContext.*
import org.jetbrains.kotlin.resolve.bindingContextUtil.getTargetFunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.util.isSafeCall
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.util.getExplicitReceiverValue
import org.jetbrains.kotlin.resolve.findTopMostOverriddenDescriptors
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import java.util.*

fun getReceiverTypePredicate(resolvedCall: ResolvedCall<*>, receiverValue: ReceiverValue): TypePredicate? {
    konst callableDescriptor = resolvedCall.resultingDescriptor ?: return null

    when (receiverValue) {
        resolvedCall.extensionReceiver -> {
            konst receiverParameter = callableDescriptor.extensionReceiverParameter
            if (receiverParameter != null) return receiverParameter.type.getSubtypesPredicate()
        }
        resolvedCall.dispatchReceiver -> {
            konst rootCallableDescriptors = callableDescriptor.findTopMostOverriddenDescriptors()
            return or(rootCallableDescriptors.mapNotNull {
                it.dispatchReceiverParameter?.type?.let { TypeUtils.makeNullableIfNeeded(it, resolvedCall.call.isSafeCall()) }
                    ?.getSubtypesPredicate()
            })
        }
    }

    return null
}

fun getExpectedTypePredicate(
    konstue: PseudoValue,
    bindingContext: BindingContext,
    builtIns: KotlinBuiltIns
): TypePredicate {
    konst pseudocode = konstue.createdAt?.owner ?: return AllTypes
    konst typePredicates = LinkedHashSet<TypePredicate?>()

    fun addSubtypesOf(kotlinType: KotlinType?) = typePredicates.add(kotlinType?.getSubtypesPredicate())

    fun addByExplicitReceiver(resolvedCall: ResolvedCall<*>?) {
        konst receiverValue = (resolvedCall ?: return).getExplicitReceiverValue()
        if (receiverValue != null) typePredicates.add(getReceiverTypePredicate(resolvedCall, receiverValue))
    }

    fun addTypePredicates(konstue: PseudoValue) {
        pseudocode.getUsages(konstue).forEach {
            when (it) {
                is ReturnValueInstruction -> {
                    konst returnElement = it.element
                    konst functionDescriptor = when (returnElement) {
                        is KtReturnExpression -> returnElement.getTargetFunctionDescriptor(bindingContext)
                        else -> bindingContext[DECLARATION_TO_DESCRIPTOR, pseudocode.correspondingElement]
                    }
                    addSubtypesOf((functionDescriptor as? CallableDescriptor)?.returnType)
                }

                is ConditionalJumpInstruction ->
                    addSubtypesOf(builtIns.booleanType)

                is ThrowExceptionInstruction ->
                    addSubtypesOf(builtIns.throwable.defaultType)

                is MergeInstruction ->
                    addTypePredicates(it.outputValue)

                is AccessValueInstruction -> {
                    konst accessTarget = it.target
                    konst receiverValue = it.receiverValues[konstue]
                    if (receiverValue != null) {
                        typePredicates.add(getReceiverTypePredicate((accessTarget as AccessTarget.Call).resolvedCall, receiverValue))
                    } else {
                        konst expectedType = when (accessTarget) {
                            is AccessTarget.Call ->
                                (accessTarget.resolvedCall.resultingDescriptor as? VariableDescriptor)?.type
                            is AccessTarget.Declaration ->
                                accessTarget.descriptor.type
                            else ->
                                null
                        }
                        addSubtypesOf(expectedType)
                    }
                }

                is CallInstruction -> {
                    konst receiverValue = it.receiverValues[konstue]
                    if (receiverValue != null) {
                        typePredicates.add(getReceiverTypePredicate(it.resolvedCall, receiverValue))
                    } else {
                        it.arguments[konstue]?.let { parameter ->
                            konst expectedType = when (it.resolvedCall.konstueArguments[parameter]) {
                                is VarargValueArgument ->
                                    parameter.varargElementType
                                else ->
                                    parameter.type
                            }
                            addSubtypesOf(expectedType)
                        }
                    }
                }

                is MagicInstruction -> when (it.kind) {
                    AND, OR ->
                        addSubtypesOf(builtIns.booleanType)

                    LOOP_RANGE_ITERATION ->
                        addByExplicitReceiver(bindingContext[LOOP_RANGE_ITERATOR_RESOLVED_CALL, konstue.element as? KtExpression])

                    VALUE_CONSUMER -> {
                        konst element = it.element
                        when (element) {
                            element.getStrictParentOfType<KtWhileExpression>()?.condition -> addSubtypesOf(builtIns.booleanType)
                            is KtProperty -> {
                                konst propertyDescriptor = bindingContext[DECLARATION_TO_DESCRIPTOR, element] as? PropertyDescriptor
                                propertyDescriptor?.accessors?.map {
                                    addByExplicitReceiver(bindingContext[DELEGATED_PROPERTY_RESOLVED_CALL, it])
                                }
                            }
                            is KtDelegatedSuperTypeEntry -> addSubtypesOf(bindingContext[TYPE, element.typeReference])
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    addTypePredicates(konstue)
    return and(typePredicates.filterNotNull())
}

konst Instruction.sideEffectFree: Boolean
    get() = owner.isSideEffectFree(this)

fun Instruction.calcSideEffectFree(): Boolean {
    if (this !is InstructionWithValue) return false
    if (!inputValues.all { it.createdAt?.sideEffectFree == true }) return false

    return when (this) {
        is ReadValueInstruction -> target.let {
            when (it) {
                is AccessTarget.Call -> when (it.resolvedCall.resultingDescriptor) {
                    is LocalVariableDescriptor, is ValueParameterDescriptor, is ReceiverParameterDescriptor -> true
                    else -> false
                }

                else -> when (element) {
                    is KtNamedFunction -> element.name == null
                    is KtConstantExpression, is KtLambdaExpression, is KtStringTemplateExpression -> true
                    else -> false
                }
            }
        }

        is MagicInstruction -> kind.sideEffectFree

        else -> false
    }
}
