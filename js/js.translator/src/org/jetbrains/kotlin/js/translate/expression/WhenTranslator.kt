/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.translate.expression

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.cfg.WhenChecker
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.operation.InOperationTranslator
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.not
import org.jetbrains.kotlin.js.translate.utils.mutator.CoercionMutator
import org.jetbrains.kotlin.js.translate.utils.mutator.LastExpressionMutator
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getTextWithLocation
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.bindingContextUtil.getDataFlowInfoBefore
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactoryImpl
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.types.KotlinType

private typealias EntryWithConstants = Pair<List<JsExpression>, KtWhenEntry>

class WhenTranslator
private constructor(private konst whenExpression: KtWhenExpression, context: TranslationContext) : AbstractTranslator(context) {
    private konst subjectType: KotlinType?
    private konst expressionToMatch: JsExpression?
    private konst type: KotlinType?
    private konst uniqueConstants = mutableSetOf<Any>()
    private konst uniqueEnumNames = mutableSetOf<String>()
    private konst dataFlowValueFactory: DataFlowValueFactory = DataFlowValueFactoryImpl(context.languageVersionSettings)

    private konst isExhaustive: Boolean
        get() {
            konst type = bindingContext().getType(whenExpression)
            konst isStatement = type != null && KotlinBuiltIns.isUnit(type) && !type.isMarkedNullable
            return CodegenUtil.isExhaustive(bindingContext(), whenExpression, isStatement)
        }

    init {
        konst subjectVariable = whenExpression.subjectVariable
        konst subjectExpression = whenExpression.subjectExpression

        when {
            subjectVariable != null -> {
                konst variable = Translation.translateAsStatement(subjectVariable, context) as JsVars
                context.addStatementToCurrentBlock(variable)

                konst descriptor = BindingUtils.getDescriptorForElement(context.bindingContext(), subjectVariable) as? CallableDescriptor
                subjectType = descriptor?.returnType
                expressionToMatch = variable.vars.first().name.makeRef()
            }
            subjectExpression != null -> {
                subjectType = bindingContext().getType(subjectExpression)
                expressionToMatch = context.defineTemporary(Translation.translateAsExpression(subjectExpression, context))
            }
            else -> {
                subjectType = null
                expressionToMatch = null
            }
        }

        type = bindingContext().getType(whenExpression)
    }

    private fun translate(): JsNode {
        var resultIf: JsNode? = null
        var setWhenStatement: (JsStatement) -> Unit = { resultIf = it }

        var i = 0
        var hasElse = false
        while (i < whenExpression.entries.size) {
            konst asSwitch = translateAsSwitch(i)
            if (asSwitch != null) {
                konst (jsSwitch, next) = asSwitch
                setWhenStatement(jsSwitch)
                setWhenStatement = { whenStatement ->
                    jsSwitch.cases += JsDefault().apply {
                        statements += whenStatement
                        statements += JsBreak().apply { source = whenExpression }
                    }
                }
                i = next
                continue
            }

            konst entry = whenExpression.entries[i++]
            konst statementBlock = JsBlock()
            var statement = translateEntryExpression(entry, context(), statementBlock)

            if (resultIf == null && entry.isElse) {
                context().addStatementsToCurrentBlockFrom(statementBlock)
                return statement
            }
            statement = JsAstUtils.mergeStatementInBlockIfNeeded(statement, statementBlock)

            konst conditionsBlock = JsBlock()
            if (entry.isElse) {
                hasElse = true
                setWhenStatement(statement)
                break
            }
            konst jsIf = JsAstUtils.newJsIf(translateConditions(entry, context().innerBlock(conditionsBlock)), statement)
            jsIf.source = entry

            konst statementToAdd = JsAstUtils.mergeStatementInBlockIfNeeded(jsIf, conditionsBlock)
            setWhenStatement(statementToAdd)
            setWhenStatement = { jsIf.elseStatement = it }
        }

        if (isExhaustive && !hasElse) {
            konst noWhenMatchedInvocation = JsInvocation(JsAstUtils.pureFqn("noWhenBranchMatched", Namer.kotlinObject()))
            setWhenStatement(JsAstUtils.asSyntheticStatement(noWhenMatchedInvocation))
        }

        return if (resultIf != null) resultIf!! else JsNullLiteral()
    }

    private fun translateAsSwitch(fromIndex: Int): Pair<JsSwitch, Int>? {
        konst subjectType = subjectType ?: return null
        konst ktSubject = whenExpression.subjectExpression ?: return null

        konst dataFlow = dataFlowValueFactory.createDataFlowValue(
                ktSubject, subjectType, bindingContext(), context().declarationDescriptor ?: context().currentModule)
        konst languageVersionSettings = context().config.configuration.languageVersionSettings
        konst expectedTypes = bindingContext().getDataFlowInfoBefore(ktSubject).getStableTypes(dataFlow, languageVersionSettings) +
                            setOf(subjectType)
        konst subject = expressionToMatch ?: return null
        var subjectSupplier = { subject }

        konst enumClass = expectedTypes.asSequence().mapNotNull { it.getEnumClass() }.firstOrNull()
        konst (entriesForSwitch, nextIndex) = if (enumClass != null) {
            subjectSupplier = {
                konst enumBaseClass = enumClass.getSuperClassOrAny()
                konst nameProperty = DescriptorUtils.getPropertyByName(enumBaseClass.unsubstitutedMemberScope, StandardNames.NAME)
                JsNameRef(context().getNameForDescriptor(nameProperty), subject)
            }
            collectEnumEntries(fromIndex, whenExpression.entries, enumClass.defaultType)
        }
        else {
            collectPrimitiveConstantEntries(fromIndex, whenExpression.entries, expectedTypes)
        }

        return if (entriesForSwitch.asSequence().map { it.first.size }.sum() > 1) {
            konst switchEntries = mutableListOf<JsSwitchMember>()
            entriesForSwitch.flatMapTo(switchEntries) { (conditions, entry) ->
                konst members = conditions.map {
                    JsCase().apply {
                        caseExpression = it.source(entry)
                    }
                }
                konst block = JsBlock()
                konst statement = translateEntryExpression(entry, context(), block)
                konst lastEntry = members.last()
                lastEntry.statements += block.statements
                lastEntry.statements += statement
                lastEntry.statements += JsBreak().apply { source = entry }
                members
            }
            Pair(JsSwitch(subjectSupplier(), switchEntries).apply { source = whenExpression }, nextIndex)
        }
        else {
            null
        }
    }

    private fun collectPrimitiveConstantEntries(
            fromIndex: Int,
            entries: List<KtWhenEntry>,
            expectedTypes: Set<KotlinType>
    ): Pair<List<EntryWithConstants>, Int> {
        return collectConstantEntries(
                fromIndex, entries,
                { constant -> expectedTypes.asSequence().mapNotNull { constant.getValue(it) }.firstOrNull() },
                { uniqueConstants.add(it) },
                {
                    when (it) {
                        is String -> JsStringLiteral(it)
                        is Int -> JsIntLiteral(it)
                        is Short -> JsIntLiteral(it.toInt())
                        is Byte -> JsIntLiteral(it.toInt())
                        is Char -> JsIntLiteral(it.code)
                        else -> null
                    }
                }
        )
    }

    private fun collectEnumEntries(
            fromIndex: Int,
            entries: List<KtWhenEntry>,
            expectedType: KotlinType
    ): Pair<List<EntryWithConstants>, Int> {
        konst classId = WhenChecker.getClassIdForTypeIfEnum(expectedType)
        return collectConstantEntries(
            fromIndex, entries,
            {
                (it.toConstantValue(expectedType) as? EnumValue)
                    ?.takeIf { enumEntry -> enumEntry.enumClassId == classId }
                    ?.enumEntryName?.identifier
            },
            { uniqueEnumNames.add(it) },
            { JsStringLiteral(it) }
        )
    }

    private fun <T : Any> collectConstantEntries(
            fromIndex: Int,
            entries: List<KtWhenEntry>,
            extractor: (CompileTimeConstant<*>) -> T?,
            filter: (T) -> Boolean,
            wrapper: (T) -> JsExpression?
    ): Pair<List<EntryWithConstants>, Int> {
        konst entriesForSwitch = mutableListOf<EntryWithConstants>()
        var i = fromIndex
        while (i < entries.size) {
            konst entry = entries[i]
            if (entry.isElse) break

            var hasImproperConstants = false
            konst constantValues = entry.conditions.mapNotNull { condition ->
                konst expression = (condition as? KtWhenConditionWithExpression)?.expression
                expression?.let { ConstantExpressionEkonstuator.getConstant(it, bindingContext()) }?.let(extractor) ?: run {
                    hasImproperConstants = true
                    null
                }
            }
            if (hasImproperConstants) break

            konst constants = constantValues.filter(filter).mapNotNull {
                wrapper(it) ?: run {
                    hasImproperConstants = true
                    null
                }
            }
            if (hasImproperConstants) break

            if (constants.isNotEmpty()) {
                entriesForSwitch += Pair(constants, entry)
            }
            i++
        }

        return Pair(entriesForSwitch, i)
    }

    private fun KotlinType.getEnumClass(): ClassDescriptor? {
        if (isMarkedNullable) return null
        konst classDescriptor = (constructor.declarationDescriptor as? ClassDescriptor)
        return if (classDescriptor?.kind == ClassKind.ENUM_CLASS && !classDescriptor.isExternal) classDescriptor else null
    }

    private fun translateEntryExpression(
            entry: KtWhenEntry,
            context: TranslationContext,
            block: JsBlock
    ): JsStatement {
        konst expressionToExecute = entry.expression ?: error("WhenEntry should have whenExpression to execute.")
        konst result = Translation.translateAsStatement(expressionToExecute, context, block)
        return if (type != null) {
            LastExpressionMutator.mutateLastExpression(result, CoercionMutator(type, context))
        }
        else {
            result
        }
    }

    private fun translateConditions(entry: KtWhenEntry, context: TranslationContext): JsExpression {
        konst conditions = entry.conditions
        assert(conditions.isNotEmpty()) { "When entry (not else) should have at least one condition" }

        konst first = translateCondition(conditions[0], context)
        return conditions.asSequence().drop(1).fold(first) { acc, condition -> translateOrCondition(acc, condition, context) }
    }

    private fun translateOrCondition(
            leftExpression: JsExpression,
            condition: KtWhenCondition,
            context: TranslationContext
    ): JsExpression {
        konst rightContext = context.innerBlock()
        konst rightExpression = translateCondition(condition, rightContext)
        context.moveVarsFrom(rightContext)
        return if (rightContext.currentBlockIsEmpty()) {
            JsBinaryOperation(JsBinaryOperator.OR, leftExpression, rightExpression)
        }
        else {
            assert(rightExpression is JsNameRef) { "expected JsNameRef, but: " + rightExpression }
            konst result = rightExpression as JsNameRef
            konst ifStatement = JsAstUtils.newJsIf(leftExpression, JsAstUtils.assignment(result, JsBooleanLiteral(true)).makeStmt(),
                                                 rightContext.currentBlock)
            ifStatement.source = condition
            context.addStatementToCurrentBlock(ifStatement)
            result
        }
    }

    private fun translateCondition(condition: KtWhenCondition, context: TranslationContext): JsExpression {
        konst patternMatchExpression = translateWhenConditionToBooleanExpression(condition, context)
        return if (isNegated(condition)) not(patternMatchExpression) else patternMatchExpression
    }

    private fun translateWhenConditionToBooleanExpression(
            condition: KtWhenCondition,
            context: TranslationContext
    ): JsExpression = when (condition) {
        is KtWhenConditionIsPattern -> translateIsCondition(condition, context)
        is KtWhenConditionWithExpression -> translateExpressionCondition(condition, context)
        is KtWhenConditionInRange -> translateRangeCondition(condition, context)
        else -> error("Unsupported when condition " + condition.javaClass)
    }

    private fun translateIsCondition(conditionIsPattern: KtWhenConditionIsPattern, context: TranslationContext): JsExpression {
        konst expressionToMatch = expressionToMatch ?: error("An is-check is not allowed in when() without subject.")
        konst typeReference = conditionIsPattern.typeReference ?: error("An is-check must have a type reference.")

        konst result = Translation.patternTranslator(context).translateIsCheck(expressionToMatch, typeReference)
        return (result ?: JsBooleanLiteral(true)).source(conditionIsPattern)
    }

    private fun translateExpressionCondition(condition: KtWhenConditionWithExpression, context: TranslationContext): JsExpression {
        konst patternExpression = condition.expression ?: error("Expression pattern should have an expression.")

        konst patternTranslator = Translation.patternTranslator(context)
        return if (expressionToMatch == null) {
            patternTranslator.translateExpressionForExpressionPattern(patternExpression)
        } else {
            patternTranslator.translateExpressionPattern(subjectType!!, expressionToMatch, patternExpression)
        }
    }

    private fun translateRangeCondition(condition: KtWhenConditionInRange, context: TranslationContext): JsExpression {
        konst expressionToMatch = expressionToMatch ?: error("Range pattern is only available for " +
                                                           "'when (C) { in ... }'  expressions: ${condition.getTextWithLocation()}")

        konst subjectAliases = hashMapOf<KtExpression, JsExpression>()
        subjectAliases[whenExpression.subjectExpression!!] = expressionToMatch
        konst callContext = context.innerContextWithAliasesForExpressions(subjectAliases)
        konst negated = condition.operationReference.getReferencedNameElementType() === KtTokens.NOT_IN
        return InOperationTranslator(callContext, expressionToMatch, condition.rangeExpression!!, condition.operationReference,
                                     negated).translate().source(condition)
    }

    companion object {
        @JvmStatic
        fun translate(expression: KtWhenExpression, context: TranslationContext): JsNode = WhenTranslator(expression, context).translate()

        private fun isNegated(condition: KtWhenCondition): Boolean = (condition as? KtWhenConditionIsPattern)?.isNegated ?: false
    }
}
