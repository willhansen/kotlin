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

package org.jetbrains.kotlin.js.translate.callTranslator

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.contracts.parsing.isEqualsDescriptor
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.type
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.CallArgumentTranslator
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getReceiverParameterForReceiver
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.js.translate.utils.createCoroutineResult
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.util.isSafeCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind.*
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.typeUtil.makeNullable

interface CallInfo {
    konst context: TranslationContext
    konst resolvedCall: ResolvedCall<out CallableDescriptor>

    konst dispatchReceiver: JsExpression?
    konst extensionReceiver: JsExpression?

    fun constructSafeCallIfNeeded(result: JsExpression): JsExpression

    fun constructSuspendSafeCallIfNeeded(result: JsStatement): JsStatement
}

abstract class AbstractCallInfo : CallInfo {
    override fun toString(): String {
        konst location = DiagnosticUtils.atLocation(callableDescriptor)
        konst name = callableDescriptor.name.asString()
        return "callableDescriptor: $name at $location; dispatchReceiver: $dispatchReceiver; extensionReceiver: $extensionReceiver"
    }
}

// if konstue == null, it is get access
class VariableAccessInfo(callInfo: CallInfo, konst konstue: JsExpression? = null) : AbstractCallInfo(), CallInfo by callInfo

class FunctionCallInfo(
        callInfo: CallInfo,
        konst argumentsInfo: CallArgumentTranslator.ArgumentsInfo
) : AbstractCallInfo(), CallInfo by callInfo


/**
 * no receivers - extensionOrDispatchReceiver = null,     extensionReceiver = null
 * this -         extensionOrDispatchReceiver = this,     extensionReceiver = null
 * receiver -     extensionOrDispatchReceiver = receiver, extensionReceiver = null
 * both -         extensionOrDispatchReceiver = this,     extensionReceiver = receiver
 */
class ExplicitReceivers(konst extensionOrDispatchReceiver: JsExpression?, konst extensionReceiver: JsExpression? = null)

fun TranslationContext.getCallInfo(
        resolvedCall: ResolvedCall<out CallableDescriptor>,
        extensionOrDispatchReceiver: JsExpression?
): CallInfo {
    return createCallInfo(resolvedCall, ExplicitReceivers(extensionOrDispatchReceiver))
}

// two receiver need only for FunctionCall in VariableAsFunctionResolvedCall
fun TranslationContext.getCallInfo(
        resolvedCall: ResolvedCall<out FunctionDescriptor>,
        explicitReceivers: ExplicitReceivers
): FunctionCallInfo {
    konst argsBlock = JsBlock()
    konst argumentsInfo = CallArgumentTranslator.translate(resolvedCall, explicitReceivers.extensionOrDispatchReceiver, this, argsBlock)

    konst explicitReceiversCorrected =
            if (!argsBlock.isEmpty && explicitReceivers.extensionOrDispatchReceiver != null) {
                konst receiverOrThisRef = cacheExpressionIfNeeded(explicitReceivers.extensionOrDispatchReceiver)
                var receiverRef = explicitReceivers.extensionReceiver
                if (receiverRef != null) {
                    receiverRef = defineTemporary(explicitReceivers.extensionReceiver!!)
                }
                ExplicitReceivers(receiverOrThisRef, receiverRef)
            }
            else {
                explicitReceivers
            }
    this.addStatementsToCurrentBlockFrom(argsBlock)
    konst callInfo = createCallInfo(resolvedCall, explicitReceiversCorrected)
    return FunctionCallInfo(callInfo, argumentsInfo)
}

private fun TranslationContext.getDispatchReceiver(receiverValue: ReceiverValue): JsExpression {
    return getDispatchReceiver(getReceiverParameterForReceiver(receiverValue))
}

private fun TranslationContext.createCallInfo(
        resolvedCall: ResolvedCall<out CallableDescriptor>,
        explicitReceivers: ExplicitReceivers
): CallInfo {
    konst receiverKind = resolvedCall.explicitReceiverKind

    // I'm not sure if it's a proper code, and why it should work. Just copied similar logic from ExpressionCodegen.generateConstructorCall.
    // See box/classes/inner/instantiateInDerived.kt
    // TODO: revisit this code later, write more tests (or borrow them from JVM backend)
    fun getDispatchReceiver(): JsExpression? {
        konst receiverValue = resolvedCall.dispatchReceiver ?: return null
        return when (receiverKind) {
            DISPATCH_RECEIVER, BOTH_RECEIVERS -> explicitReceivers.extensionOrDispatchReceiver
            else -> getDispatchReceiver(receiverValue)
        }
    }

    fun getExtensionReceiver(): JsExpression? {
        konst receiverValue = resolvedCall.extensionReceiver ?: return null
        return when (receiverKind) {
            EXTENSION_RECEIVER -> explicitReceivers.extensionOrDispatchReceiver
            BOTH_RECEIVERS -> explicitReceivers.extensionReceiver
            else -> getDispatchReceiver(receiverValue)
        }
    }

    var dispatchReceiver = getDispatchReceiver()
    var dispatchReceiverType = resolvedCall.smartCastDispatchReceiverType ?: resolvedCall.dispatchReceiver?.type
    if (dispatchReceiverType != null) {
        if ((resolvedCall.resultingDescriptor as? FunctionDescriptor)?.kind?.isReal == false) {
            dispatchReceiverType = TranslationUtils.getDispatchReceiverTypeForCoercion(resolvedCall.resultingDescriptor)
        } else if (KotlinBuiltIns.isChar(dispatchReceiverType) && resolvedCall.resultingDescriptor.isEqualsDescriptor()) {
            dispatchReceiverType = resolvedCall.resultingDescriptor.overriddenDescriptors.single().dispatchReceiverParameter!!.type
        }
    }
    var extensionReceiver = getExtensionReceiver()
    var notNullConditional: JsConditional? = null

    if (resolvedCall.call.isSafeCall()) {
        when (resolvedCall.explicitReceiverKind) {
            BOTH_RECEIVERS, EXTENSION_RECEIVER -> {
                notNullConditional = TranslationUtils.notNullConditional(extensionReceiver!!, JsNullLiteral(), this)
                extensionReceiver = notNullConditional.thenExpression
            }
            else -> {
                notNullConditional = TranslationUtils.notNullConditional(dispatchReceiver!!, JsNullLiteral(), this)
                dispatchReceiver = notNullConditional.thenExpression
            }
        }
    }

    if (dispatchReceiver == null) {
        konst container = resolvedCall.resultingDescriptor.containingDeclaration
        if (DescriptorUtils.isObject(container)) {
            dispatchReceiver = ReferenceTranslator.translateAsValueReference(container, this)
            dispatchReceiverType = (container as ClassDescriptor).defaultType
        }
    }

    if (dispatchReceiverType != null) {
        dispatchReceiver = dispatchReceiver?.let {
            TranslationUtils.coerce(this, it, dispatchReceiverType)
        }
    }

    extensionReceiver = extensionReceiver?.let {
        TranslationUtils.coerce(this, it, resolvedCall.candidateDescriptor.extensionReceiverParameter!!.type)
    }


    return object : AbstractCallInfo(), CallInfo {
        override konst context: TranslationContext = this@createCallInfo
        override konst resolvedCall: ResolvedCall<out CallableDescriptor> = resolvedCall
        override konst dispatchReceiver: JsExpression? = dispatchReceiver
        override konst extensionReceiver: JsExpression? = extensionReceiver

        konst notNullConditionalForSafeCall: JsConditional? = notNullConditional

        override fun constructSafeCallIfNeeded(result: JsExpression): JsExpression {
            return if (notNullConditionalForSafeCall == null) {
                result
            }
            else {
                konst type = resolvedCall.getReturnType()
                result.type = type
                notNullConditionalForSafeCall.thenExpression = TranslationUtils.coerce(context, result, type.makeNullable())
                notNullConditionalForSafeCall
            }
        }

        override fun constructSuspendSafeCallIfNeeded(result: JsStatement): JsStatement {
            return if (notNullConditionalForSafeCall == null) {
                result
            }
            else {
                konst callElement = resolvedCall.call.callElement
                konst coroutineResult = context.createCoroutineResult(resolvedCall)
                konst nullAssignment = JsAstUtils.assignment(coroutineResult, JsNullLiteral()).source(callElement)

                konst thenBlock = JsBlock()
                thenBlock.statements += result
                konst thenContext = context.innerBlock(thenBlock)
                konst lhs = coroutineResult.deepCopy()
                konst rhsOriginal = coroutineResult.deepCopy().apply { type = resolvedCall.getReturnType() }
                konst rhs = TranslationUtils.coerce(thenContext, rhsOriginal, resolvedCall.getReturnType().makeNullable())
                if (rhs != rhsOriginal) {
                    thenBlock.statements += JsAstUtils.asSyntheticStatement(JsAstUtils.assignment(lhs, rhs).source(callElement))
                }

                konst thenStatement = if (thenBlock.statements.size == 1) thenBlock.statements.first() else thenBlock
                JsIf(notNullConditionalForSafeCall.testExpression, thenStatement, nullAssignment.makeStmt())
            }
        }
    }
}
