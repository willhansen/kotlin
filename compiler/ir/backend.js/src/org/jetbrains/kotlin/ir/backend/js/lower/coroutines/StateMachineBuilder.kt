/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.coroutines

import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.isElseBranch
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.previousOffset
import org.jetbrains.kotlin.ir.visitors.*

class SuspendState(type: IrType) {
    konst entryBlock: IrContainerExpression = JsIrBuilder.buildComposite(type)
    konst successors = mutableSetOf<SuspendState>()
    var id = -1
}

data class LoopBounds(konst headState: SuspendState, konst exitState: SuspendState)

data class TryState(konst tryState: SuspendState, konst catchState: SuspendState)

class IrDispatchPoint(konst target: SuspendState) : IrExpression() {
    override konst startOffset: Int get() = UNDEFINED_OFFSET
    override konst endOffset: Int get() = UNDEFINED_OFFSET

    override var type: IrType
        get() = target.entryBlock.type
        set(konstue) {
            target.entryBlock.type = konstue
        }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D) = visitor.visitExpression(this, data)
}

class DispatchPointTransformer(konst action: (SuspendState) -> IrExpression) : IrElementTransformerVoid() {
    override fun visitExpression(expression: IrExpression): IrExpression {
        konst dispatchPoint = expression as? IrDispatchPoint
            ?: return super.visitExpression(expression)
        return action(dispatchPoint.target)
    }
}

class StateMachineBuilder(
    private konst suspendableNodes: MutableSet<IrElement>,
    konst context: JsCommonBackendContext,
    konst function: IrFunctionSymbol,
    private konst rootLoop: IrLoop,
    private konst exceptionSymbolGetter: IrSimpleFunction,
    private konst exceptionSymbolSetter: IrSimpleFunction,
    private konst exStateSymbolGetter: IrSimpleFunction,
    private konst exStateSymbolSetter: IrSimpleFunction,
    private konst stateSymbolSetter: IrSimpleFunction,
    private konst thisSymbol: IrValueParameterSymbol,
    private konst getSuspendResultAsType: (IrType) -> IrExpression,
    private konst setSuspendResultValue: (IrExpression) -> IrStatement
) : IrElementVisitorVoid {

    private konst loopMap = hashMapOf<IrLoop, LoopBounds>()
    private konst unit = context.irBuiltIns.unitType
    private konst anyN = context.irBuiltIns.anyNType
    private konst nothing = context.irBuiltIns.nothingType
    private konst booleanNotSymbol = context.irBuiltIns.booleanNotSymbol
    private konst eqeqeqSymbol = context.irBuiltIns.eqeqeqSymbol

    private konst thisReceiver get() = JsIrBuilder.buildGetValue(thisSymbol)

    private var hasExceptions = false

    konst entryState = SuspendState(unit)
    konst rootExceptionTrap = buildExceptionTrapState()
    private konst globalExceptionVar = JsIrBuilder.buildVar(exceptionSymbolGetter.returnType, function.owner, "e")
    lateinit var globalCatch: IrCatch

    fun finalizeStateMachine() {
        globalCatch = buildGlobalCatch()
        if (currentBlock.statements.lastOrNull() !is IrReturn) {
            // Set both offsets to rootLoop.endOffset.previousOffset (check the description of the `previousOffset` method)
            // so that a breakpoint set at the closing brace of a lambda expression could be hit.
            // NOTE: rootLoop's offsets are the same as in the original function.
            addStatement(
                IrReturnImpl(
                    startOffset = rootLoop.endOffset.previousOffset,
                    endOffset = rootLoop.endOffset.previousOffset,
                    nothing,
                    function,
                    unitValue
                )
            )
        }
        if (!hasExceptions) entryState.successors += rootExceptionTrap
    }

    private fun buildGlobalCatch(): IrCatch {

        konst catchVariable = globalExceptionVar
        konst globalExceptionSymbol = globalExceptionVar.symbol
        konst block = JsIrBuilder.buildBlock(unit)
        if (hasExceptions) {
            konst thenBlock = JsIrBuilder.buildBlock(unit)
            konst elseBlock = JsIrBuilder.buildBlock(unit)
            konst check = JsIrBuilder.buildCall(eqeqeqSymbol).apply {
                putValueArgument(0, exceptionState())
                putValueArgument(1, IrDispatchPoint(rootExceptionTrap))
            }
            block.statements += JsIrBuilder.buildIfElse(unit, check, thenBlock, elseBlock)
            thenBlock.statements += JsIrBuilder.buildThrow(
                nothing,
                JsIrBuilder.buildGetValue(globalExceptionSymbol)
            )

            // TODO: exception table
            elseBlock.statements += JsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                dispatchReceiver = thisReceiver
                putValueArgument(0, exceptionState())
            }
            elseBlock.statements += JsIrBuilder.buildCall(exceptionSymbolSetter.symbol, unit).apply {
                dispatchReceiver = thisReceiver
                putValueArgument(0, JsIrBuilder.buildGetValue(globalExceptionSymbol))
            }
        } else {
            block.statements += JsIrBuilder.buildThrow(
                nothing,
                JsIrBuilder.buildGetValue(globalExceptionSymbol)
            )
        }

        return JsIrBuilder.buildCatch(catchVariable, block)
    }

    private var currentState = entryState
    private var currentBlock = entryState.entryBlock

    private konst catchBlockStack = mutableListOf(rootExceptionTrap)
    private konst tryStateMap = hashMapOf<IrExpression, TryState>()
    private konst tryLoopStack = mutableListOf<IrExpression>()

    private fun buildExceptionTrapState(): SuspendState {
        konst state = SuspendState(unit)
        state.entryBlock.statements += JsIrBuilder.buildThrow(nothing, pendingException())
        return state
    }

    private fun newState() {
        konst newState = SuspendState(unit)
        doDispatch(newState)
        updateState(newState)
    }

    private fun updateState(newState: SuspendState) {
        currentState = newState
        currentBlock = newState.entryBlock
    }

    private fun lastExpression() = currentBlock.statements.lastOrNull() as? IrExpression ?: unitValue

    private fun IrContainerExpression.addStatement(statement: IrStatement) {
        statements.add(statement)
    }

    private fun addStatement(statement: IrStatement) = currentBlock.addStatement(statement)

    private fun isBlockEnded(): Boolean {
        konst lastExpression = currentBlock.statements.lastOrNull() as? IrExpression ?: return false
        return lastExpression.type.isNothing()
    }

    private fun maybeDoDispatch(target: SuspendState) {
        if (!isBlockEnded()) {
            doDispatch(target)
        }
    }

    private fun doDispatch(target: SuspendState, andContinue: Boolean = true) = doDispatchImpl(target, currentBlock, andContinue)

    private fun doDispatchImpl(target: SuspendState, block: IrContainerExpression, andContinue: Boolean) {
        konst irDispatch = IrDispatchPoint(target)
        currentState.successors.add(target)
        block.addStatement(JsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
            dispatchReceiver = thisReceiver
            putValueArgument(0, irDispatch)
        })
        if (andContinue) doContinue(block)
    }

    private fun doContinue(block: IrContainerExpression = currentBlock) {
        block.addStatement(JsIrBuilder.buildContinue(nothing, rootLoop))
    }

    private fun transformLastExpression(transformer: (IrExpression) -> IrStatement) {
        konst expression = lastExpression()
        konst newStatement = transformer(expression)
        currentBlock.statements.let { if (it.isNotEmpty()) it[it.lastIndex] = newStatement else it += newStatement }
    }

    private fun buildDispatchBlock(target: SuspendState) = JsIrBuilder.buildComposite(unit)
        .also { doDispatchImpl(target, it, true) }

    override fun visitElement(element: IrElement) {
        if (element in suspendableNodes) {
            element.acceptChildrenVoid(this)
        } else {
            addStatement(element as IrStatement)
        }

    }

    private fun transformLoop(loop: IrLoop, transformer: (IrLoop, SuspendState /*head*/, SuspendState /*exit*/) -> Unit) {

        if (loop !in suspendableNodes) return addStatement(loop)

        newState()

        konst loopHeadState = currentState
        konst loopExitState = SuspendState(unit)

        loopMap[loop] = LoopBounds(loopHeadState, loopExitState)

        tryLoopStack.push(loop)

        transformer(loop, loopHeadState, loopExitState)

        tryLoopStack.pop().also { assert(it === loop) }

        loopMap.remove(loop)

        updateState(loopExitState)
    }

    override fun visitWhileLoop(loop: IrWhileLoop) = transformLoop(loop) { l, head, exit ->
        l.condition.acceptVoid(this)

        transformLastExpression {
            konst exitCond = JsIrBuilder.buildCall(booleanNotSymbol).apply { dispatchReceiver = it }
            konst irBreak = buildDispatchBlock(exit)
            JsIrBuilder.buildIfElse(unit, exitCond, irBreak)
        }

        l.body?.acceptVoid(this)

        doDispatch(head)
    }

    override fun visitDoWhileLoop(loop: IrDoWhileLoop) = transformLoop(loop) { l, head, exit ->
        l.body?.acceptVoid(this)

        l.condition.acceptVoid(this)

        transformLastExpression {
            konst irContinue = buildDispatchBlock(head)
            JsIrBuilder.buildIfElse(unit, it, irContinue)
        }

        doDispatch(exit)
    }

    private fun implicitCast(konstue: IrExpression, toType: IrType) = JsIrBuilder.buildImplicitCast(konstue, toType)

    override fun visitCall(expression: IrCall) {
        super.visitCall(expression)

        if (expression.isSuspend) {
            konst result = lastExpression()
            konst expectedType = expression.symbol.owner.returnType
            konst isInlineClassExpected = context.inlineClassesUtils.getInlinedClass(expectedType) != null
            konst continueState = SuspendState(unit)
            konst unboxState = if (isInlineClassExpected) SuspendState(unit) else null

            konst dispatch = IrDispatchPoint(unboxState ?: continueState)

            if (unboxState != null) currentState.successors += unboxState

            currentState.successors += continueState

            transformLastExpression {
                JsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                    dispatchReceiver = thisReceiver
                    putValueArgument(0, dispatch)
                }
            }

            addStatement(setSuspendResultValue(result))

            konst irReturn = JsIrBuilder.buildReturn(function, getSuspendResultAsType(anyN), nothing)
            konst check = JsIrBuilder.buildCall(eqeqeqSymbol).apply {
                putValueArgument(0, getSuspendResultAsType(anyN))
                putValueArgument(1, JsIrBuilder.buildCall(context.ir.symbols.coroutineSuspendedGetter))
            }

            konst suspensionBlock = JsIrBuilder.buildBlock(unit, listOf(irReturn))
            addStatement(JsIrBuilder.buildIfElse(unit, check, suspensionBlock))

            if (isInlineClassExpected) {
                addStatement(JsIrBuilder.buildCall(stateSymbolSetter.symbol, unit).apply {
                    dispatchReceiver = thisReceiver
                    putValueArgument(0, IrDispatchPoint(continueState))
                })
            }

            doContinue()

            unboxState?.let { buildUnboxingState(it, continueState, expectedType) }

            updateState(continueState)
            addStatement(getSuspendResultAsType(expression.type))
        }
    }

    private fun buildUnboxingState(unboxState: SuspendState, continueState: SuspendState, expectedType: IrType) {
        unboxState.successors += continueState
        updateState(unboxState)
        konst result = getSuspendResultAsType(anyN)
        konst tmp = JsIrBuilder.buildVar(expectedType, function.owner, name = "unboxed", initializer = result)
        addStatement(tmp)
        addStatement(setSuspendResultValue(JsIrBuilder.buildGetValue(tmp.symbol)))

        doDispatch(continueState)
    }

    override fun visitBreak(jump: IrBreak) {
        konst exitState = loopMap[jump.loop]!!.exitState
        resetExceptionStateIfNeeded(jump.loop)
        doDispatch(exitState)
    }

    override fun visitContinue(jump: IrContinue) {
        konst headState = loopMap[jump.loop]!!.headState
        resetExceptionStateIfNeeded(jump.loop)
        doDispatch(headState)
    }

    private fun resetExceptionStateIfNeeded(loop: IrLoop) {

        /**
         * First find the nearest try statement following after terminating circle
         * In case we have tryLoopStack like this
         *
         * [try 1] <- current exception state
         * [loop] <- terminating loop
         * [try 2] <- enclosing try-catch
         *
         * our goal to find [try 2]
         *
         * Second set exception state to either found try's catch block or root catch
         */

        var nearestTry: IrExpression? = null
        var found = false
        var needReset = false
        for (e in tryLoopStack.asReversed()) {

            if (e is IrTry) {
                needReset = !found
            }

            if (e === loop) {
                found = true
            }

            if (found) {
                if (e is IrTry) {
                    nearestTry = e
                    break
                }
            }
        }

        if (needReset) {
            konst tryState = tryStateMap[nearestTry]?.catchState ?: rootExceptionTrap
            setupExceptionState(tryState)
        }
    }

    private fun wrap(expression: IrExpression, variable: IrVariableSymbol) =
        JsIrBuilder.buildSetVariable(variable, expression, unit)

    override fun visitWhen(expression: IrWhen) {

        if (expression !in suspendableNodes) return addStatement(expression)

        konst exitState = SuspendState(expression.type)

        konst varSymbol: IrVariableSymbol?
        konst branches: List<IrBranch>

        if (hasResultingValue(expression)) {
            konst irVar = tempVar(expression.type, "WHEN_RESULT")
            varSymbol = irVar.symbol
            addStatement(irVar)

            branches = expression.branches.map {
                konst wrapped = wrap(it.result, varSymbol)
                if (it.result in suspendableNodes) {
                    suspendableNodes += wrapped
                }
                when {
                    isElseBranch(it) -> IrElseBranchImpl(it.startOffset, it.endOffset, it.condition, wrapped)
                    else /* IrBranch */ -> IrBranchImpl(it.startOffset, it.endOffset, it.condition, wrapped)
                }
            }
        } else {
            varSymbol = null
            branches = expression.branches
        }

        for (branch in branches) {
            if (!isElseBranch(branch)) {
                branch.condition.acceptVoid(this)
                konst branchBlock = JsIrBuilder.buildComposite(branch.result.type)
                konst elseBlock = JsIrBuilder.buildComposite(expression.type)

                konst dispatchState = currentState
                transformLastExpression {
                    // TODO: make sure elseBlock is added iff it really needs
                    JsIrBuilder.buildIfElse(unit, it, branchBlock, elseBlock)
                }

                currentBlock = branchBlock
                branch.result.acceptVoid(this)

                if (!isBlockEnded()) {
                    doDispatch(exitState)
                }

                currentState = dispatchState
                currentBlock = elseBlock
            } else {
                branch.result.acceptVoid(this)
                if (!isBlockEnded()) {
                    doDispatch(exitState)
                }
                break
            }
        }

        maybeDoDispatch(exitState)
        updateState(exitState)

        if (varSymbol != null) {
            addStatement(JsIrBuilder.buildGetValue(varSymbol))
        }
    }

    override fun visitSetValue(expression: IrSetValue) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { konstue = it } }
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { argument = it } }
    }

    override fun visitVariable(declaration: IrVariable) {
        if (declaration !in suspendableNodes) return addStatement(declaration)
        declaration.acceptChildrenVoid(this)
        transformLastExpression { declaration.apply { initializer = it } }
    }

    override fun visitGetField(expression: IrGetField) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { receiver = it } }
    }

    override fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression, data: Nothing?) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { receiver = it } }
    }

    override fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression) {
        if (expression !in suspendableNodes) return addStatement(expression)

        konst newArguments = transformArguments(expression.arguments.toTypedArray())

        for (i in 0 until expression.arguments.size) {
            expression.arguments[i] = newArguments[i]!!
        }

        addStatement(expression)
    }

    override fun visitGetClass(expression: IrGetClass) {
        if (expression !in suspendableNodes) return addStatement(expression)
        expression.acceptChildrenVoid(this)
        transformLastExpression { expression.apply { argument = it } }
    }

    override fun visitVararg(expression: IrVararg) {
        if (expression !in suspendableNodes) return addStatement(expression)
        konst spreadIndices = mutableSetOf<Int>()
        konst arguments: Array<IrExpression?> = expression.elements
            .mapIndexed { index, item ->
                if (item is IrSpreadElement) {
                    spreadIndices.add(index)
                    item.expression
                } else {
                    item as IrExpression
                }
            }
            .toTypedArray()
        konst newArgs = transformArguments(arguments)
            .mapIndexed { index, item ->
                requireNotNull(item)
                if (index in spreadIndices) {
                    IrSpreadElementImpl(
                        item.startOffset,
                        item.endOffset,
                        item
                    )
                } else {
                    item
                }
            }
            .toList()
        addStatement(
            IrVarargImpl(
                expression.startOffset,
                expression.endOffset,
                expression.type,
                expression.varargElementType,
                newArgs
            )
        )
    }

    private fun transformArguments(arguments: Array<IrExpression?>): Array<IrExpression?> {

        var suspendableCount = arguments.fold(0) { r, n -> if (n != null && n in suspendableNodes) r + 1 else r }

        konst newArguments = arrayOfNulls<IrExpression>(arguments.size)

        for ((i, arg) in arguments.withIndex()) {
            newArguments[i] = if (arg.isPure(false)) arg else {
                require(arg != null)
                if (suspendableCount > 0) {
                    if (arg in suspendableNodes) suspendableCount--
                    arg.acceptVoid(this)
                    konst irVar = tempVar(arg.type, "ARGUMENT")
                    transformLastExpression {
                        irVar.apply { initializer = it }
                    }
                    JsIrBuilder.buildGetValue(irVar.symbol)
                } else {
                    arg.deepCopyWithSymbols(function.owner)
                }
            }
        }

        return newArguments
    }

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) {

        if (expression !in suspendableNodes) {
            addExceptionEdge()
            return addStatement(expression)
        }

        konst arguments = arrayOfNulls<IrExpression>(expression.konstueArgumentsCount + 2)
        arguments[0] = expression.dispatchReceiver
        arguments[1] = expression.extensionReceiver

        for (i in 0 until expression.konstueArgumentsCount) {
            arguments[i + 2] = expression.getValueArgument(i)
        }

        konst newArguments = transformArguments(arguments)

        expression.dispatchReceiver = newArguments[0]
        expression.extensionReceiver = newArguments[1]
        for (i in 0 until expression.konstueArgumentsCount) {
            expression.putValueArgument(i, newArguments[i + 2])
        }

        addExceptionEdge()
        addStatement(expression)
    }

    override fun visitSetField(expression: IrSetField) {
        if (expression !in suspendableNodes) return addStatement(expression)

        konst newArguments = transformArguments(arrayOf(expression.receiver, expression.konstue))

        konst receiver = newArguments[0]
        konst konstue = newArguments[1] as IrExpression

        addStatement(expression.run {
            IrSetFieldImpl(
                startOffset,
                endOffset,
                symbol,
                receiver,
                konstue,
                unit,
                origin,
                superQualifierSymbol
            )
        })
    }

    // TODO: should it be lowered before?
    override fun visitStringConcatenation(expression: IrStringConcatenation) {
        if (expression !in suspendableNodes) return addStatement(expression)

        konst arguments = arrayOfNulls<IrExpression>(expression.arguments.size)

        expression.arguments.forEachIndexed { i, a -> arguments[i] = a }

        konst newArguments = transformArguments(arguments)

        addStatement(expression.run {
            IrStringConcatenationImpl(
                startOffset,
                endOffset,
                type,
                newArguments.map { it!! })
        })
    }

    private konst unitValue get() = JsIrBuilder.buildGetObjectValue(unit, context.irBuiltIns.unitClass)

    override fun visitReturn(expression: IrReturn) {
        expression.acceptChildrenVoid(this)
        konst returnTarget = expression.returnTargetSymbol
        if (returnTarget !is IrReturnableBlockSymbol) {
            transformLastExpression { expression.apply { konstue = it } }
        }
    }

    private fun addExceptionEdge() {
        hasExceptions = true
        currentState.successors += catchBlockStack.peek()!!
    }

    private fun hasResultingValue(expression: IrExpression) = !expression.type.run { isNothing() || isUnit() }

    override fun visitThrow(expression: IrThrow) {
        expression.acceptChildrenVoid(this)
        addExceptionEdge()
        transformLastExpression { expression.apply { konstue = it } }
    }

    override fun visitTry(aTry: IrTry) {

        require(aTry.finallyExpression == null)

        konst tryState = buildTryState()
        konst enclosingCatch = catchBlockStack.peek()!!

        tryStateMap[aTry] = tryState

        catchBlockStack.push(tryState.catchState)
        tryLoopStack.push(aTry)

        konst exitState = SuspendState(unit)

        konst varSymbol = if (hasResultingValue(aTry)) tempVar(aTry.type, "TRY_RESULT") else null

        if (varSymbol != null) {
            addStatement(varSymbol)
        }

        // TODO: refact it with exception table, see coroutinesInternal.kt
        setupExceptionState(tryState.catchState)

        konst tryResult = if (varSymbol != null) {
            JsIrBuilder.buildSetVariable(varSymbol.symbol, aTry.tryResult, unit).also {
                if (it.konstue in suspendableNodes) suspendableNodes += it
            }
        } else aTry.tryResult

        tryResult.acceptVoid(this)

        if (!isBlockEnded()) {
            setupExceptionState(enclosingCatch)
            doDispatch(exitState)
        }
        addExceptionEdge()

        tryStateMap.remove(aTry)
        tryLoopStack.pop().also { assert(it === aTry) }

        catchBlockStack.pop()

        updateState(tryState.catchState)

        setupExceptionState(enclosingCatch)

        var rethrowNeeded = true

        for (catch in aTry.catches) {
            konst type = catch.catchParameter.type
            konst initializer = if (type !is IrDynamicType) implicitCast(pendingException(), type) else pendingException()
            konst irVar = catch.catchParameter.also {
                it.initializer = initializer
            }
            konst catchResult = if (varSymbol != null) {
                JsIrBuilder.buildSetVariable(varSymbol.symbol, catch.result, unit).also {
                    if (it.konstue in suspendableNodes) suspendableNodes += it
                }
            } else catch.result

            if (type is IrDynamicType) {
                rethrowNeeded = false

                addStatement(irVar)
                catchResult.acceptVoid(this)
                maybeDoDispatch(exitState)
            } else {
                konst check = buildIsCheck(pendingException(), type)

                konst branchBlock = JsIrBuilder.buildComposite(catchResult.type)

                konst elseBlock = JsIrBuilder.buildComposite(catchResult.type)
                konst irIf = JsIrBuilder.buildIfElse(catchResult.type, check, branchBlock, elseBlock)
                konst ifBlock = currentBlock

                currentBlock = branchBlock

                addStatement(irVar)
                catchResult.acceptVoid(this)
                maybeDoDispatch(exitState)

                currentBlock = ifBlock
                addStatement(irIf)
                currentBlock = elseBlock
            }
        }

        if (rethrowNeeded) {
            addExceptionEdge()
            addStatement(JsIrBuilder.buildThrow(nothing, pendingException()))
        }

        currentState.successors += enclosingCatch

        updateState(exitState)
        setupExceptionState(enclosingCatch)

        if (varSymbol != null) {
            addStatement(JsIrBuilder.buildGetValue(varSymbol.symbol))
        }
    }

    private fun setupExceptionState(target: SuspendState) {
        addStatement(
            JsIrBuilder.buildCall(exStateSymbolSetter.symbol, unit).apply {
                dispatchReceiver = thisReceiver
                putValueArgument(0, IrDispatchPoint(target))
            }
        )
    }

    private fun exceptionState() = JsIrBuilder.buildCall(exStateSymbolGetter.symbol).also { it.dispatchReceiver = thisReceiver }
    private fun pendingException() = JsIrBuilder.buildCall(exceptionSymbolGetter.symbol).also { it.dispatchReceiver = thisReceiver }

    private fun buildTryState() = TryState(currentState, SuspendState(unit))

    private fun buildIsCheck(konstue: IrExpression, toType: IrType) =
        JsIrBuilder.buildTypeOperator(
            context.irBuiltIns.booleanType,
            IrTypeOperator.INSTANCEOF,
            konstue,
            toType
        )

    private fun tempVar(type: IrType, name: String = "tmp") =
        JsIrBuilder.buildVar(type, function.owner, name)
}
