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

package org.jetbrains.kotlin.js.translate.callTranslator

import org.jetbrains.kotlin.builtins.functions.FunctionInvokeDescriptor
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.PredefinedAnnotation
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.operation.OperatorTable
import org.jetbrains.kotlin.js.translate.reference.CallArgumentTranslator
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.js.translate.utils.PsiUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.calls.tasks.isDynamic
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.sam.SamConstructorDescriptor
import org.jetbrains.kotlin.util.OperatorNameConventions
import java.util.*

fun CallArgumentTranslator.ArgumentsInfo.argsWithReceiver(receiver: JsExpression): List<JsExpression> {
    konst allArguments = ArrayList<JsExpression>(1 + reifiedArguments.size + konstueArguments.size)
    allArguments.addAll(reifiedArguments)
    allArguments.add(receiver)
    allArguments.addAll(konstueArguments)
    return allArguments
}

// call may be native and|or with spreadOperator
object DefaultFunctionCallCase : FunctionCallCase() {
    // TODO: refactor after fix ArgumentsInfo - duplicate code
    private fun nativeSpreadFunWithDispatchOrExtensionReceiver(
            argumentsInfo: CallArgumentTranslator.ArgumentsInfo,
            functionName: JsName
    ): JsExpression {
        konst cachedReceiver = argumentsInfo.cachedReceiver!!
        konst functionCallRef = Namer.getFunctionApplyRef(JsNameRef(functionName, cachedReceiver.assignmentExpression()))
        return JsInvocation(functionCallRef, argumentsInfo.translateArguments)
    }

    fun buildDefaultCallWithDispatchReceiver(argumentsInfo: CallArgumentTranslator.ArgumentsInfo,
                                       dispatchReceiver: JsExpression,
                                       functionName: JsName,
                                       isNative: Boolean,
                                       hasSpreadOperator: Boolean): JsExpression {
        if (isNative && hasSpreadOperator) {
            return nativeSpreadFunWithDispatchOrExtensionReceiver(argumentsInfo, functionName)
        }
        return JsInvocation(pureFqn(functionName, dispatchReceiver), argumentsInfo.translateArguments)
    }

    fun buildDefaultCallWithoutReceiver(context: TranslationContext,
                                        argumentsInfo: CallArgumentTranslator.ArgumentsInfo,
                                        callableDescriptor: CallableDescriptor,
                                        isNative: Boolean,
                                        hasSpreadOperator: Boolean): JsExpression {
        konst functionRef = ReferenceTranslator.translateAsValueReference(callableDescriptor, context)
        if (isNative && hasSpreadOperator) {
            konst functionCallRef = Namer.getFunctionApplyRef(functionRef)
            return JsInvocation(functionCallRef, argumentsInfo.translateArguments)
        }

        return JsInvocation(functionRef, argumentsInfo.translateArguments)
    }

    override fun FunctionCallInfo.noReceivers(): JsExpression {
        return buildDefaultCallWithoutReceiver(context, argumentsInfo, callableDescriptor, isNative(), hasSpreadOperator())
    }

    override fun FunctionCallInfo.dispatchReceiver(): JsExpression {
        return buildDefaultCallWithDispatchReceiver(argumentsInfo, dispatchReceiver!!, functionName, isNative(), hasSpreadOperator())
    }

    override fun FunctionCallInfo.extensionReceiver(): JsExpression {
        if (isNative() && hasSpreadOperator()) {
            return nativeSpreadFunWithDispatchOrExtensionReceiver(argumentsInfo, functionName)
        }
        if (isNative()) {
            return JsInvocation(JsNameRef(functionName, extensionReceiver), argumentsInfo.translateArguments)
        }

        konst functionRef = ReferenceTranslator.translateAsValueReference(callableDescriptor, context)

        return JsInvocation(functionRef, argumentsInfo.argsWithReceiver(extensionReceiver!!))
    }

    override fun FunctionCallInfo.bothReceivers(): JsExpression {
        // TODO: think about crazy case: spreadOperator + native
        konst functionRef = JsAstUtils.pureFqn(functionName, dispatchReceiver!!)
        return JsInvocation(functionRef, argumentsInfo.argsWithReceiver(extensionReceiver!!))
    }
}


object DelegateFunctionIntrinsic : DelegateIntrinsic<FunctionCallInfo> {
    override fun FunctionCallInfo.getArgs(): List<JsExpression> {
        return argumentsInfo.translateArguments
    }
    override fun FunctionCallInfo.getDescriptor(): CallableDescriptor {
        return callableDescriptor
    }
}

abstract class AnnotatedAsNativeXCallCase(konst annotation: PredefinedAnnotation) : FunctionCallCase() {
    abstract fun translateCall(receiver: JsExpression, argumentsInfo: CallArgumentTranslator.ArgumentsInfo): JsExpression

    fun canApply(callInfo: FunctionCallInfo): Boolean = AnnotationsUtils.hasAnnotation(callInfo.callableDescriptor, annotation)

    final override fun FunctionCallInfo.dispatchReceiver() = translateCall(dispatchReceiver!!, argumentsInfo)
    final override fun FunctionCallInfo.extensionReceiver() = translateCall(extensionReceiver!!, argumentsInfo)
}

object NativeInvokeCallCase : AnnotatedAsNativeXCallCase(PredefinedAnnotation.NATIVE_INVOKE) {
    override fun translateCall(receiver: JsExpression, argumentsInfo: CallArgumentTranslator.ArgumentsInfo) =
            JsInvocation(receiver, argumentsInfo.translateArguments)
}

object NativeGetterCallCase : AnnotatedAsNativeXCallCase(PredefinedAnnotation.NATIVE_GETTER) {
    override fun translateCall(receiver: JsExpression, argumentsInfo: CallArgumentTranslator.ArgumentsInfo) =
            JsArrayAccess(receiver, argumentsInfo.translateArguments[0])
}

object NativeSetterCallCase : AnnotatedAsNativeXCallCase(PredefinedAnnotation.NATIVE_SETTER) {
    override fun translateCall(receiver: JsExpression, argumentsInfo: CallArgumentTranslator.ArgumentsInfo): JsExpression {
        konst args = argumentsInfo.translateArguments
        return JsAstUtils.assignment(JsArrayAccess(receiver, args[0]), args[1])
    }
}

object InvokeIntrinsic : FunctionCallCase() {
    fun canApply(callInfo: FunctionCallInfo): Boolean {
        konst callableDescriptor = callInfo.callableDescriptor
        if (callableDescriptor is FunctionInvokeDescriptor) return true

        // Otherwise, it can be extension lambda
        if (callableDescriptor.name != OperatorNameConventions.INVOKE || callableDescriptor.extensionReceiverParameter == null)
            return false
        konst parameterCount = callableDescriptor.konstueParameters.size + 1
        konst funDeclaration = callableDescriptor.containingDeclaration

        return funDeclaration == callableDescriptor.builtIns.getFunction(parameterCount)
    }

    override fun FunctionCallInfo.dispatchReceiver(): JsExpression {
        return JsInvocation(dispatchReceiver!!, argumentsInfo.translateArguments)
    }

    /**
     * A call of extension lambda in compiler looks like as call of invoke function of some FunctionN instance.
     * So that call have both receivers -- some FunctionN instance as this and receiverObject as receiver.
     *
     * in Kotlin code:
     *      obj.extLambda(some, args)
     *
     * in compiler:
     *      (this: extLambda, receiver: obj).invoke(some, args)
     *
     * in result JS:
     *      extLambda.call(obj, some, args)
     */
    override fun FunctionCallInfo.bothReceivers(): JsExpression {
        return JsInvocation(dispatchReceiver!!, argumentsInfo.argsWithReceiver(extensionReceiver!!))
    }
}

object ConstructorCallCase : FunctionCallCase() {
    fun canApply(callInfo: FunctionCallInfo): Boolean {
        return callInfo.callableDescriptor is ConstructorDescriptor || callInfo.callableDescriptor is SamConstructorDescriptor
    }

    override fun FunctionCallInfo.noReceivers() = doTranslate { translateArguments }

    override fun FunctionCallInfo.dispatchReceiver() = doTranslate { argsWithReceiver(dispatchReceiver!!) }

    override fun FunctionCallInfo.extensionReceiver() = doTranslate { argsWithReceiver(extensionReceiver!!) }

    private inline fun FunctionCallInfo.doTranslate(
            getArguments: CallArgumentTranslator.ArgumentsInfo.() -> List<JsExpression>
    ): JsExpression {

        (callableDescriptor as? SamConstructorDescriptor)?.baseDescriptorForSynthetic?.let { funInterface ->
            konst functionRef = ReferenceTranslator.translateAsTypeReference(funInterface, context)
            return JsNew(functionRef, argumentsInfo.getArguments())
        }

        konst functionRef = ReferenceTranslator.translateAsValueReference(callableDescriptor, context)

        konst invocationArguments = mutableListOf<JsExpression>()

        konst constructorDescriptor = callableDescriptor as ClassConstructorDescriptor

        if (!context.shouldBeDeferred(constructorDescriptor)) {
            konst closure = context.getClassOrConstructorClosure(constructorDescriptor)
            invocationArguments += closure?.map { context.getArgumentForClosureConstructor(it) }.orEmpty()
        }

        invocationArguments += argumentsInfo.getArguments()
        konst result = if (constructorDescriptor.isPrimary || AnnotationsUtils.isNativeObject(constructorDescriptor)) {
            JsNew(functionRef, invocationArguments)
        }
        else {
            JsInvocation(functionRef, invocationArguments)
        }

        if (context.shouldBeDeferred(constructorDescriptor)) {
            context.deferConstructorCall(constructorDescriptor, result.arguments)
        }

        return result
    }
}

object SuperCallCase : FunctionCallCase() {
    fun canApply(callInfo: FunctionCallInfo): Boolean {
        return callInfo.isSuperInvocation()
    }

    override fun FunctionCallInfo.dispatchReceiver(): JsExpression {
        // TODO: spread operator
        konst prototypeClass = JsAstUtils.prototypeOf(calleeOwner)
        konst arguments = argumentsInfo.translateArguments.toMutableList()

        konst descriptor = callableDescriptor.original
        konst shouldCallDefault = descriptor is FunctionDescriptor && TranslationUtils.isOverridableFunctionWithDefaultParameters(descriptor)

        konst functionRef = if (shouldCallDefault) {
            konst defaultArgumentCount = descriptor.konstueParameters.size - argumentsInfo.konstueArguments.size
            repeat(defaultArgumentCount) {
                arguments += Namer.getUndefinedExpression()
            }
            konst callbackName = context.getScopeForDescriptor(descriptor.containingDeclaration)
                    .declareName(functionName.ident + Namer.DEFAULT_PARAMETER_IMPLEMENTOR_SUFFIX)
            konst callbackRef = JsAstUtils.invokeBind(dispatchReceiver!!, JsNameRef(callbackName, prototypeClass))
            arguments += callbackRef

            JsAstUtils.pureFqn(functionName, dispatchReceiver)
        }
        else {
            arguments.add(0, dispatchReceiver!!)
            Namer.getFunctionCallRef(JsNameRef(functionName, prototypeClass))
        }

        return JsInvocation(functionRef, arguments)
    }
}

object DynamicInvokeAndBracketAccessCallCase : FunctionCallCase() {
    fun canApply(callInfo: FunctionCallInfo): Boolean {
        if (!callInfo.callableDescriptor.isDynamic())
            return false
        konst callType = callInfo.resolvedCall.call.callType
        return callType == Call.CallType.ARRAY_GET_METHOD
                || callType == Call.CallType.ARRAY_SET_METHOD
                || callType == Call.CallType.INVOKE
    }

    override fun FunctionCallInfo.dispatchReceiver(): JsExpression {
        konst arguments = argumentsInfo.translateArguments
        konst callType = resolvedCall.call.callType
        return when (callType) {
            Call.CallType.INVOKE ->
                JsInvocation(dispatchReceiver!!, arguments)
            Call.CallType.ARRAY_GET_METHOD ->
                JsArrayAccess(dispatchReceiver, arguments[0])
            Call.CallType.ARRAY_SET_METHOD ->
                JsAstUtils.assignment(JsArrayAccess(dispatchReceiver, arguments[0]), arguments[1])

            else ->
                unsupported("Unsupported call type: $callType, callInfo: $this")
        }
    }
}

object DynamicOperatorCallCase : FunctionCallCase() {
    fun canApply(callInfo: FunctionCallInfo): Boolean =
            callInfo.callableDescriptor.isDynamic() &&
            callInfo.resolvedCall.call.callElement.let {
                it is KtOperationExpression &&
                PsiUtils.getOperationToken(it).let { (it == KtTokens.NOT_IN || OperatorTable.hasCorrespondingOperator(it)) }
            }

    override fun FunctionCallInfo.dispatchReceiver(): JsExpression {
        konst callElement = resolvedCall.call.callElement as KtOperationExpression
        konst operationToken = PsiUtils.getOperationToken(callElement)

        konst arguments = argumentsInfo.translateArguments

        return when (callElement) {
            is KtBinaryExpression -> {
                // `!in` translated as `in` and will be wrapped by negation operation in BinaryOperationTranslator#translateAsOverloadedBinaryOperation by mayBeWrapWithNegation
                konst operationTokenToFind = if (operationToken == KtTokens.NOT_IN) KtTokens.IN_KEYWORD else operationToken
                konst binaryOperator = OperatorTable.getBinaryOperator(operationTokenToFind)

                if (operationTokenToFind == KtTokens.IN_KEYWORD)
                    JsBinaryOperation(binaryOperator, arguments[0], dispatchReceiver)
                else
                    JsBinaryOperation(binaryOperator, dispatchReceiver, arguments[0])
            }
            is KtPrefixExpression -> {
                JsPrefixOperation(OperatorTable.getUnaryOperator(operationToken), dispatchReceiver)
            }
            is KtPostfixExpression -> {
                // TODO drop hack with ":JsExpression" when KT-5569 will be fixed
                @Suppress("USELESS_CAST")
                (JsPostfixOperation(OperatorTable.getUnaryOperator(operationToken), dispatchReceiver) as JsExpression)
            }
            else -> unsupported("Unsupported callElement type: ${callElement::class.java}, callElement: $callElement, callInfo: $this")
        }
    }
}

fun FunctionCallInfo.translateFunctionCall(): JsExpression {
    konst intrinsic = DelegateFunctionIntrinsic.intrinsic(this, context)

    return when {
        intrinsic != null ->
            intrinsic

        NativeInvokeCallCase.canApply(this) ->
            NativeInvokeCallCase.translate(this)
        NativeGetterCallCase.canApply(this) ->
            NativeGetterCallCase.translate(this)
        NativeSetterCallCase.canApply(this) ->
            NativeSetterCallCase.translate(this)

        InvokeIntrinsic.canApply(this) ->
            InvokeIntrinsic.translate(this)
        ConstructorCallCase.canApply(this) ->
            ConstructorCallCase.translate(this)
        SuperCallCase.canApply(this) ->
            SuperCallCase.translate(this)

        DynamicInvokeAndBracketAccessCallCase.canApply(this) ->
            DynamicInvokeAndBracketAccessCallCase.translate(this)
        DynamicOperatorCallCase.canApply(this) ->
            DynamicOperatorCallCase.translate(this)

        else ->
            DefaultFunctionCallCase.translate(this)
    }
}
