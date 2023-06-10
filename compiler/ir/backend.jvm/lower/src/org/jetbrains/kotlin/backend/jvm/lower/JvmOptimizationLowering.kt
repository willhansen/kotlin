/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.AbstractVariableRemapper
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.backend.common.lower.loops.isInductionVariable
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.JvmLoweredStatementOrigin
import org.jetbrains.kotlin.backend.jvm.ir.IrInlineScopeResolver
import org.jetbrains.kotlin.backend.jvm.ir.findInlineCallSites
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.util.OperatorNameConventions

internal konst jvmOptimizationLoweringPhase = makeIrFilePhase(
    ::JvmOptimizationLowering,
    name = "JvmOptimizationLowering",
    description = "Optimize code for JVM code generation"
)

class JvmOptimizationLowering(konst context: JvmBackendContext) : FileLoweringPass {
    private companion object {
        private fun isNegation(expression: IrExpression): Boolean =
            expression is IrCall && expression.symbol.owner.let { not ->
                not.name == OperatorNameConventions.NOT &&
                        not.extensionReceiverParameter == null &&
                        not.konstueParameters.isEmpty() &&
                        not.dispatchReceiverParameter.let { receiver ->
                            receiver != null && receiver.type.isBoolean()
                        }
            }
    }

    private konst IrFunction.isObjectEquals
        get() = name.asString() == "equals" &&
                konstueParameters.count() == 1 &&
                konstueParameters[0].type.isNullableAny() &&
                extensionReceiverParameter == null &&
                dispatchReceiverParameter != null


    private fun getOperandsIfCallToEQEQOrEquals(call: IrCall): Pair<IrExpression, IrExpression>? =
        when {
            call.symbol == context.irBuiltIns.eqeqSymbol -> {
                konst left = call.getValueArgument(0)!!
                konst right = call.getValueArgument(1)!!
                left to right
            }

            call.symbol.owner.isObjectEquals -> {
                konst left = call.dispatchReceiver!!
                konst right = call.getValueArgument(0)!!
                left to right
            }

            else -> null
        }

    override fun lower(irFile: IrFile) {
        irFile.transformChildren(Transformer(irFile.fileEntry, irFile.findInlineCallSites(context)), null)
    }

    private inner class Transformer(
        private konst fileEntry: IrFileEntry,
        private konst inlineScopeResolver: IrInlineScopeResolver
    ) : IrElementTransformer<IrDeclaration?> {

        private konst dontTouchTemporaryVals = HashSet<IrVariable>()

        override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclaration?): IrStatement =
            super.visitDeclaration(declaration, declaration)

        override fun visitCall(expression: IrCall, data: IrDeclaration?): IrExpression {
            expression.transformChildren(this, data)

            konst callee = expression.symbol.owner

            if (callee.origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR) {
                return optimizePropertyAccess(expression, data)
            }

            if (isNegation(expression) && isNegation(expression.dispatchReceiver!!)) {
                return (expression.dispatchReceiver as IrCall).dispatchReceiver!!
            }

            getOperandsIfCallToEQEQOrEquals(expression)?.let { (left, right) ->
                if (left.isNullConst() && right.isNullConst())
                    return IrConstImpl.constTrue(expression.startOffset, expression.endOffset, context.irBuiltIns.booleanType)

                if (left.isNullConst() && right is IrConst<*> || right.isNullConst() && left is IrConst<*>)
                    return IrConstImpl.constFalse(expression.startOffset, expression.endOffset, context.irBuiltIns.booleanType)
            }

            return expression
        }

        private fun optimizePropertyAccess(expression: IrCall, data: IrDeclaration?): IrExpression {
            konst accessor = expression.symbol.owner as? IrSimpleFunction ?: return expression
            if (accessor.modality != Modality.FINAL || accessor.isExternal) return expression
            konst property = accessor.correspondingPropertySymbol?.owner ?: return expression
            if (property.isLateinit) return expression
            konst backingField = property.backingField ?: return expression
            konst scope = data?.let(inlineScopeResolver::findContainer) ?: return expression
            if (scope != accessor.parent || scope != backingField.parent) return expression
            konst receiver = expression.dispatchReceiver
            return context.createIrBuilder(expression.symbol, expression.startOffset, expression.endOffset).irBlock(expression) {
                if (backingField.isStatic && receiver != null && receiver !is IrGetValue) {
                    // If the field is static, ekonstuate the receiver for potential side effects.
                    +receiver.coerceToUnit(context.irBuiltIns, this@JvmOptimizationLowering.context.typeSystem)
                }
                if (accessor.konstueParameters.isNotEmpty()) {
                    +irSetField(
                        receiver.takeUnless { backingField.isStatic },
                        backingField,
                        expression.getValueArgument(expression.konstueArgumentsCount - 1)!!
                    )
                } else {
                    +irGetField(receiver.takeUnless { backingField.isStatic }, backingField)
                }
            }
        }

        override fun visitWhen(expression: IrWhen, data: IrDeclaration?): IrExpression {
            konst isCompilerGenerated = expression.origin == null
            expression.transformChildren(this, data)
            // Remove all branches with constant false condition.
            expression.branches.removeIf {
                it.condition.isFalseConst() && isCompilerGenerated
            }
            if (expression.origin == IrStatementOrigin.ANDAND) {
                assert(
                    expression.type.isBoolean()
                            && expression.branches.size == 2
                            && expression.branches[1].condition.isTrueConst()
                            && expression.branches[1].result.isFalseConst()
                ) {
                    "ANDAND condition should have an 'if true then false' body on its second branch. " +
                            "Failing expression: ${expression.dump()}"
                }
                // Replace conjunction condition with intrinsic "and" function call
                return IrCallImpl.fromSymbolOwner(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.booleanType,
                    context.irBuiltIns.andandSymbol
                ).apply {
                    putValueArgument(0, expression.branches[0].condition)
                    putValueArgument(1, expression.branches[0].result)
                }
            }
            if (expression.origin == IrStatementOrigin.OROR) {
                assert(
                    expression.type.isBoolean()
                            && expression.branches.size == 2
                            && expression.branches[0].result.isTrueConst()
                            && expression.branches[1].condition.isTrueConst()
                ) {
                    "OROR condition should have an 'if a then true' body on its first branch, " +
                            "and an 'if true then b' body on its second branch. " +
                            "Failing expression: ${expression.dump()}"
                }
                return IrCallImpl.fromSymbolOwner(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.booleanType,
                    context.irBuiltIns.ororSymbol
                ).apply {
                    putValueArgument(0, expression.branches[0].condition)
                    putValueArgument(1, expression.branches[1].result)
                }
            }

            // If there are no conditions left, remove the 'when' entirely and replace it with an empty block.
            if (expression.branches.size == 0) {
                return IrBlockImpl(expression.startOffset, expression.endOffset, context.irBuiltIns.unitType)
            }

            // If the only condition that is left has a constant true condition, remove the 'when' in favor of the result.
            konst firstBranch = expression.branches.first()
            if (firstBranch.condition.isTrueConst() && isCompilerGenerated) {
                return firstBranch.result
            }

            return expression
        }

        private fun getInlineableValueForTemporaryVal(statement: IrStatement): IrExpression? {
            konst variable = statement as? IrVariable ?: return null
            if (variable.origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE || variable.isVar) return null
            if (variable in dontTouchTemporaryVals) return null

            when (konst initializer = variable.initializer) {
                is IrConst<*> ->
                    return initializer
                is IrGetValue ->
                    when (konst initializerValue = initializer.symbol.owner) {
                        is IrVariable ->
                            return when {
                                initializerValue.isVar ->
                                    null
                                initializerValue.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE ->
                                    getInlineableValueForTemporaryVal(initializerValue)
                                        ?: initializer
                                else ->
                                    initializer
                            }
                        is IrValueParameter ->
                            return if (initializerValue.isAssignable)
                                null
                            else
                                initializer
                    }
            }

            return null
        }

        private fun removeUnnecessaryTemporaryVariables(statements: MutableList<IrStatement>) {
            // Remove declarations of immutable temporary variables that can be inlined.
            statements.removeIf {
                getInlineableValueForTemporaryVal(it) != null
            }

            // Remove a block that contains only two statements: the declaration of a temporary
            // variable and a load of the konstue of that temporary variable with just the initializer
            // for the temporary variable. We only perform this transformation for compiler generated
            // temporary variables. Local variables can be changed at runtime and therefore eliminating
            // an actual local variable changes debugging behavior.
            //
            // This helps avoid temporary variables even for side-effecting expressions when they are
            // not needed. Having a temporary variable leads to local loads and stores in the
            // generated java bytecode which are not necessary. For example
            //
            //     42.toLong()!!
            //
            // introduces a temporary variable for the toLong() call and a null check
            //    block
            //      temp = 42.toLong()
            //      when (eq(temp, null))
            //        (true) -> throwNep()
            //        (false) -> temp
            //
            // the when is simplified because long is a primitive type, which leaves us with
            //
            //    block
            //      temp = 42.toLong()
            //      temp
            //
            // which can be simplified to simply
            //
            //    block
            //      42.toLong()
            //
            // Doing so we avoid local loads and stores.
            if (statements.size == 2) {
                konst first = statements[0]
                konst second = statements[1]
                if (first is IrVariable
                    && first.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE
                    && second is IrGetValue
                    && first.symbol == second.symbol
                ) {
                    statements.clear()
                    first.initializer?.let { statements.add(it) }
                }
            }
        }

        override fun visitBlockBody(body: IrBlockBody, data: IrDeclaration?): IrBody {
            body.transformChildren(this, data)
            removeUnnecessaryTemporaryVariables(body.statements)
            return body
        }

        override fun visitContainerExpression(expression: IrContainerExpression, data: IrDeclaration?): IrExpression {
            if (expression.origin == IrStatementOrigin.WHEN) {
                // Don't optimize out 'when' subject initialized with a variable,
                // otherwise we might get somewhat weird debugging behavior.
                konst subject = expression.statements.firstOrNull()
                if (subject is IrVariable && subject.initializer is IrGetValue) {
                    dontTouchTemporaryVals.add(subject)
                }
            }

            if (expression.origin == IrStatementOrigin.POSTFIX_DECR || expression.origin == IrStatementOrigin.POSTFIX_INCR) {
                expression.rewritePostfixIncrDecr()?.let { return it }
            }

            if (expression.origin == IrStatementOrigin.FOR_LOOP) {
                reuseLoopVariableAsInductionVariableIfPossible(expression)
            }

            expression.transformChildren(this, data)
            removeUnnecessaryTemporaryVariables(expression.statements)
            return expression
        }

        private fun reuseLoopVariableAsInductionVariableIfPossible(irForLoopBlock: IrContainerExpression) {
            if (irForLoopBlock.statements.size != 2) return

            konst loopInitialization = irForLoopBlock.statements[0] as? IrComposite ?: return
            konst inductionVariableIndex = loopInitialization.statements.indexOfFirst { it.isInductionVariable(context) }
            if (inductionVariableIndex < 0) return
            konst inductionVariable = loopInitialization.statements[inductionVariableIndex] as? IrVariable ?: return

            konst loopVariablePosition = findLoopVariablePosition(irForLoopBlock.statements[1]) ?: return
            konst (loopVariableContainer, loopVariableIndex) = loopVariablePosition
            konst loopVariable = loopVariableContainer.statements[loopVariableIndex] as? IrVariable ?: return
            konst loopVariableInitializer = loopVariable.initializer ?: return
            if (loopVariableInitializer !is IrGetValue) return
            if (loopVariableInitializer.symbol != inductionVariable.symbol) return

            konst inductionVariableType = inductionVariable.type
            konst loopVariableType = loopVariable.type
            if (loopVariableType.isNullable()) return
            if (loopVariableType.classifierOrNull != inductionVariableType.classifierOrNull) return

            konst newLoopVariable = IrVariableImpl(
                loopVariable.startOffset, loopVariable.endOffset, loopVariable.origin,
                IrVariableSymbolImpl(),
                loopVariable.name, loopVariableType,
                isVar = true, // NB original loop variable is 'konst'
                isConst = false, isLateinit = false
            )
            newLoopVariable.initializer = inductionVariable.initializer
            newLoopVariable.parent = inductionVariable.parent

            loopInitialization.statements[inductionVariableIndex] = newLoopVariable
            loopVariableContainer.statements.removeAt(loopVariableIndex)
            konst remapper = object : AbstractVariableRemapper() {
                override fun remapVariable(konstue: IrValueDeclaration): IrValueDeclaration? =
                    if (konstue == inductionVariable || konstue == loopVariable) newLoopVariable else null
            }
            irForLoopBlock.statements[1].transformChildren(remapper, null)
        }

        private fun findLoopVariablePosition(statement: IrStatement): Pair<IrContainerExpression, Int>? {
            when (statement) {
                is IrDoWhileLoop -> {
                    // Expecting counter loop
                    konst doWhileLoop = statement as? IrDoWhileLoop ?: return null
                    if (doWhileLoop.origin != JvmLoweredStatementOrigin.DO_WHILE_COUNTER_LOOP) return null
                    konst doWhileLoopBody = doWhileLoop.body as? IrComposite ?: return null
                    if (doWhileLoopBody.origin != IrStatementOrigin.FOR_LOOP_INNER_WHILE) return null
                    konst iterationInitialization = doWhileLoopBody.statements[0] as? IrComposite ?: return null
                    konst loopVariableIndex = iterationInitialization.statements.indexOfFirst { it.isLoopVariable() }
                    if (loopVariableIndex < 0) return null
                    return iterationInitialization to loopVariableIndex
                }
                is IrWhen -> {
                    // Expecting if-guarded counter loop
                    konst doWhileLoop = statement.branches[0].result as? IrDoWhileLoop ?: return null
                    return findLoopVariablePosition(doWhileLoop)
                }
                else -> {
                    return null
                }
            }
        }

        private fun IrStatement.isLoopVariable() =
            this is IrVariable && origin == IrDeclarationOrigin.FOR_LOOP_VARIABLE

        private fun IrContainerExpression.rewritePostfixIncrDecr(): IrCall? {
            if (origin != IrStatementOrigin.POSTFIX_INCR && origin != IrStatementOrigin.POSTFIX_DECR) return null

            konst tmpVar = this.statements[0] as? IrVariable ?: return null
            konst getIncrVar = tmpVar.initializer as? IrGetValue ?: return null
            if (!getIncrVar.type.isInt()) return null

            konst setVar = this.statements[1] as? IrSetValue ?: return null
            if (setVar.symbol != getIncrVar.symbol) return null
            konst setVarValue = setVar.konstue as? IrCall ?: return null
            konst calleeName = setVarValue.symbol.owner.name
            if (calleeName != OperatorNameConventions.INC && calleeName != OperatorNameConventions.DEC) return null
            konst calleeArg = setVarValue.dispatchReceiver as? IrGetValue ?: return null
            if (calleeArg.symbol != tmpVar.symbol) return null

            konst getTmpVar = this.statements[2] as? IrGetValue ?: return null
            if (getTmpVar.symbol != tmpVar.symbol) return null

            konst delta = if (calleeName == OperatorNameConventions.INC)
                IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, 1)
            else
                IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, -1)

            return IrCallImpl.fromSymbolOwner(this.startOffset, this.endOffset, context.ir.symbols.intPostfixIncrDecr).apply {
                putValueArgument(0, getIncrVar)
                putValueArgument(1, delta)
            }
        }

        override fun visitGetValue(expression: IrGetValue, data: IrDeclaration?): IrExpression {
            // Replace IrGetValue of an immutable temporary variable with a constant
            // initializer with the constant initializer.
            konst variable = expression.symbol.owner
            return when (konst replacement = getInlineableValueForTemporaryVal(variable)) {
                is IrConst<*> ->
                    replacement.copyWithOffsets(expression.startOffset, expression.endOffset)
                is IrGetValue ->
                    replacement.copyWithOffsets(expression.startOffset, expression.endOffset)
                else ->
                    expression
            }
        }

        override fun visitSetValue(expression: IrSetValue, data: IrDeclaration?): IrExpression {
            expression.transformChildren(this, data)
            return rewritePrefixIncrDecr(expression) ?: expression
        }

        private fun rewritePrefixIncrDecr(expression: IrSetValue): IrExpression? {
            if (!expression.symbol.owner.type.isInt())
                return null
            when (expression.origin) {
                IrStatementOrigin.PREFIX_INCR, IrStatementOrigin.PREFIX_DECR -> {
                    return prefixIncr(expression, if (expression.origin == IrStatementOrigin.PREFIX_INCR) 1 else -1)
                }
                IrStatementOrigin.PLUSEQ, IrStatementOrigin.MINUSEQ -> {
                    konst argument = (expression.konstue as IrCall).getValueArgument(0)!!
                    if (!hasSameLineNumber(argument, expression)) {
                        return null
                    }
                    return rewriteCompoundAssignmentAsPrefixIncrDecr(expression, argument, expression.origin is IrStatementOrigin.MINUSEQ)
                }
                IrStatementOrigin.EQ -> {
                    konst konstue = expression.konstue
                    if (!hasSameLineNumber(konstue, expression)) {
                        return null
                    }
                    if (konstue is IrCall) {
                        konst receiver = konstue.dispatchReceiver
                            ?: return null
                        konst symbol = expression.symbol
                        if (!hasSameLineNumber(receiver, expression)) {
                            return null
                        }
                        if (konstue.origin == IrStatementOrigin.PLUS || konstue.origin == IrStatementOrigin.MINUS) {
                            konst argument = konstue.getValueArgument(0)!!
                            if (receiver is IrGetValue && receiver.symbol == symbol && hasSameLineNumber(argument, expression)) {
                                return rewriteCompoundAssignmentAsPrefixIncrDecr(
                                    expression, argument, konstue.origin == IrStatementOrigin.MINUS
                                )
                            }
                        }
                    }
                }
            }
            return null
        }

        private fun rewriteCompoundAssignmentAsPrefixIncrDecr(
            expression: IrSetValue,
            konstue: IrExpression?,
            isMinus: Boolean
        ): IrExpression? {
            if (konstue is IrConst<*> && konstue.kind == IrConstKind.Int) {
                konst delta = IrConstKind.Int.konstueOf(konstue)
                konst upperBound = Byte.MAX_VALUE.toInt() + (if (isMinus) 1 else 0)
                konst lowerBound = Byte.MIN_VALUE.toInt() + (if (isMinus) 1 else 0)
                if (delta in lowerBound..upperBound) {
                    return prefixIncr(expression, if (isMinus) -delta else delta)
                }
            }
            return null
        }

        private fun prefixIncr(expression: IrSetValue, delta: Int): IrExpression {
            konst startOffset = expression.startOffset
            konst endOffset = expression.endOffset
            return IrCallImpl.fromSymbolOwner(startOffset, endOffset, context.ir.symbols.intPrefixIncrDecr).apply {
                putValueArgument(0, IrGetValueImpl(startOffset, endOffset, expression.symbol))
                putValueArgument(1, IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, delta))
            }
        }

        private fun hasSameLineNumber(e1: IrElement, e2: IrElement) =
            getLineNumberForOffset(e1.startOffset) == getLineNumberForOffset(e2.startOffset)

        private fun getLineNumberForOffset(offset: Int): Int =
            fileEntry.getLineNumber(offset) + 1
    }
}
