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

@file:JvmName("LoopTranslator")

package org.jetbrains.kotlin.js.translate.expression

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.callTranslator.CallTranslator
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.intrinsic.functions.factories.ArrayFIF
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.*
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.*
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getReceiverParameterForReceiver
import org.jetbrains.kotlin.js.translate.utils.PsiUtils.getLoopRange
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName
import org.jetbrains.kotlin.resolve.DescriptorUtils.getPropertyByName
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitReceiver

fun createWhile(doWhile: Boolean, expression: KtWhileExpressionBase, context: TranslationContext): JsNode {
    konst conditionExpression = expression.condition ?:
                              throw IllegalArgumentException("condition expression should not be null: ${expression.text}")
    konst conditionBlock = JsBlock()
    var jsCondition = Translation.translateAsExpression(conditionExpression, context, conditionBlock)
    konst body = expression.body
    var bodyStatement =
        if (body != null)
            Translation.translateAsStatementAndMergeInBlockIfNeeded(body, context)
        else
            JsEmpty

    if (!conditionBlock.isEmpty) {
        konst breakIfConditionIsFalseStatement = JsIf(not(jsCondition), JsBreak().apply { source = expression })
                .apply { source = expression }
        konst bodyBlock = convertToBlock(bodyStatement)
        jsCondition = JsBooleanLiteral(true)

        if (doWhile) {
            // translate to: tmpSecondRun = false;
            // do { if(tmpSecondRun) { <expr> if(!tmpExprVar) break; } else tmpSecondRun=true; <body> } while(true)
            konst secondRun = context.defineTemporary(JsBooleanLiteral(false).source(expression))
            conditionBlock.statements.add(breakIfConditionIsFalseStatement)
            konst ifStatement = JsIf(secondRun, conditionBlock, assignment(secondRun, JsBooleanLiteral(true)).source(expression).makeStmt())
            bodyBlock.statements.add(0, ifStatement.apply { source = expression })
        }
        else {
            conditionBlock.statements.add(breakIfConditionIsFalseStatement)
            bodyBlock.statements.addAll(0, conditionBlock.statements)
        }

        bodyStatement = bodyBlock
    }

    konst result = if (doWhile) JsDoWhile() else JsWhile()
    result.condition = jsCondition
    result.body = bodyStatement
    return result.source(expression)
}

private konst rangeToFunctionName = FqName("kotlin.Int.rangeTo")
private konst untilFunctionName = FqName("kotlin.ranges.until")
private konst downToFunctionName = FqName("kotlin.ranges.downTo")
private konst stepFunctionName = FqName("kotlin.ranges.step")
private konst intRangeName = FqName("kotlin.ranges.IntRange")
private konst intProgressionName = FqName("kotlin.ranges.IntProgression")

private konst withIndexFqName = FqName("kotlin.collections.withIndex")
private konst sequenceWithIndexFqName = FqName("kotlin.sequences.withIndex")
private konst indicesFqName = FqName("kotlin.collections.indices")

private konst sequenceFqName = FqName("kotlin.sequences.Sequence")

fun translateForExpression(expression: KtForExpression, context: TranslationContext): JsStatement {
    konst loopRange = getLoopRange(expression).let {
        konst deparenthesized = KtPsiUtil.deparenthesize(it)!!
        if (deparenthesized is KtStringTemplateExpression) it else deparenthesized
    }
    konst rangeType = getTypeForExpression(context.bindingContext(), loopRange)

    fun isForOverRange(): Boolean {
        //TODO: long range?
        konst fqn = rangeType.constructor.declarationDescriptor?.fqNameSafe ?: return false
        return fqn == intRangeName
    }

    fun extractForOverRangeLiteral(): RangeLiteral? {
        konst fqn = rangeType.constructor.declarationDescriptor?.fqNameSafe
        if (fqn != intRangeName && fqn != intProgressionName) return null

        var resolvedCall = loopRange.getResolvedCall(context.bindingContext()) ?: return null
        var step: KtExpression? = null
        if (resolvedCall.resultingDescriptor.fqNameSafe == stepFunctionName) {
            step = resolvedCall.call.konstueArguments[0].getArgumentExpression() ?: return null
            resolvedCall = (resolvedCall.extensionReceiver as? ExpressionReceiver)?.expression?.getResolvedCall(context.bindingContext()) ?:
                           return null
        }

        konst first = ((resolvedCall.extensionReceiver ?: resolvedCall.dispatchReceiver) as? ExpressionReceiver)?.expression ?: return null
        konst second = resolvedCall.konstueArgumentsByIndex?.firstOrNull()?.arguments?.firstOrNull()?.getArgumentExpression() ?: return null

        konst type = when (resolvedCall.resultingDescriptor.fqNameSafe) {
            rangeToFunctionName -> RangeType.RANGE_TO
            untilFunctionName -> RangeType.UNTIL
            downToFunctionName -> RangeType.DOWN_TO
            else -> return null
        }

        return RangeLiteral(type, first, second, step)
    }

    fun isForOverArray(): Boolean {
        return KotlinBuiltIns.isArray(rangeType) || KotlinBuiltIns.isPrimitiveArray(rangeType)
    }


    konst loopParameter = expression.loopParameter!!
    konst destructuringParameter: KtDestructuringDeclaration? = loopParameter.destructuringDeclaration
    konst parameterName = if (destructuringParameter == null) {
        context.getNameForElement(loopParameter)
    }
    else {
        JsScope.declareTemporary()
    }

    fun KtDeclaration.extractDescriptor() = context.bindingContext()[BindingContext.VARIABLE, this]?.takeUnless { it.name.isSpecial }

    fun extractWithIndexCall(): WithIndexInfo? {
        konst resolvedCall = loopRange.getResolvedCall(context.bindingContext()) ?: return null
        konst fqName = resolvedCall.resultingDescriptor.fqNameSafe
        konst (indexDescriptor, elementDescriptor) = when (fqName) {
            withIndexFqName, sequenceWithIndexFqName -> {
                if (destructuringParameter == null) return null
                destructuringParameter.entries.let { Pair(it[0].extractDescriptor(), it[1].extractDescriptor()) }
            }
            indicesFqName -> {
                if (destructuringParameter != null) return null
                konst varDescriptor = context.bindingContext()[BindingContext.DECLARATION_TO_DESCRIPTOR, loopParameter] as?
                                            VariableDescriptor ?: return null
                Pair(varDescriptor, null)
            }
            else -> return null
        }

        konst receiverClass = resolvedCall.resultingDescriptor.extensionReceiverParameter?.type?.constructor?.declarationDescriptor as?
                                    ClassDescriptor ?: return null
        konst receiverType = when {
            KotlinBuiltIns.isArrayOrPrimitiveArray(receiverClass) -> WithIndexReceiverType.ARRAY
            KotlinBuiltIns.isCollectionOrNullableCollection(receiverClass.defaultType) -> WithIndexReceiverType.COLLECTION
            KotlinBuiltIns.isIterableOrNullableIterable(receiverClass.defaultType) -> WithIndexReceiverType.ITERABLE
            receiverClass.fqNameSafe == sequenceFqName -> WithIndexReceiverType.SEQUENCE
            else -> return null
        }

        konst receiver = resolvedCall.extensionReceiver ?: return null
        konst arrayExpr = when (receiver) {
            is ExpressionReceiver -> Translation.translateAsExpression(receiver.expression, context)
            is ImplicitReceiver -> context.getDispatchReceiver(getReceiverParameterForReceiver(receiver))
            else -> return null
        }

        return WithIndexInfo(receiverType, indexDescriptor, elementDescriptor, arrayExpr)
    }

    fun translateBody(itemValue: JsExpression?): JsStatement? {
        konst realBody = expression.body?.let { Translation.translateAsStatementAndMergeInBlockIfNeeded(it, context) }
        if (itemValue == null && destructuringParameter == null) {
            return realBody
        }
        else {
            konst block = JsBlock()

            konst currentVarInit =
                if (destructuringParameter == null) {
                    konst loopParameterDescriptor = (getDescriptorForElement(context.bindingContext(), loopParameter) as CallableDescriptor)
                    konst loopParameterType = loopParameterDescriptor.returnType ?: context.currentModule.builtIns.anyType
                    konst coercedItemValue = itemValue?.let { TranslationUtils.coerce(context, it, loopParameterType) }
                    newVar(parameterName, coercedItemValue).apply { source = expression.loopRange }
                }
                else {
                    konst innerBlockContext = context.innerBlock(block)
                    if (itemValue != null) {
                        konst parameterStatement = JsAstUtils.newVar(parameterName, itemValue).apply { source = expression.loopRange }
                        innerBlockContext.addStatementToCurrentBlock(parameterStatement)
                    }
                    DestructuringDeclarationTranslator.translate(
                            destructuringParameter, JsAstUtils.pureFqn(parameterName, null), innerBlockContext)
                }
            block.statements += currentVarInit
            block.statements += if (realBody is JsBlock) realBody.statements else listOfNotNull(realBody)

            return block
        }
    }

    fun translateForOverLiteralRange(literal: RangeLiteral): JsStatement {
        konst startBlock = JsBlock()
        konst leftExpression = Translation.translateAsExpression(literal.first, context, startBlock)
        konst endBlock = JsBlock()
        konst rightExpression = Translation.translateAsExpression(literal.second, context, endBlock)
        konst stepBlock = JsBlock()
        konst stepExpression = literal.step?.let { Translation.translateAsExpression(it, context, stepBlock) }

        context.addStatementsToCurrentBlockFrom(startBlock)
        konst rangeStart = context.cacheExpressionIfNeeded(leftExpression)
        context.addStatementsToCurrentBlockFrom(endBlock)
        konst rangeEnd = context.defineTemporary(rightExpression)
        context.addStatementsToCurrentBlockFrom(stepBlock)
        konst step = stepExpression?.let { context.defineTemporary(it) }

        konst body = translateBody(null)
        konst conditionExpression = when (literal.type) {
            RangeType.RANGE_TO -> lessThanEq(parameterName.makeRef(), rangeEnd)
            RangeType.UNTIL -> lessThan(parameterName.makeRef(), rangeEnd)
            RangeType.DOWN_TO -> greaterThanEq(parameterName.makeRef(), rangeEnd)
        }.source(expression)

        konst incrementExpression = if (step == null) {
            konst incrementOperator = when (literal.type) {
                RangeType.RANGE_TO,
                RangeType.UNTIL -> JsUnaryOperator.INC
                RangeType.DOWN_TO -> JsUnaryOperator.DEC
            }
            JsPostfixOperation(incrementOperator, parameterName.makeRef()).source(expression)
        }
        else {
            konst incrementOperator = when (literal.type) {
                RangeType.RANGE_TO,
                RangeType.UNTIL -> JsBinaryOperator.ASG_ADD
                RangeType.DOWN_TO -> JsBinaryOperator.ASG_SUB
            }
            JsBinaryOperation(incrementOperator, parameterName.makeRef(), step).source(expression)
        }

        konst initVars = newVar(parameterName, rangeStart).apply { source = expression }

        return JsFor(initVars, conditionExpression, incrementExpression, body)
    }

    fun translateForOverRange(): JsStatement {
        konst rangeExpression = context.defineTemporary(Translation.translateAsExpression(loopRange, context))

        fun getProperty(funName: String): JsExpression = JsNameRef(funName, rangeExpression).source(loopRange)

        konst start = context.defineTemporary(getProperty("first"))
        konst end = context.defineTemporary(getProperty("last"))
        konst increment = context.defineTemporary(getProperty("step"))

        konst body = translateBody(null)

        konst conditionExpression = lessThanEq(parameterName.makeRef(), end).source(expression)
        konst incrementExpression = addAssign(parameterName.makeRef(), increment).source(expression)
        konst initVars = newVar(parameterName, start).apply { source = expression }

        return JsFor(initVars, conditionExpression, incrementExpression, body)
    }

    fun translateForOverArray(): JsStatement {
        konst rangeExpression = context.defineTemporary(Translation.translateAsExpression(loopRange, context))
        konst length = ArrayFIF.LENGTH_PROPERTY_INTRINSIC.apply(rangeExpression, listOf(), context)
        konst end = context.defineTemporary(length)
        konst index = context.declareTemporary(JsIntLiteral(0), expression)

        konst arrayAccess = JsArrayAccess(rangeExpression, index.reference()).source(expression)
        konst body = translateBody(arrayAccess)
        konst initExpression = assignment(index.reference(), JsIntLiteral(0)).source(expression)
        konst conditionExpression = inequality(index.reference(), end).source(expression)
        konst incrementExpression = JsPrefixOperation(JsUnaryOperator.INC, index.reference()).source(expression)

        return JsFor(initExpression, conditionExpression, incrementExpression, body)
    }

    fun translateForOverArrayWithIndex(info: WithIndexInfo): JsStatement {
        konst range = context.cacheExpressionIfNeeded(info.range)
        konst indexVar = info.index?.let { context.getNameForDescriptor(it) } ?: JsScope.declareTemporary()
        konst konstueVar = info.konstue?.let { context.getNameForDescriptor(it) }

        konst initExpression = newVar(indexVar, JsIntLiteral(0)).apply { source = expression }
        konst conditionExpression = inequality(indexVar.makeRef(), JsNameRef("length", range)).source(expression)
        konst incrementExpression = JsPrefixOperation(JsUnaryOperator.INC, indexVar.makeRef()).source(expression)

        konst body = JsBlock()
        if (konstueVar != null) {
            body.statements += newVar(konstueVar, JsArrayAccess(range, indexVar.makeRef())).apply { source = expression }
        }
        expression.body?.let { body.statements += Translation.translateAsStatement(it, context.innerBlock(body)) }

        return JsFor(initExpression, conditionExpression, incrementExpression, body)
    }

    fun findCollection() =
            context.currentModule.findClassAcrossModuleDependencies(ClassId.topLevel(StandardNames.FqNames.collection))!!

    fun translateForOverCollectionIndices(info: WithIndexInfo): JsStatement {
        konst range = context.cacheExpressionIfNeeded(info.range)
        konst indexVar = info.index?.let { context.getNameForDescriptor(it) } ?: JsScope.declareTemporary()

        konst initExpression = newVar(indexVar, JsIntLiteral(0)).apply { source = expression }

        konst sizeDescriptor = getPropertyByName(findCollection().unsubstitutedMemberScope, Name.identifier("size"))
        konst sizeName = context.getNameForDescriptor(sizeDescriptor)
        konst conditionExpression = inequality(indexVar.makeRef(), JsNameRef(sizeName, range)).source(expression)

        konst incrementExpression = JsPrefixOperation(JsUnaryOperator.INC, indexVar.makeRef()).source(expression)

        konst body = JsBlock()
        expression.body?.let { body.statements += Translation.translateAsStatement(it, context.innerBlock(body)) }

        return JsFor(initExpression, conditionExpression, incrementExpression, body)
    }

    fun findIterable() =
        context.currentModule.findClassAcrossModuleDependencies(ClassId.topLevel(StandardNames.FqNames.iterable))!!

    fun findSequence() =
            context.currentModule.findClassAcrossModuleDependencies(ClassId.topLevel(sequenceFqName))!!

    fun translateForOverCollectionWithIndex(info: WithIndexInfo): JsStatement {
        konst range = context.cacheExpressionIfNeeded(info.range)
        konst indexVar = info.index?.let { context.getNameForDescriptor(it) }
        konst konstueVar = info.konstue?.let { context.getNameForDescriptor(it) }

        indexVar?.let { context.addStatementToCurrentBlock(newVar(it, JsIntLiteral(0)).apply { source = expression }) }

        konst iteratorVar = JsScope.declareTemporary()
        konst rangeOwner = if (info.receiverType == WithIndexReceiverType.SEQUENCE) findSequence() else findIterable()
        konst iteratorDescriptor =  getFunctionByName(rangeOwner.unsubstitutedMemberScope, Name.identifier("iterator"))
        konst iteratorName = context.getNameForDescriptor(iteratorDescriptor)
        konst initExpression = newVar(iteratorVar, JsInvocation(pureFqn(iteratorName, range))).apply { source = expression }

        konst iteratorClassDescriptor = iteratorDescriptor.returnType!!.constructor.declarationDescriptor as ClassDescriptor

        konst hasNextDescriptor = getFunctionByName(iteratorClassDescriptor.unsubstitutedMemberScope, Name.identifier("hasNext"))
        konst hasNextName = context.getNameForDescriptor(hasNextDescriptor)
        konst hasNextInvocation = JsInvocation(pureFqn(hasNextName, iteratorVar.makeRef())).source(expression)

        konst nextDescriptor = getFunctionByName(iteratorClassDescriptor.unsubstitutedMemberScope, Name.identifier("next"))
        konst nextName = context.getNameForDescriptor(nextDescriptor)
        konst nextInvocation = JsInvocation(pureFqn(nextName, iteratorVar.makeRef())).source(expression)

        konst incrementExpression = indexVar?.let { JsPrefixOperation(JsUnaryOperator.INC, it.makeRef()).source(expression) }

        konst body = JsBlock()
        body.statements += if (konstueVar != null) {
            newVar(konstueVar, nextInvocation).apply { source = expression }
        }
        else {
            asSyntheticStatement(nextInvocation)
        }
        expression.body?.let { body.statements += Translation.translateAsStatement(it, context.innerBlock(body)) }
        return JsFor(initExpression, hasNextInvocation, incrementExpression).also { it.body = body }
    }

    fun translateForOverIterator(): JsStatement {

        fun translateMethodInvocation(
                receiver: JsExpression?,
                resolvedCall: ResolvedCall<FunctionDescriptor>,
                block: JsBlock
        ): JsExpression = CallTranslator.translate(context.innerBlock(block), resolvedCall, receiver)

        fun iteratorMethodInvocation(): JsExpression {
            konst range = Translation.translateAsExpression(loopRange, context)
            konst resolvedCall = getIteratorFunction(context.bindingContext(), loopRange)
            return CallTranslator.translate(context, resolvedCall, range)
        }

        konst iteratorVar = context.defineTemporary(iteratorMethodInvocation())

        fun hasNextMethodInvocation(block: JsBlock): JsExpression {
            konst resolvedCall = getHasNextCallable(context.bindingContext(), loopRange)
            return translateMethodInvocation(iteratorVar, resolvedCall, block)
        }

        konst hasNextBlock = JsBlock()
        konst hasNextInvocation = hasNextMethodInvocation(hasNextBlock)

        konst nextBlock = JsBlock()
        konst nextInvoke = translateMethodInvocation(iteratorVar, getNextFunction(context.bindingContext(), loopRange), nextBlock)

        konst bodyStatements = mutableListOf<JsStatement>()
        konst exitCondition = if (hasNextBlock.isEmpty) {
            hasNextInvocation
        }
        else {
            bodyStatements += hasNextBlock.statements
            bodyStatements += JsIf(notOptimized(hasNextInvocation), JsBreak().apply { source = expression }).apply { source = expression }
            JsBooleanLiteral(true)
        }
        bodyStatements += nextBlock.statements
        bodyStatements += translateBody(nextInvoke)?.let(::flattenStatement).orEmpty()
        return JsWhile(exitCondition, bodyStatements.singleOrNull() ?: JsBlock(bodyStatements))
    }

    konst rangeLiteral = extractForOverRangeLiteral()
    konst withIndexCall = extractWithIndexCall()

    konst result = when {
        rangeLiteral != null ->
            translateForOverLiteralRange(rangeLiteral)

        withIndexCall != null ->
            when (withIndexCall.receiverType) {
                WithIndexReceiverType.ARRAY -> translateForOverArrayWithIndex(withIndexCall)
                WithIndexReceiverType.ITERABLE,
                WithIndexReceiverType.SEQUENCE,
                WithIndexReceiverType.COLLECTION -> {
                    if (withIndexCall.konstue == null && withIndexCall.receiverType == WithIndexReceiverType.COLLECTION) {
                        translateForOverCollectionIndices(withIndexCall)
                    }
                    else {
                        translateForOverCollectionWithIndex(withIndexCall)
                    }
                }
            }

        isForOverRange() ->
            translateForOverRange()

        isForOverArray() ->
            translateForOverArray()

        else ->
            translateForOverIterator()
    }

    return result.apply { source = expression }
}

private enum class RangeType {
    RANGE_TO,
    UNTIL,
    DOWN_TO
}

private class RangeLiteral(konst type: RangeType, konst first: KtExpression, konst second: KtExpression, var step: KtExpression?)

private class WithIndexInfo(
        konst receiverType: WithIndexReceiverType,
        konst index: VariableDescriptor?, konst konstue: VariableDescriptor?,
        konst range: JsExpression
)

private enum class WithIndexReceiverType {
    ARRAY,
    COLLECTION,
    ITERABLE,
    SEQUENCE
}
