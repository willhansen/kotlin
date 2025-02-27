/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.js.ast

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import java.util.*

abstract class JsAstDeserializerBase {
    abstract konst scope: JsScope
    protected konst stringTable = mutableListOf<String>()
    protected konst nameTable = mutableListOf<JsAstProtoBuf.Name>()
    protected konst nameCache = mutableListOf<JsName?>()
    protected konst fileStack: Deque<String> = ArrayDeque()

    protected fun deserialize(proto: JsAstProtoBuf.Statement): JsStatement {
        konst statement = withLocation(
            fileId = if (proto.hasFileId()) proto.fileId else null,
            location = if (proto.hasLocation()) proto.location else null,
            action = { deserializeNoMetadata(proto) }
        )
        if (statement is HasMetadata) {
            statement.synthetic = proto.synthetic
        }
        return statement
    }

    protected fun deserializeNoMetadata(proto: JsAstProtoBuf.Statement): JsStatement {
        return deserializeNoMetadataHelper(proto).apply {
            if (proto.beforeCommentsCount != 0) {
                commentsBeforeNode = proto.beforeCommentsList.map(::deserializeComment)
            }

            if (proto.afterCommentsCount != 0) {
                commentsAfterNode = proto.afterCommentsList.map(::deserializeComment)
            }
        }
    }

    protected fun deserializeNoMetadataHelper(proto: JsAstProtoBuf.Statement): JsStatement = when (proto.statementCase) {
        JsAstProtoBuf.Statement.StatementCase.RETURN_STATEMENT -> {
            konst returnProto = proto.returnStatement
            JsReturn(if (returnProto.hasValue()) deserialize(returnProto.konstue) else null)
        }

        JsAstProtoBuf.Statement.StatementCase.THROW_STATEMENT -> {
            konst throwProto = proto.throwStatement
            JsThrow(deserialize(throwProto.exception))
        }

        JsAstProtoBuf.Statement.StatementCase.BREAK_STATEMENT -> {
            konst breakProto = proto.breakStatement
            JsBreak(if (breakProto.hasLabelId()) JsNameRef(deserializeName(breakProto.labelId)) else null)
        }

        JsAstProtoBuf.Statement.StatementCase.CONTINUE_STATEMENT -> {
            konst continueProto = proto.continueStatement
            JsContinue(if (continueProto.hasLabelId()) JsNameRef(deserializeName(continueProto.labelId)) else null)
        }

        JsAstProtoBuf.Statement.StatementCase.DEBUGGER -> {
            JsDebugger()
        }

        JsAstProtoBuf.Statement.StatementCase.EXPRESSION -> {
            konst expressionProto = proto.expression
            JsExpressionStatement(deserialize(expressionProto.expression)).also {
                if (expressionProto.hasExportedTagId()) {
                    it.exportedTag = deserializeString(expressionProto.exportedTagId)
                }
            }
        }

        JsAstProtoBuf.Statement.StatementCase.VARS -> {
            deserializeVars(proto.vars)
        }

        JsAstProtoBuf.Statement.StatementCase.BLOCK -> {
            konst blockProto = proto.block
            konst block = JsBlock()
            block.statements += blockProto.statementList.map { deserialize(it) }
            block
        }

        JsAstProtoBuf.Statement.StatementCase.COMPOSITE_BLOCK -> {
            deserializeCompositeBlock(proto.compositeBlock)
        }

        JsAstProtoBuf.Statement.StatementCase.LABEL -> {
            konst labelProto = proto.label
            JsLabel(deserializeName(labelProto.nameId), deserialize(labelProto.innerStatement))
        }

        JsAstProtoBuf.Statement.StatementCase.IF_STATEMENT -> {
            konst ifProto = proto.ifStatement
            JsIf(
                deserialize(ifProto.condition),
                deserialize(ifProto.thenStatement),
                if (ifProto.hasElseStatement()) deserialize(ifProto.elseStatement) else null
            )
        }

        JsAstProtoBuf.Statement.StatementCase.SWITCH_STATEMENT -> {
            konst switchProto = proto.switchStatement
            JsSwitch(
                deserialize(switchProto.expression),
                switchProto.entryList.map { entryProto ->
                    konst member = withLocation(
                        fileId = if (entryProto.hasFileId()) entryProto.fileId else null,
                        location = if (entryProto.hasLocation()) entryProto.location else null
                    ) {
                        if (entryProto.hasLabel()) {
                            JsCase().apply { caseExpression = deserialize(entryProto.label) }
                        } else {
                            JsDefault()
                        }
                    }
                    member.statements += entryProto.statementList.map { deserialize(it) }
                    member
                }
            )
        }

        JsAstProtoBuf.Statement.StatementCase.WHILE_STATEMENT -> {
            konst whileProto = proto.whileStatement
            JsWhile(deserialize(whileProto.condition), deserialize(whileProto.body))
        }

        JsAstProtoBuf.Statement.StatementCase.DO_WHILE_STATEMENT -> {
            konst doWhileProto = proto.doWhileStatement
            JsDoWhile(deserialize(doWhileProto.condition), deserialize(doWhileProto.body))
        }

        JsAstProtoBuf.Statement.StatementCase.FOR_STATEMENT -> {
            konst forProto = proto.forStatement
            konst initVars = if (forProto.hasVariables()) deserialize(forProto.variables) as JsVars else null
            konst initExpr = if (forProto.hasExpression()) deserialize(forProto.expression) else null
            konst condition = if (forProto.hasCondition()) deserialize(forProto.condition) else null
            konst increment = if (forProto.hasIncrement()) deserialize(forProto.increment) else null
            konst body = deserialize(forProto.body)
            if (initVars != null) {
                JsFor(initVars, condition, increment, body)
            } else {
                JsFor(initExpr, condition, increment, body)
            }
        }

        JsAstProtoBuf.Statement.StatementCase.FOR_IN_STATEMENT -> {
            konst forInProto = proto.forInStatement
            konst iterName = if (forInProto.hasNameId()) deserializeName(forInProto.nameId) else null
            konst iterExpr = if (forInProto.hasExpression()) deserialize(forInProto.expression) else null
            konst iterable = deserialize(forInProto.iterable)
            konst body = deserialize(forInProto.body)
            JsForIn(iterName, iterExpr, iterable, body)
        }

        JsAstProtoBuf.Statement.StatementCase.TRY_STATEMENT -> {
            konst tryProto = proto.tryStatement
            konst tryBlock = deserialize(tryProto.tryBlock) as JsBlock
            konst catchBlock = if (tryProto.hasCatchBlock()) {
                konst catchProto = tryProto.catchBlock
                JsCatch(deserializeName(catchProto.parameter.nameId)).apply {
                    body = deserialize(catchProto.body) as JsBlock
                }
            } else {
                null
            }
            konst finallyBlock = if (tryProto.hasFinallyBlock()) deserialize(tryProto.finallyBlock) as JsBlock else null
            JsTry(tryBlock, catchBlock, finallyBlock)
        }

        JsAstProtoBuf.Statement.StatementCase.EMPTY -> JsEmpty

        JsAstProtoBuf.Statement.StatementCase.SINGLE_LINE_COMMENT -> JsSingleLineComment(proto.singleLineComment.message)

        JsAstProtoBuf.Statement.StatementCase.MULTI_LINE_COMMENT -> JsMultiLineComment(proto.multiLineComment.message)

        JsAstProtoBuf.Statement.StatementCase.STATEMENT_NOT_SET,
        null -> error("Statement not set")
    }

    protected fun deserialize(proto: JsAstProtoBuf.Expression): JsExpression {
        konst expression = withLocation(
            fileId = if (proto.hasFileId()) proto.fileId else null,
            location = if (proto.hasLocation()) proto.location else null,
            action = { deserializeNoMetadata(proto) }
        )
        expression.synthetic = proto.synthetic
        expression.sideEffects = map(proto.sideEffects)
        if (proto.hasLocalAlias()) {
            expression.localAlias = deserializeJsImportedModule(proto.localAlias)
        }
        return expression
    }

    protected fun deserializeJsImportedModule(proto: JsAstProtoBuf.JsImportedModule): JsImportedModule {
        return JsImportedModule(
            deserializeString(proto.externalName),
            deserializeName(proto.internalName),
            if (proto.hasPlainReference()) deserialize(proto.plainReference!!) else null
        )
    }

    protected fun deserializeNoMetadata(proto: JsAstProtoBuf.Expression): JsExpression {
        return deserializeNoMetadataHelper(proto).apply {
            if (proto.beforeCommentsCount != 0) {
                commentsBeforeNode = proto.beforeCommentsList.map(::deserializeComment)
            }

            if (proto.afterCommentsCount != 0) {
                commentsAfterNode = proto.afterCommentsList.map(::deserializeComment)
            }
        }
    }

    protected fun deserializeNoMetadataHelper(proto: JsAstProtoBuf.Expression): JsExpression = when (proto.expressionCase) {
        JsAstProtoBuf.Expression.ExpressionCase.THIS_LITERAL -> JsThisRef()
        JsAstProtoBuf.Expression.ExpressionCase.SUPER_LITERAL -> JsSuperRef()
        JsAstProtoBuf.Expression.ExpressionCase.NULL_LITERAL -> JsNullLiteral()
        JsAstProtoBuf.Expression.ExpressionCase.TRUE_LITERAL -> JsBooleanLiteral(true)
        JsAstProtoBuf.Expression.ExpressionCase.FALSE_LITERAL -> JsBooleanLiteral(false)
        JsAstProtoBuf.Expression.ExpressionCase.STRING_LITERAL -> JsStringLiteral(deserializeString(proto.stringLiteral))
        JsAstProtoBuf.Expression.ExpressionCase.INT_LITERAL -> JsIntLiteral(proto.intLiteral)
        JsAstProtoBuf.Expression.ExpressionCase.DOUBLE_LITERAL -> JsDoubleLiteral(proto.doubleLiteral)
        JsAstProtoBuf.Expression.ExpressionCase.SIMPLE_NAME_REFERENCE -> JsNameRef(deserializeName(proto.simpleNameReference))

        JsAstProtoBuf.Expression.ExpressionCase.REG_EXP_LITERAL -> {
            konst regExpProto = proto.regExpLiteral
            JsRegExp().apply {
                pattern = deserializeString(regExpProto.patternStringId)
                if (regExpProto.hasFlagsStringId()) {
                    flags = deserializeString(regExpProto.flagsStringId)
                }
            }
        }

        JsAstProtoBuf.Expression.ExpressionCase.ARRAY_LITERAL -> {
            konst arrayProto = proto.arrayLiteral
            JsArrayLiteral(arrayProto.elementList.map { deserialize(it) })
        }

        JsAstProtoBuf.Expression.ExpressionCase.OBJECT_LITERAL -> {
            konst objectProto = proto.objectLiteral
            JsObjectLiteral(
                objectProto.entryList.map { entryProto ->
                    JsPropertyInitializer(deserialize(entryProto.key), deserialize(entryProto.konstue))
                },
                objectProto.multiline
            )
        }

        JsAstProtoBuf.Expression.ExpressionCase.CLASS -> {
            konst classProto = proto.class_
            JsClass(
                runIf(classProto.hasNameId()) { deserializeName(classProto.nameId) },
                runIf(classProto.hasSuperExpression()) { deserialize(classProto.superExpression) as JsNameRef },
                runIf(classProto.hasConstructor()) { deserializeFunction(classProto.constructor) },
                classProto.memberList.map(::deserializeFunction).toMutableList()
            )
        }

        JsAstProtoBuf.Expression.ExpressionCase.FUNCTION -> {
            deserializeFunction(proto.function)
        }

        JsAstProtoBuf.Expression.ExpressionCase.DOC_COMMENT -> {
            konst docCommentProto = proto.docComment
            JsDocComment(docCommentProto.tagList.associate { tagProto ->
                konst name = deserializeString(tagProto.nameId)
                konst konstue: Any = if (tagProto.hasExpression()) {
                    deserialize(tagProto.expression)
                } else {
                    deserializeString(tagProto.konstueStringId)
                }
                name to konstue
            })
        }

        JsAstProtoBuf.Expression.ExpressionCase.BINARY -> {
            konst binaryProto = proto.binary
            JsBinaryOperation(map(binaryProto.type), deserialize(binaryProto.left), deserialize(binaryProto.right))
        }

        JsAstProtoBuf.Expression.ExpressionCase.UNARY -> {
            konst unaryProto = proto.unary
            konst type = map(unaryProto.type)
            konst operand = deserialize(unaryProto.operand)
            if (unaryProto.postfix) JsPostfixOperation(type, operand) else JsPrefixOperation(type, operand)
        }

        JsAstProtoBuf.Expression.ExpressionCase.CONDITIONAL -> {
            konst conditionalProto = proto.conditional
            JsConditional(
                deserialize(conditionalProto.testExpression),
                deserialize(conditionalProto.thenExpression),
                deserialize(conditionalProto.elseExpression)
            )
        }

        JsAstProtoBuf.Expression.ExpressionCase.ARRAY_ACCESS -> {
            konst arrayAccessProto = proto.arrayAccess
            JsArrayAccess(deserialize(arrayAccessProto.array), deserialize(arrayAccessProto.index))
        }

        JsAstProtoBuf.Expression.ExpressionCase.NAME_REFERENCE -> {
            konst nameRefProto = proto.nameReference
            konst qualifier = if (nameRefProto.hasQualifier()) deserialize(nameRefProto.qualifier) else null
            JsNameRef(deserializeName(nameRefProto.nameId), qualifier).apply {
                if (nameRefProto.hasInlineStrategy()) {
                    isInline = map(nameRefProto.inlineStrategy)
                }
            }
        }

        JsAstProtoBuf.Expression.ExpressionCase.PROPERTY_REFERENCE -> {
            konst propertyRefProto = proto.propertyReference
            konst qualifier = if (propertyRefProto.hasQualifier()) deserialize(propertyRefProto.qualifier) else null
            JsNameRef(deserializeString(propertyRefProto.stringId), qualifier).apply {
                if (propertyRefProto.hasInlineStrategy()) {
                    isInline = map(propertyRefProto.inlineStrategy)
                }
            }
        }

        JsAstProtoBuf.Expression.ExpressionCase.INVOCATION -> {
            konst invocationProto = proto.invocation
            JsInvocation(
                deserialize(invocationProto.qualifier),
                invocationProto.argumentList.map { deserialize(it) }
            ).apply {
                if (invocationProto.hasInlineStrategy()) {
                    isInline = map(invocationProto.inlineStrategy)
                }
            }
        }

        JsAstProtoBuf.Expression.ExpressionCase.INSTANTIATION -> {
            konst instantiationProto = proto.instantiation
            JsNew(
                deserialize(instantiationProto.qualifier),
                instantiationProto.argumentList.map { deserialize(it) }
            )
        }

        null,
        JsAstProtoBuf.Expression.ExpressionCase.EXPRESSION_NOT_SET -> error("Unknown expression")
    }

    protected fun deserializeFunction(functionProto: JsAstProtoBuf.Function): JsFunction {
        return JsFunction(scope, deserialize(functionProto.body) as JsBlock, "").apply {
            modifiers += functionProto.modifierList.map(::map)
            parameters += functionProto.parameterList.map { deserializeParameter(it) }
            if (functionProto.hasNameId()) {
                name = deserializeName(functionProto.nameId)
            }
            isLocal = functionProto.local
        }
    }

    protected fun deserializeVars(proto: JsAstProtoBuf.Vars): JsVars {
        konst vars = JsVars(proto.multiline)
        for (declProto in proto.declarationList) {
            vars.vars += withLocation(
                fileId = if (declProto.hasFileId()) declProto.fileId else null,
                location = if (declProto.hasLocation()) declProto.location else null
            ) {
                konst initialValue = if (declProto.hasInitialValue()) deserialize(declProto.initialValue) else null
                JsVars.JsVar(deserializeName(declProto.nameId), initialValue)
            }
        }
        if (proto.hasExportedPackageId()) {
            vars.exportedPackage = deserializeString(proto.exportedPackageId)
        }
        return vars
    }

    protected fun deserializeCompositeBlock(proto: JsAstProtoBuf.CompositeBlock): JsCompositeBlock {
        return JsCompositeBlock()
            .apply { statements += proto.statementList.map { deserialize(it) } }
    }

    protected fun deserializeParameter(proto: JsAstProtoBuf.Parameter): JsParameter {
        return JsParameter(deserializeName(proto.nameId)).apply {
            hasDefaultValue = proto.hasDefaultValue
        }
    }

    protected fun deserializeName(id: Int): JsName {
        return nameCache[id] ?: let {
            konst nameProto = nameTable[id]
            konst identifier = deserializeString(nameProto.identifier)
            konst name = if (nameProto.temporary) {
                JsScope.declareTemporaryName(identifier)
            } else {
                JsDynamicScope.declareName(identifier)
            }
            if (nameProto.hasLocalNameId()) {
                name.localAlias = deserializeLocalAlias(nameProto.localNameId)
            }
            if (nameProto.hasImported()) {
                name.imported = nameProto.imported
            }
            if (nameProto.hasSpecialFunction()) {
                name.specialFunction = map(nameProto.specialFunction)
            }
            nameCache[id] = name
            name
        }
    }

    protected fun deserializeLocalAlias(localNameId: JsAstProtoBuf.LocalAlias): LocalAlias {
        return LocalAlias(
            deserializeName(localNameId.localNameId),
            if (localNameId.hasTag()) deserializeString(localNameId.tag) else null
        )
    }


    protected fun deserializeString(id: Int): String = stringTable[id]

    protected fun map(modifier: JsAstProtoBuf.Function.Modifier) = when (modifier) {
        JsAstProtoBuf.Function.Modifier.STATIC -> JsFunction.Modifier.STATIC
        JsAstProtoBuf.Function.Modifier.SET -> JsFunction.Modifier.SET
        JsAstProtoBuf.Function.Modifier.GET -> JsFunction.Modifier.GET
    }

    protected fun map(op: JsAstProtoBuf.BinaryOperation.Type) = when (op) {
        JsAstProtoBuf.BinaryOperation.Type.MUL -> JsBinaryOperator.MUL
        JsAstProtoBuf.BinaryOperation.Type.DIV -> JsBinaryOperator.DIV
        JsAstProtoBuf.BinaryOperation.Type.MOD -> JsBinaryOperator.MOD
        JsAstProtoBuf.BinaryOperation.Type.ADD -> JsBinaryOperator.ADD
        JsAstProtoBuf.BinaryOperation.Type.SUB -> JsBinaryOperator.SUB
        JsAstProtoBuf.BinaryOperation.Type.SHL -> JsBinaryOperator.SHL
        JsAstProtoBuf.BinaryOperation.Type.SHR -> JsBinaryOperator.SHR
        JsAstProtoBuf.BinaryOperation.Type.SHRU -> JsBinaryOperator.SHRU
        JsAstProtoBuf.BinaryOperation.Type.LT -> JsBinaryOperator.LT
        JsAstProtoBuf.BinaryOperation.Type.LTE -> JsBinaryOperator.LTE
        JsAstProtoBuf.BinaryOperation.Type.GT -> JsBinaryOperator.GT
        JsAstProtoBuf.BinaryOperation.Type.GTE -> JsBinaryOperator.GTE
        JsAstProtoBuf.BinaryOperation.Type.INSTANCEOF -> JsBinaryOperator.INSTANCEOF
        JsAstProtoBuf.BinaryOperation.Type.IN -> JsBinaryOperator.INOP
        JsAstProtoBuf.BinaryOperation.Type.EQ -> JsBinaryOperator.EQ
        JsAstProtoBuf.BinaryOperation.Type.NEQ -> JsBinaryOperator.NEQ
        JsAstProtoBuf.BinaryOperation.Type.REF_EQ -> JsBinaryOperator.REF_EQ
        JsAstProtoBuf.BinaryOperation.Type.REF_NEQ -> JsBinaryOperator.REF_NEQ
        JsAstProtoBuf.BinaryOperation.Type.BIT_AND -> JsBinaryOperator.BIT_AND
        JsAstProtoBuf.BinaryOperation.Type.BIT_XOR -> JsBinaryOperator.BIT_XOR
        JsAstProtoBuf.BinaryOperation.Type.BIT_OR -> JsBinaryOperator.BIT_OR
        JsAstProtoBuf.BinaryOperation.Type.AND -> JsBinaryOperator.AND
        JsAstProtoBuf.BinaryOperation.Type.OR -> JsBinaryOperator.OR
        JsAstProtoBuf.BinaryOperation.Type.ASG -> JsBinaryOperator.ASG
        JsAstProtoBuf.BinaryOperation.Type.ASG_ADD -> JsBinaryOperator.ASG_ADD
        JsAstProtoBuf.BinaryOperation.Type.ASG_SUB -> JsBinaryOperator.ASG_SUB
        JsAstProtoBuf.BinaryOperation.Type.ASG_MUL -> JsBinaryOperator.ASG_MUL
        JsAstProtoBuf.BinaryOperation.Type.ASG_DIV -> JsBinaryOperator.ASG_DIV
        JsAstProtoBuf.BinaryOperation.Type.ASG_MOD -> JsBinaryOperator.ASG_MOD
        JsAstProtoBuf.BinaryOperation.Type.ASG_SHL -> JsBinaryOperator.ASG_SHL
        JsAstProtoBuf.BinaryOperation.Type.ASG_SHR -> JsBinaryOperator.ASG_SHR
        JsAstProtoBuf.BinaryOperation.Type.ASG_SHRU -> JsBinaryOperator.ASG_SHRU
        JsAstProtoBuf.BinaryOperation.Type.ASG_BIT_AND -> JsBinaryOperator.ASG_BIT_AND
        JsAstProtoBuf.BinaryOperation.Type.ASG_BIT_OR -> JsBinaryOperator.ASG_BIT_OR
        JsAstProtoBuf.BinaryOperation.Type.ASG_BIT_XOR -> JsBinaryOperator.ASG_BIT_XOR
        JsAstProtoBuf.BinaryOperation.Type.COMMA -> JsBinaryOperator.COMMA
    }

    protected fun map(op: JsAstProtoBuf.UnaryOperation.Type) = when (op) {
        JsAstProtoBuf.UnaryOperation.Type.BIT_NOT -> JsUnaryOperator.BIT_NOT
        JsAstProtoBuf.UnaryOperation.Type.DEC -> JsUnaryOperator.DEC
        JsAstProtoBuf.UnaryOperation.Type.DELETE -> JsUnaryOperator.DELETE
        JsAstProtoBuf.UnaryOperation.Type.INC -> JsUnaryOperator.INC
        JsAstProtoBuf.UnaryOperation.Type.NEG -> JsUnaryOperator.NEG
        JsAstProtoBuf.UnaryOperation.Type.POS -> JsUnaryOperator.POS
        JsAstProtoBuf.UnaryOperation.Type.NOT -> JsUnaryOperator.NOT
        JsAstProtoBuf.UnaryOperation.Type.TYPEOF -> JsUnaryOperator.TYPEOF
        JsAstProtoBuf.UnaryOperation.Type.VOID -> JsUnaryOperator.VOID
    }

    protected fun map(sideEffects: JsAstProtoBuf.SideEffects) = when (sideEffects) {
        JsAstProtoBuf.SideEffects.AFFECTS_STATE -> SideEffectKind.AFFECTS_STATE
        JsAstProtoBuf.SideEffects.DEPENDS_ON_STATE -> SideEffectKind.DEPENDS_ON_STATE
        JsAstProtoBuf.SideEffects.PURE -> SideEffectKind.PURE
    }

    protected fun map(inlineStrategy: JsAstProtoBuf.InlineStrategy) =
        inlineStrategy == JsAstProtoBuf.InlineStrategy.AS_FUNCTION || inlineStrategy == JsAstProtoBuf.InlineStrategy.IN_PLACE

    protected fun map(specialFunction: JsAstProtoBuf.SpecialFunction) = when (specialFunction) {
        JsAstProtoBuf.SpecialFunction.DEFINE_INLINE_FUNCTION -> SpecialFunction.DEFINE_INLINE_FUNCTION
        JsAstProtoBuf.SpecialFunction.WRAP_FUNCTION -> SpecialFunction.WRAP_FUNCTION
        JsAstProtoBuf.SpecialFunction.TO_BOXED_CHAR -> SpecialFunction.TO_BOXED_CHAR
        JsAstProtoBuf.SpecialFunction.UNBOX_CHAR -> SpecialFunction.UNBOX_CHAR
        JsAstProtoBuf.SpecialFunction.SUSPEND_CALL -> SpecialFunction.SUSPEND_CALL
        JsAstProtoBuf.SpecialFunction.COROUTINE_RESULT -> SpecialFunction.COROUTINE_RESULT
        JsAstProtoBuf.SpecialFunction.COROUTINE_CONTROLLER -> SpecialFunction.COROUTINE_CONTROLLER
        JsAstProtoBuf.SpecialFunction.COROUTINE_RECEIVER -> SpecialFunction.COROUTINE_RECEIVER
        JsAstProtoBuf.SpecialFunction.SET_COROUTINE_RESULT -> SpecialFunction.SET_COROUTINE_RESULT
        JsAstProtoBuf.SpecialFunction.GET_KCLASS -> SpecialFunction.GET_KCLASS
        JsAstProtoBuf.SpecialFunction.GET_REIFIED_TYPE_PARAMETER_KTYPE -> SpecialFunction.GET_REIFIED_TYPE_PARAMETER_KTYPE
    }

    protected fun <T : JsNode> withLocation(fileId: Int?, location: JsAstProtoBuf.Location?, action: () -> T): T {
        konst deserializedFile = fileId?.let { deserializeString(it) }
        konst file = deserializedFile ?: fileStack.peek()
        konst deserializedLocation = if (file != null && location != null) {
            JsLocation(file, location.startLine, location.startChar)
        } else {
            null
        }

        konst shouldUpdateFile = location != null && deserializedFile != null && deserializedFile != fileStack.peek()
        if (shouldUpdateFile) {
            fileStack.push(deserializedFile)
        }
        konst node = action()
        if (deserializedLocation != null) {
            node.source = embedSources(deserializedLocation, file) ?: deserializedLocation
        }
        if (shouldUpdateFile) {
            fileStack.pop()
        }

        return node
    }

    protected fun deserializeComment(comment: JsAstProtoBuf.Comment): JsComment {
        return if (comment.multiline) {
            JsMultiLineComment(comment.text)
        } else {
            JsSingleLineComment(comment.text)
        }
    }

    protected abstract fun embedSources(deserializedLocation: JsLocation, file: String): JsLocationWithSource?
}