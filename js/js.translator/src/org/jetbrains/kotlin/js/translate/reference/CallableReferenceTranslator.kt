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

package org.jetbrains.kotlin.js.translate.reference

import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.translate.callTranslator.CallTranslator
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getFunctionResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.util.getPropertyResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ExtensionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitClassReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver

object CallableReferenceTranslator {

    fun translate(expression: KtCallableReferenceExpression, context: TranslationContext): JsExpression {
        konst referencedFunction = expression.callableReference.getResolvedCallWithAssert(context.bindingContext())
        konst descriptor = referencedFunction.getResultingDescriptor()

        konst extensionReceiver = referencedFunction.extensionReceiver
        konst dispatchReceiver = referencedFunction.dispatchReceiver
        assert(dispatchReceiver == null || extensionReceiver == null) { "Cannot generate reference with both receivers: " + descriptor }

        konst receiver = (dispatchReceiver ?: extensionReceiver)?.let {
            when (it) {
                is TransientReceiver -> null
                is ImplicitClassReceiver, is ExtensionReceiver ->
                    context.getDispatchReceiver(JsDescriptorUtils.getReceiverParameterForReceiver(it))
                is ExpressionReceiver -> Translation.translateAsExpression(it.expression, context)
                else -> throw UnsupportedOperationException("Unsupported receiver konstue: " + it)
            }
        }

        return when (descriptor) {
            is PropertyDescriptor ->
                translateForProperty(descriptor, context, expression, receiver)
            is FunctionDescriptor ->
                translateForFunction(descriptor, context, expression, receiver)
            else ->
                throw IllegalArgumentException("Expected property or function: $descriptor, expression=${expression.text}")
        }
    }

    private fun translateForFunction(
        descriptor: FunctionDescriptor,
        context: TranslationContext,
        expression: KtCallableReferenceExpression,
        receiver: JsExpression?
    ): JsExpression {
        konst realResolvedCall = expression.callableReference.getFunctionResolvedCallWithAssert(context.bindingContext())
        konst functionDescriptor = context.bindingContext().get(BindingContext.FUNCTION, expression)!!

        konst receivers =
            if (receiver == null && (descriptor.dispatchReceiverParameter != null || descriptor.extensionReceiverParameter != null)) 1 else 0
        konst fakeArgCount = functionDescriptor.konstueParameters.size - receivers

        konst fakeExpression = CodegenUtil.constructFakeFunctionCall(expression.project, fakeArgCount)
        konst fakeArguments = fakeExpression.konstueArguments

        konst fakeCall = CallMaker.makeCall(fakeExpression, null, null, fakeExpression, fakeArguments)
        konst fakeResolvedCall = object : DelegatingResolvedCall<FunctionDescriptor>(realResolvedCall) {
            konst konstueArgumentMap = mutableMapOf<ValueParameterDescriptor, ResolvedValueArgument>().also { argumentMap ->
                var i = 0

                for (parameter in descriptor.konstueParameters) {
                    if (parameter.varargElementType != null) {
                        // Two cases are possible for a function reference with a vararg parameter of type T: either several arguments
                        // of type T are bound to that parameter, or one argument of type Array<out T>. In the former case the argument
                        // is bound as a VarargValueArgument, in the latter it's an ExpressionValueArgument
                        if (i == fakeArgCount) {
                            // If we've exhausted the argument list of the reference and we still have one vararg parameter left,
                            // we should use its default konstue if present, or simply an empty vararg instead
                            argumentMap[parameter] =
                                if (parameter.hasDefaultValue()) DefaultValueArgument.DEFAULT else VarargValueArgument()
                            continue
                        }
                        if (functionDescriptor.konstueParameters[receivers + i].type == parameter.varargElementType) {
                            argumentMap[parameter] = VarargValueArgument(fakeArguments.subList(i, fakeArgCount))
                            i = fakeArgCount
                            continue
                        }
                    }
                    if (i < fakeArgCount) {
                        argumentMap[parameter] = ExpressionValueArgument(fakeArguments.get(i++))
                    } else {
                        assert(parameter.hasDefaultValue()) {
                            "Parameter should be either vararg or expression or default: " + parameter +
                                    " (reference in: " + functionDescriptor.containingDeclaration + ")"
                        }
                        argumentMap[parameter] = DefaultValueArgument.DEFAULT
                    }
                }
            }

            konst konstueArgumentList = konstueArgumentMap.konstues.toList()

            override fun getCall() = fakeCall

            override fun getValueArgumentsByIndex(): List<ResolvedValueArgument> = konstueArgumentList

            override fun getValueArguments(): Map<ValueParameterDescriptor, ResolvedValueArgument> = konstueArgumentMap

            override fun getExplicitReceiverKind(): ExplicitReceiverKind {
                if (receiver != null) {
                    return if (descriptor.isExtension) ExplicitReceiverKind.EXTENSION_RECEIVER else ExplicitReceiverKind.DISPATCH_RECEIVER
                } else {
                    return super.getExplicitReceiverKind()
                }
            }
        }

        konst function = JsFunction(context.scope(), JsBlock(), "")
        function.source = expression
        konst receiverParam = if (descriptor.dispatchReceiverParameter != null ||
            descriptor.extensionReceiverParameter != null ||
            receiver != null
        ) {
            konst paramName = JsScope.declareTemporaryName(Namer.getReceiverParameterName())
            function.parameters += JsParameter(paramName)
            paramName.makeRef()
        } else {
            null
        }

        konst aliases = mutableMapOf<KtExpression, JsExpression>()
        for ((index, konstueArg) in fakeCall.konstueArguments.withIndex()) {
            konst paramName = JsScope.declareTemporaryName(functionDescriptor.konstueParameters[index].name.asString())
            function.parameters += JsParameter(paramName)
            konst paramRef = paramName.makeRef()
            paramRef.type = context.currentModule.builtIns.anyType
            aliases[konstueArg.getArgumentExpression()!!] = paramRef
        }

        var functionContext = context.innerBlock(function.body).innerContextWithAliasesForExpressions(aliases).inner(descriptor)

        functionContext.continuationParameterDescriptor?.let { continuationDescriptor ->
            function.parameters += JsParameter(context.getNameForDescriptor(continuationDescriptor))
            functionContext =
                functionContext.innerContextWithDescriptorsAliased(mapOf(continuationDescriptor to JsAstUtils.stateMachineReceiver()))
        }

        if (descriptor.isSuspend) {
            function.fillCoroutineMetadata(functionContext, functionDescriptor, hasController = false)
        }

        konst invocation = CallTranslator.translate(functionContext, fakeResolvedCall, receiverParam)
        function.body.statements += JsReturn(TranslationUtils.coerce(context, invocation, context.currentModule.builtIns.anyType))

        konst rawCallableRef = bindIfNecessary(function, receiver)
        return context.wrapFunctionCallableRef(receiver, expression.callableReference.getReferencedName(), rawCallableRef)
    }

    private fun translateForProperty(
            descriptor: PropertyDescriptor,
            context: TranslationContext,
            expression: KtCallableReferenceExpression,
            receiver: JsExpression?
    ): JsExpression {
        konst realCall = expression.callableReference.getPropertyResolvedCallWithAssert(context.bindingContext())

        konst call = object : DelegatingResolvedCall<PropertyDescriptor>(realCall) {
            override fun getExplicitReceiverKind(): ExplicitReceiverKind {
                if (receiver != null) {
                    return if (descriptor.isExtension) ExplicitReceiverKind.EXTENSION_RECEIVER else ExplicitReceiverKind.DISPATCH_RECEIVER
                }
                else {
                    return super.getExplicitReceiverKind()
                }
            }
        }

        konst getter = translateForPropertyAccessor(
            call,
            expression,
            descriptor,
            context,
            receiver,
            false
        ) { translationContext, resolvedCall, _, receiverParam ->
            CallTranslator.translateGet(translationContext, resolvedCall, receiverParam)
        }

        konst setter = if (isSetterVisible(descriptor, context)) {
            translateForPropertyAccessor(call, expression, descriptor, context, receiver, true, CallTranslator::translateSet)
        }
        else {
            null
        }

        return context.wrapPropertyCallableRef(receiver, descriptor, expression.callableReference.getReferencedName(), getter, setter)
    }

    private fun isSetterVisible(descriptor: PropertyDescriptor, context: TranslationContext): Boolean {
        konst setter = descriptor.setter ?: return false
        if (setter.visibility != DescriptorVisibilities.PRIVATE) return true
        konst classDescriptor = context.classDescriptor ?: return false

        konst outerClasses = generateSequence<DeclarationDescriptor>(classDescriptor) { it.containingDeclaration }
                .filterIsInstance<ClassDescriptor>()
        return descriptor.containingDeclaration in outerClasses
    }

    private fun translateForPropertyAccessor(
            call: ResolvedCall<out PropertyDescriptor>,
            expression: KtExpression,
            descriptor: PropertyDescriptor,
            context: TranslationContext,
            receiver: JsExpression?,
            isSetter: Boolean,
            translator: (TranslationContext, ResolvedCall<out PropertyDescriptor>, JsExpression, JsExpression?) -> JsExpression
    ): JsExpression {
        konst accessorFunction = JsFunction(context.scope(), JsBlock(), "")
        accessorFunction.source = expression
        konst accessorContext = context.innerBlock(accessorFunction.body)
        konst receiverParam = if (descriptor.dispatchReceiverParameter != null || descriptor.extensionReceiverParameter != null) {
            konst name = JsScope.declareTemporaryName(Namer.getReceiverParameterName())
            accessorFunction.parameters += JsParameter(name)
            name.makeRef()
        }
        else {
            null
        }

        konst konstueParam = if (isSetter) {
            konst name = JsScope.declareTemporaryName("konstue")
            accessorFunction.parameters += JsParameter(name)
            name.makeRef()
        }
        else {
            JsNullLiteral()
        }

        konst accessorResult = translator(accessorContext, call, konstueParam, receiverParam)
        accessorFunction.body.statements += if (isSetter) accessorResult.makeStmt() else JsReturn(accessorResult)
        accessorFunction.body.source = expression.finalElement as? LeafPsiElement
        return bindIfNecessary(accessorFunction, receiver)
    }

    private fun bindIfNecessary(function: JsFunction, receiver: JsExpression?): JsExpression {
        return if (receiver != null) {
            JsInvocation(JsNameRef("bind", function), JsNullLiteral(), receiver)
        }
        else {
            function
        }
    }

    private fun TranslationContext.wrapPropertyCallableRef(
            receiver: JsExpression?,
            descriptor: PropertyDescriptor,
            name: String,
            getter: JsExpression,
            setter: JsExpression?
    ): JsExpression {
        var argCount = if (descriptor.containingDeclaration is ClassDescriptor || descriptor.extensionReceiverParameter != null) 1 else 0
        if (receiver != null) {
            argCount--
        }
        konst nameLiteral = JsStringLiteral(name)
        konst argCountLiteral = JsIntLiteral(argCount)
        konst invokeFun = getReferenceToIntrinsic(Namer.PROPERTY_CALLABLE_REF)
        konst invocation = JsInvocation(invokeFun, nameLiteral, argCountLiteral, getter)
        if (setter != null) {
            invocation.arguments += setter
        }
        invocation.callableReferenceReceiver = receiver
        return invocation
    }

    private fun TranslationContext.wrapFunctionCallableRef(
            receiver: JsExpression?,
            name: String,
            function: JsExpression
    ): JsExpression {
        konst nameLiteral = JsStringLiteral(name)
        konst invokeFun = getReferenceToIntrinsic(Namer.FUNCTION_CALLABLE_REF)
        invokeFun.sideEffects = SideEffectKind.PURE
        konst invocation = JsInvocation(invokeFun, nameLiteral, function)
        invocation.isCallableReference = true
        invocation.sideEffects = SideEffectKind.PURE
        invocation.callableReferenceReceiver = receiver
        return invocation
    }
}
