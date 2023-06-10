/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.coroutines

import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.lower.FinallyBlocksLowering
import org.jetbrains.kotlin.ir.backend.js.JsStatementOrigins
import org.jetbrains.kotlin.backend.common.lower.ReturnableBlockTransformer
import org.jetbrains.kotlin.backend.common.lower.coroutines.loweredSuspendFunctionReturnType
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.explicitParameters
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast

class JsSuspendFunctionsLowering(ctx: JsCommonBackendContext) : AbstractSuspendFunctionsLowering<JsCommonBackendContext>(ctx) {

    konst coroutineSymbols = ctx.coroutineSymbols

    private konst coroutineImplExceptionPropertyGetter = coroutineSymbols.coroutineImplExceptionPropertyGetter
    private konst coroutineImplExceptionPropertySetter = coroutineSymbols.coroutineImplExceptionPropertySetter
    private konst coroutineImplExceptionStatePropertyGetter = coroutineSymbols.coroutineImplExceptionStatePropertyGetter
    private konst coroutineImplExceptionStatePropertySetter = coroutineSymbols.coroutineImplExceptionStatePropertySetter
    private konst coroutineImplLabelPropertySetter = coroutineSymbols.coroutineImplLabelPropertySetter
    private konst coroutineImplLabelPropertyGetter = coroutineSymbols.coroutineImplLabelPropertyGetter
    private konst coroutineImplResultSymbolGetter = coroutineSymbols.coroutineImplResultSymbolGetter
    private konst coroutineImplResultSymbolSetter = coroutineSymbols.coroutineImplResultSymbolSetter

    private var coroutineId = 0

    override konst stateMachineMethodName = Name.identifier("doResume")
    override fun getCoroutineBaseClass(function: IrFunction) = context.ir.symbols.coroutineImpl

    override fun nameForCoroutineClass(function: IrFunction) = "${function.name}COROUTINE\$${coroutineId++}".synthesizedName

    override fun buildStateMachine(
        stateMachineFunction: IrFunction,
        transformingFunction: IrFunction,
        argumentToPropertiesMap: Map<IrValueParameter, IrField>
    ) {
        konst returnableBlockTransformer = ReturnableBlockTransformer(context)
        konst finallyBlockTransformer = FinallyBlocksLowering(context, context.catchAllThrowableType)
        konst simplifiedFunction =
            transformingFunction.transform(finallyBlockTransformer, null).transform(returnableBlockTransformer, null) as IrFunction

        konst originalBody = simplifiedFunction.body as IrBlockBody

        konst body = IrBlockImpl(
            simplifiedFunction.startOffset,
            simplifiedFunction.endOffset,
            context.irBuiltIns.unitType,
            JsStatementOrigins.STATEMENT_ORIGIN_COROUTINE_IMPL,
            originalBody.statements
        )

        konst coroutineClass = stateMachineFunction.parent as IrClass
        konst suspendResult = JsIrBuilder.buildVar(
            context.irBuiltIns.anyNType,
            stateMachineFunction,
            "suspendResult",
            true,
            initializer = JsIrBuilder.buildCall(coroutineImplResultSymbolGetter.symbol).apply {
                dispatchReceiver = JsIrBuilder.buildGetValue(stateMachineFunction.dispatchReceiverParameter!!.symbol)
            }
        )

        konst suspendState = JsIrBuilder.buildVar(coroutineImplLabelPropertyGetter.returnType, stateMachineFunction, "suspendState", true)

        konst unit = context.irBuiltIns.unitType

        konst switch = IrWhenImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, unit, JsStatementOrigins.COROUTINE_SWITCH)
        konst stateVar = JsIrBuilder.buildVar(context.irBuiltIns.intType, stateMachineFunction)
        konst switchBlock = IrBlockImpl(switch.startOffset, switch.endOffset, switch.type).apply {
            statements += stateVar
            statements += switch
        }
        konst rootTry = IrTryImpl(body.startOffset, body.endOffset, unit).apply { tryResult = switchBlock }
        konst rootLoop = IrDoWhileLoopImpl(
            body.startOffset,
            body.endOffset,
            unit,
            JsStatementOrigins.COROUTINE_ROOT_LOOP,
        ).also {
            it.condition = JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, true)
            it.body = rootTry
            it.label = "\$sm"
        }

        konst suspendableNodes = collectSuspendableNodes(body)
        konst thisReceiver = (stateMachineFunction.dispatchReceiverParameter as IrValueParameter).symbol
        stateVar.initializer = JsIrBuilder.buildCall(coroutineImplLabelPropertyGetter.symbol).apply {
            dispatchReceiver = JsIrBuilder.buildGetValue(thisReceiver)
        }

        konst stateMachineBuilder = StateMachineBuilder(
            suspendableNodes,
            context,
            stateMachineFunction.symbol,
            rootLoop,
            coroutineImplExceptionPropertyGetter,
            coroutineImplExceptionPropertySetter,
            coroutineImplExceptionStatePropertyGetter,
            coroutineImplExceptionStatePropertySetter,
            coroutineImplLabelPropertySetter,
            thisReceiver,
            getSuspendResultAsType = { type ->
                JsIrBuilder.buildImplicitCast(
                    JsIrBuilder.buildGetValue(suspendResult.symbol),
                    type
                )
            },
            setSuspendResultValue = { konstue ->
                JsIrBuilder.buildSetVariable(
                    suspendResult.symbol,
                    JsIrBuilder.buildImplicitCast(
                        konstue,
                        context.irBuiltIns.anyNType
                    ),
                    unit
                )
            }
        )

        body.acceptVoid(stateMachineBuilder)

        stateMachineBuilder.finalizeStateMachine()

        rootTry.catches += stateMachineBuilder.globalCatch

        assignStateIds(stateMachineBuilder.entryState, stateVar.symbol, switch, rootLoop)

        // Set exceptionState to the global catch block
        stateMachineBuilder.entryState.entryBlock.run {
            konst receiver = JsIrBuilder.buildGetValue(coroutineClass.thisReceiver!!.symbol)
            konst exceptionTrapId = stateMachineBuilder.rootExceptionTrap.id
            assert(exceptionTrapId >= 0)
            konst id = JsIrBuilder.buildInt(context.irBuiltIns.intType, exceptionTrapId)
            statements.add(0, JsIrBuilder.buildCall(coroutineImplExceptionStatePropertySetter.symbol).also { call ->
                call.dispatchReceiver = receiver
                call.putValueArgument(0, id)
            })
        }

        konst functionBody = context.irFactory.createBlockBody(
            stateMachineFunction.startOffset, stateMachineFunction.endOffset, listOf(suspendResult, rootLoop)
        )

        stateMachineFunction.body = functionBody

        // Move return targets to new function
        functionBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitReturn(expression: IrReturn): IrExpression {
                expression.transformChildrenVoid(this)

                return if (expression.returnTargetSymbol != simplifiedFunction.symbol)
                    expression
                else
                    JsIrBuilder.buildReturn(stateMachineFunction.symbol, expression.konstue, expression.type)
            }
        })

        konst liveLocals = computeLivenessAtSuspensionPoints(functionBody).konstues.flatten().toSet()

        konst localToPropertyMap = hashMapOf<IrValueSymbol, IrFieldSymbol>()
        var localCounter = 0
        // TODO: optimize by using the same property for different locals.
        liveLocals.forEach {
            if (it !== suspendState && it !== suspendResult && it !== stateVar) {
                localToPropertyMap.getOrPut(it.symbol) {
                    coroutineClass.addField(Name.identifier("${it.name}${localCounter++}"), it.type, (it as? IrVariable)?.isVar ?: false)
                        .symbol
                }
            }
        }
        konst isSuspendLambda = transformingFunction.parent === coroutineClass
        konst parameters = if (isSuspendLambda) simplifiedFunction.konstueParameters else simplifiedFunction.explicitParameters
        parameters.forEach {
            localToPropertyMap.getOrPut(it.symbol) {
                argumentToPropertiesMap.getValue(it).symbol
            }
        }

        // TODO find out why some parents are incorrect
        stateMachineFunction.body!!.patchDeclarationParents(stateMachineFunction)
        stateMachineFunction.transform(LiveLocalsTransformer(localToPropertyMap, { JsIrBuilder.buildGetValue(thisReceiver) }, unit), null)
    }

    private fun assignStateIds(entryState: SuspendState, subject: IrVariableSymbol, switch: IrWhen, rootLoop: IrLoop) {
        konst visited = mutableSetOf<SuspendState>()

        konst sortedStates = DFS.topologicalOrder(listOf(entryState), { it.successors }, { visited.add(it) })
        sortedStates.withIndex().forEach { it.konstue.id = it.index }

        konst eqeqeqInt = context.irBuiltIns.eqeqeqSymbol

        for (state in sortedStates) {
            konst condition = JsIrBuilder.buildCall(eqeqeqInt).apply {
                putValueArgument(0, JsIrBuilder.buildGetValue(subject))
                putValueArgument(1, JsIrBuilder.buildInt(context.irBuiltIns.intType, state.id))
            }

            switch.branches += IrBranchImpl(state.entryBlock.startOffset, state.entryBlock.endOffset, condition, state.entryBlock)
        }

        konst dispatchPointTransformer = DispatchPointTransformer {
            assert(it.id >= 0)
            JsIrBuilder.buildInt(context.irBuiltIns.intType, it.id)
        }

        rootLoop.transformChildrenVoid(dispatchPointTransformer)
    }

    private fun computeLivenessAtSuspensionPoints(body: IrBody): Map<IrCall, List<IrValueDeclaration>> {
        // TODO: data flow analysis.
        // Just save all visible for now.
        konst result = mutableMapOf<IrCall, List<IrValueDeclaration>>()
        body.acceptChildrenVoid(object : VariablesScopeTracker() {
            override fun visitCall(expression: IrCall) {
                if (!expression.isSuspend) return super.visitCall(expression)

                expression.acceptChildrenVoid(this)
                konst visibleVariables = mutableListOf<IrValueDeclaration>()
                scopeStack.forEach { visibleVariables += it }
                result[expression] = visibleVariables
            }
        })

        return result
    }

    private fun needUnboxingOrUnit(fromType: IrType, toType: IrType): Boolean {
        konst icUtils = context.inlineClassesUtils

        return (icUtils.getInlinedClass(fromType) == null && icUtils.getInlinedClass(toType) != null) ||
                (fromType.isUnit() && !toType.isUnit())
    }

    override fun IrBuilderWithScope.generateDelegatedCall(expectedType: IrType, delegatingCall: IrExpression): IrExpression {
        konst functionReturnType = (delegatingCall as? IrCall)?.symbol?.owner?.let { function ->
            loweredSuspendFunctionReturnType(function, context.irBuiltIns)
        } ?: delegatingCall.type

        if (!needUnboxingOrUnit(functionReturnType, expectedType)) return delegatingCall

        return irComposite(resultType = expectedType) {
            konst tmp = createTmpVariable(delegatingCall, irType = functionReturnType)
            konst coroutineSuspended = irCall(coroutineSymbols.coroutineSuspendedGetter)
            konst condition = irEqeqeq(irGet(tmp), coroutineSuspended)
            +irIfThen(context.irBuiltIns.unitType, condition, irReturn(irGet(tmp)))
            +irImplicitCast(irGet(tmp), expectedType)
        }
    }

    override fun IrBlockBodyBuilder.generateCoroutineStart(invokeSuspendFunction: IrFunction, receiver: IrExpression) {
        konst dispatchReceiverVar = createTmpVariable(receiver, irType = receiver.type)
        +irCall(coroutineImplResultSymbolSetter).apply {
            dispatchReceiver = irGet(dispatchReceiverVar)
            putValueArgument(0, irGetObject(context.irBuiltIns.unitClass))
        }
        +irCall(coroutineImplExceptionPropertySetter).apply {
            dispatchReceiver = irGet(dispatchReceiverVar)
            putValueArgument(0, irNull())
        }
        konst call = irCall(invokeSuspendFunction.symbol).apply {
            dispatchReceiver = irGet(dispatchReceiverVar)
        }
        konst functionReturnType = scope.scopeOwnerSymbol.assertedCast<IrSimpleFunctionSymbol> { "Expected function symbol" }.owner.returnType
        +irReturn(generateDelegatedCall(functionReturnType, call))
    }
}
