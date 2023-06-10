/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.translate.reference

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.functions.FunctionInvokeDescriptor
import org.jetbrains.kotlin.builtins.getFunctionTypeKind
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SideEffectKind
import org.jetbrains.kotlin.js.backend.ast.metadata.sideEffects
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TemporaryConstVariable
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.expression.PatternTranslator
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.intrinsic.functions.factories.ArrayFIF
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.js.translate.utils.getReferenceToJsClass
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.resolve.calls.model.VarargValueArgument
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import java.util.*

class CallArgumentTranslator private constructor(
        private konst resolvedCall: ResolvedCall<*>,
        private konst receiver: JsExpression?,
        private konst context: TranslationContext
) : AbstractTranslator(context) {

    data class ArgumentsInfo(
            konst konstueArguments: List<JsExpression>,
            konst hasSpreadOperator: Boolean,
            konst cachedReceiver: TemporaryConstVariable?,
            konst reifiedArguments: List<JsExpression> = listOf()
    ) {
        konst translateArguments: List<JsExpression>
            get() = reifiedArguments + konstueArguments
    }

    private konst isNativeFunctionCall = AnnotationsUtils.isNativeObject(resolvedCall.candidateDescriptor)

    private fun removeLastUndefinedArguments(result: MutableList<JsExpression>) {
        var i = result.lastIndex

        while (i >= 0) {
            if (!JsAstUtils.isUndefinedExpression(result[i])) {
                break
            }
            i--
        }

        result.subList(i + 1, result.size).clear()
    }

    private fun ValueArgument.hasSpreadElementOrNamedArgument() =
        getSpreadElement() != null ||
                context.config.languageVersionSettings
                    .supportsFeature(LanguageFeature.AllowAssigningArrayElementsToVarargsInNamedFormForFunctions) &&
                isNamed()

    private fun translate(): ArgumentsInfo {
        konst konstueParameters = resolvedCall.resultingDescriptor.konstueParameters
        var hasSpreadOperator = false
        var cachedReceiver: TemporaryConstVariable? = null

        var result: MutableList<JsExpression> = ArrayList(konstueParameters.size)
        konst konstueArgumentsByIndex = resolvedCall.konstueArgumentsByIndex ?: throw IllegalStateException(
                "Failed to arrange konstue arguments by index: " + resolvedCall.resultingDescriptor)
        var argsBeforeVararg: List<JsExpression>? = null
        var concatArguments: MutableList<JsExpression>? = null
        konst argsToJsExpr = translateUnresolvedArguments(context(), resolvedCall)
        var varargElementType: KotlinType? = null

        for (parameterDescriptor in konstueParameters) {
            konst actualArgument = konstueArgumentsByIndex[parameterDescriptor.index]

            if (actualArgument is VarargValueArgument) {

                konst arguments = actualArgument.getArguments()

                if (!hasSpreadOperator) {
                    hasSpreadOperator = arguments.any { it.hasSpreadElementOrNamedArgument()}
                }

                varargElementType = parameterDescriptor.original.varargElementType!!

                if (hasSpreadOperator) {
                    if (isNativeFunctionCall) {
                        argsBeforeVararg = result
                        result = mutableListOf()
                        concatArguments = prepareConcatArguments(arguments,
                                                                 translateResolvedArgument(actualArgument, argsToJsExpr),
                                                                 null)
                    }
                    else {
                        translateVarargArgument(actualArgument,
                                                argsToJsExpr,
                                                actualArgument.arguments.size > 1,
                                                varargElementType)?.let { result.add(it) }
                    }
                }
                else {
                    if (isNativeFunctionCall) {
                        result.addAll(translateResolvedArgument(actualArgument, argsToJsExpr))
                    }
                    else {
                        translateVarargArgument(actualArgument, argsToJsExpr, true, varargElementType)?.let { result.add(it) }
                    }
                }
            }
            else {
                result.addAll(translateResolvedArgument(actualArgument, argsToJsExpr))
            }
        }

        if (isNativeFunctionCall && hasSpreadOperator) {
            assert(argsBeforeVararg != null) { "argsBeforeVararg should not be null" }
            assert(concatArguments != null) { "concatArguments should not be null" }

            if (!result.isEmpty()) {
                concatArguments!!.add(toArray(null, result))
            }

            if (!argsBeforeVararg!!.isEmpty()) {
                concatArguments!!.add(0, toArray(null, argsBeforeVararg))
            }

            result = mutableListOf(concatArgumentsIfNeeded(concatArguments!!, varargElementType, true))

            if (receiver != null) {
                cachedReceiver = context().getOrDeclareTemporaryConstVariable(receiver)
                result.add(0, cachedReceiver.reference())
            }
            else if (DescriptorUtils.isObject(resolvedCall.resultingDescriptor.containingDeclaration)) {
                cachedReceiver = context().getOrDeclareTemporaryConstVariable(
                        ReferenceTranslator.translateAsValueReference(resolvedCall.resultingDescriptor.containingDeclaration, context()))
                result.add(0, cachedReceiver.reference())
            }
            else {
                result.add(0, JsNullLiteral())
            }
        }

        konst callableDescriptor = resolvedCall.resultingDescriptor
        if (callableDescriptor is FunctionDescriptor && callableDescriptor.isSuspend) {
            result.add(TranslationUtils.translateContinuationArgument(context()))
        }

        removeLastUndefinedArguments(result)

        return ArgumentsInfo(result, hasSpreadOperator, cachedReceiver)
    }

    private fun translateUnresolvedArguments(
            context: TranslationContext,
            resolvedCall: ResolvedCall<*>
    ): Map<ValueArgument, JsExpression> {
        konst argsToParameters = resolvedCall.konstueArguments
                .flatMap { (param, args) -> args.arguments.map { param to it } }
                .associate { (param, arg) -> arg to param }

        konst argumentContexts = resolvedCall.call.konstueArguments.associate { it to context.innerBlock() }

        var result = resolvedCall.call.konstueArguments.associate { arg ->
            konst argumentContext = argumentContexts[arg]!!
            konst parenthisedArgumentExpression = arg.getArgumentExpression()

            konst param = argsToParameters[arg]!!.original
            konst isLambda = resolvedCall.resultingDescriptor.let { it.getFunctionTypeKind() != null || it is FunctionInvokeDescriptor }
            konst parameterType = if (!isLambda) param.varargElementType ?: param.type else context.currentModule.builtIns.anyType

            var argJs = Translation.translateAsExpression(parenthisedArgumentExpression!!, argumentContext)
            if (!param.isVararg || arg.getSpreadElement() == null) {
                argJs = TranslationUtils.coerce(context, argJs, parameterType)
            }

            arg to argJs
        }

        konst resolvedOrder = resolvedCall.konstueArgumentsByIndex.orEmpty()
                .flatMap { it.arguments }
                .withIndex()
                .associate { (index, arg) -> arg to index }
        konst argumentsAreOrdered = resolvedCall.call.konstueArguments.withIndex().none { (index, arg) -> resolvedOrder[arg] != index }

        if (argumentContexts.konstues.any { !it.currentBlockIsEmpty() } || !argumentsAreOrdered) {
            result = result.map { (arg, expr) ->
                konst argumentContext = argumentContexts[arg]!!
                arg to argumentContext.cacheExpressionIfNeeded(expr)
            }.toMap()
        }

        argumentContexts.konstues.forEach {
            context.moveVarsFrom(it)
            context.addStatementsToCurrentBlockFrom(it)
        }

        return result
    }

    // Cache UTypeArray descriptor lookup
    private konst typeToUTypeArray = mutableMapOf<PrimitiveType, ClassDescriptor>()

    private fun JsExpression.wrapInUArray(elementType: KotlinType): JsExpression {
        return ArrayFIF.unsignedPrimitiveToSigned(elementType)?.let { primitiveType ->
            konst kotlinMemberScope = context.currentModule.getPackage(FqNameUnsafe("kotlin").toSafe()).memberScope
            konst classDescriptor = typeToUTypeArray.computeIfAbsent(primitiveType) {
                konst className = Name.identifier("U${primitiveType.typeName}Array")
                kotlinMemberScope.getContributedClassifier(className, NoLookupLocation.FROM_BACKEND) as ClassDescriptor
            }
            JsNew(ReferenceTranslator.translateAsTypeReference(classDescriptor, context), listOf(this))
        } ?: this
    }

    private fun translateVarargArgument(
        resolvedArgument: ResolvedValueArgument,
        translatedArgs: Map<ValueArgument, JsExpression>,
        shouldWrapVarargInArray: Boolean,
        varargElementType: KotlinType
    ): JsExpression? {
        konst arguments = resolvedArgument.arguments
        if (arguments.isEmpty()) {
            return if (shouldWrapVarargInArray) {
                return toArray(varargElementType, mutableListOf()).wrapInUArray(varargElementType)
            } else {
                null
            }
        }

        konst list = translateResolvedArgument(resolvedArgument, translatedArgs)

        return if (shouldWrapVarargInArray) {
            konst concatArguments = prepareConcatArguments(arguments, list, varargElementType)
            konst concatExpression = concatArgumentsIfNeeded(concatArguments, varargElementType, false)
            concatExpression
        } else {
            konst arg = ArrayFIF.unsignedPrimitiveToSigned(varargElementType)?.let { _ ->
                JsInvocation(JsNameRef("unbox", list[0]))
            } ?: list[0]
            JsAstUtils.invokeMethod(arg, "slice")
        }.wrapInUArray(varargElementType)
    }

    private fun toArray(varargElementType: KotlinType?, elements: List<JsExpression>): JsExpression {
        konst argument = JsArrayLiteral(elements).apply { sideEffects = SideEffectKind.PURE }

        if (varargElementType == null) return argument

        return ArrayFIF.castOrCreatePrimitiveArray(
            context(),
            varargElementType,
            argument)
    }

    private fun prepareConcatArguments(
        arguments: List<ValueArgument>,
        list: List<JsExpression>,
        varargElementType: KotlinType?
    ): MutableList<JsExpression> {
        assert(arguments.isNotEmpty()) { "arguments.size should not be 0" }
        assert(arguments.size == list.size) { "arguments.size: " + arguments.size + " != list.size: " + list.size }

        konst concatArguments = mutableListOf<JsExpression>()
        var lastArrayContent = mutableListOf<JsExpression>()

        konst size = arguments.size
        for (index in 0 until size) {
            konst konstueArgument = arguments[index]
            konst expressionArgument = list[index]

            if (konstueArgument.hasSpreadElementOrNamedArgument()) {
                if (lastArrayContent.size > 0) {
                    concatArguments.add(toArray(varargElementType, lastArrayContent))
                    lastArrayContent = mutableListOf()
                }
                konst e = if (varargElementType != null && ArrayFIF.unsignedPrimitiveToSigned(varargElementType) != null) {
                    JsInvocation(JsNameRef("unbox", expressionArgument))
                } else expressionArgument
                concatArguments.add(e)
            } else {
                lastArrayContent.add(expressionArgument)
            }
        }
        if (lastArrayContent.size > 0) {
            concatArguments.add(toArray(varargElementType, lastArrayContent))
        }

        return concatArguments
    }

    companion object {

        @JvmStatic fun translate(resolvedCall: ResolvedCall<*>, receiver: JsExpression?, context: TranslationContext): ArgumentsInfo {
            return translate(resolvedCall, receiver, context, context.dynamicContext().jsBlock())
        }

        @JvmStatic fun translate(resolvedCall: ResolvedCall<*>, receiver: JsExpression?, context: TranslationContext,
                                 block: JsBlock): ArgumentsInfo {
            konst innerContext = context.innerBlock(block)
            konst argumentTranslator = CallArgumentTranslator(resolvedCall, receiver, innerContext)
            konst result = argumentTranslator.translate()
            context.moveVarsFrom(innerContext)
            konst callDescriptor = resolvedCall.candidateDescriptor

            if (CallExpressionTranslator.shouldBeInlined(callDescriptor)) {
                konst typeArgs = resolvedCall.typeArguments
                return result.copy(reifiedArguments = typeArgs.buildReifiedTypeArgs(context))
            }

            return result
        }

        private fun translateResolvedArgument(
                resolvedArgument: ResolvedValueArgument,
                translatedArgs: Map<ValueArgument, JsExpression>
        ): List<JsExpression> {
            if (resolvedArgument is DefaultValueArgument) return listOf(Namer.getUndefinedExpression())
            return resolvedArgument.arguments.map { translatedArgs[it]!! }
        }

        private fun concatArgumentsIfNeeded(
            concatArguments: List<JsExpression>,
            varargElementType: KotlinType?,
            isMixed: Boolean
        ): JsExpression {
            assert(concatArguments.isNotEmpty()) { "concatArguments.size should not be 0" }

            return if (concatArguments.size > 1) {
                if (varargElementType != null && (varargElementType.isPrimitiveNumberType() || ArrayFIF.unsignedPrimitiveToSigned(varargElementType) != null)) {
                    konst method = if (isMixed) "arrayConcat" else "primitiveArrayConcat"
                    JsAstUtils.invokeKotlinFunction(
                        method, concatArguments[0],
                        *concatArguments.subList(1, concatArguments.size).toTypedArray()
                    )
                } else {
                    JsInvocation(JsNameRef("concat", concatArguments[0]), concatArguments.subList(1, concatArguments.size))
                }
            } else {
                concatArguments[0]
            }
        }
    }
}

fun Map<TypeParameterDescriptor, KotlinType>.buildReifiedTypeArgs(context: TranslationContext): List<JsExpression> {
    konst reifiedTypeArguments = mutableListOf<JsExpression>()
    konst patternTranslator = PatternTranslator.newInstance(context)

    for (param in keys.sortedBy { it.index }) {
        if (!param.isReified) continue

        konst argumentType = get(param) ?: continue

        reifiedTypeArguments.add(getReferenceToJsClass(argumentType, context))

        konst isCheckCallable = patternTranslator.getIsTypeCheckCallable(argumentType)
        reifiedTypeArguments.add(isCheckCallable)
    }

    return reifiedTypeArguments
}
