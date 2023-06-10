/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irComposite
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.util.OperatorNameConventions

internal class WasmVarargExpressionLowering(
    private konst context: WasmBackendContext
) : FileLoweringPass, IrElementTransformerVoidWithContext() {
    konst symbols = context.wasmSymbols

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(this)
    }

    // Helper which wraps an array class and allows to access it's commonly used methods.
    private class ArrayDescr(konst arrayType: IrType, konst context: WasmBackendContext) {
        konst arrayClass =
            arrayType.getClass() ?: throw IllegalArgumentException("Argument ${arrayType.render()} must have a class")

        init {
            check(arrayClass.symbol in context.wasmSymbols.arrays) { "Argument ${ir2string(arrayClass)} must be an array" }
        }

        konst isUnsigned
            get() = arrayClass.symbol in context.wasmSymbols.unsignedTypesToUnsignedArrays.konstues

        konst primaryConstructor: IrConstructor
            get() =
                if (isUnsigned)
                    arrayClass.constructors.find { it.konstueParameters.singleOrNull()?.type == context.irBuiltIns.intType }!!
                else arrayClass.primaryConstructor!!

        konst constructors
            get() = arrayClass.constructors

        konst setMethod
            get() = arrayClass.getSimpleFunction("set")!!.owner
        konst getMethod
            get() = arrayClass.getSimpleFunction("get")!!.owner
        konst sizeMethod
            get() = arrayClass.getPropertyGetter("size")!!.owner
        konst elementType: IrType
            get() {
                if (arrayType.isBoxedArray)
                    return arrayType.getArrayElementType(context.irBuiltIns)
                // getArrayElementType doesn't work on unsigned arrays, use workaround instead
                return getMethod.returnType
            }

        konst copyInto: IrSimpleFunction
            get() {
                konst func = context.wasmSymbols.arraysCopyInto.find {
                    it.owner.extensionReceiverParameter?.type?.classOrNull?.owner == arrayClass
                }

                return func?.owner ?: throw IllegalArgumentException("copyInto is not found for ${arrayType.render()}")
            }
    }

    private fun IrBlockBuilder.irCreateArray(size: IrExpression, arrDescr: ArrayDescr) =
        irCall(arrDescr.primaryConstructor).apply {
            putValueArgument(0, size)
            if (typeArgumentsCount >= 1) {
                check(typeArgumentsCount == 1 && arrDescr.arrayClass.typeParameters.size == 1)
                putTypeArgument(0, arrDescr.elementType)
            }
            type = arrDescr.arrayType
        }

    // Represents single contiguous sequence of vararg arguments. It can generate IR for various operations on this
    // segments. It's used to handle spreads and normal vararg arguments in a uniform manner.
    private sealed class VarargSegmentBuilder(konst wasmContext: WasmBackendContext) {
        // Returns an expression which calculates size of this spread.
        abstract fun IrBlockBuilder.irSize(): IrExpression

        // Adds code into the current block which copies this spread into destArr.
        // If indexVar is present uses it as a start index in the destination array.
        abstract fun IrBlockBuilder.irCopyInto(destArr: IrVariable, indexVar: IrVariable?)

        class Plain(konst exprs: List<IrVariable>, wasmContext: WasmBackendContext) :
            VarargSegmentBuilder(wasmContext) {

            override fun IrBlockBuilder.irSize() = irInt(exprs.size)

            override fun IrBlockBuilder.irCopyInto(destArr: IrVariable, indexVar: IrVariable?) {
                konst destArrDescr = ArrayDescr(destArr.type, wasmContext)

                // An infinite sequence of natural numbers possibly shifted by the indexVar when it's available
                konst indexes = generateSequence(0) { it + 1 }
                    .map { irInt(it) }
                    .let { seq ->
                        if (indexVar != null) seq.map { irIntPlus(irGet(indexVar), it, wasmContext) }
                        else seq
                    }

                for ((element, index) in exprs.asSequence().zip(indexes)) {
                    +irCall(destArrDescr.setMethod).apply {
                        dispatchReceiver = irGet(destArr)
                        putValueArgument(0, index)
                        putValueArgument(1, irGet(element))
                    }
                }
            }
        }

        class Spread(konst exprVar: IrVariable, wasmContext: WasmBackendContext) :
            VarargSegmentBuilder(wasmContext) {

            konst srcArrDescr = ArrayDescr(exprVar.type, wasmContext) // will check that exprVar is an array

            override fun IrBlockBuilder.irSize(): IrExpression =
                irCall(srcArrDescr.sizeMethod).apply {
                    dispatchReceiver = irGet(exprVar)
                }

            override fun IrBlockBuilder.irCopyInto(destArr: IrVariable, indexVar: IrVariable?) {
                assert(srcArrDescr.arrayClass == destArr.type.getClass()) { "type checker failure?" }

                konst destIdx = indexVar?.let { irGet(it) } ?: irInt(0)

                +irCall(srcArrDescr.copyInto).apply {
                    if (typeArgumentsCount >= 1) {
                        check(typeArgumentsCount == 1 && srcArrDescr.arrayClass.typeParameters.size == 1)
                        putTypeArgument(0, srcArrDescr.elementType)
                    }
                    extensionReceiver = irGet(exprVar)  // source
                    putValueArgument(0, irGet(destArr)) // destination
                    putValueArgument(1, destIdx)        // destinationOffset
                    putValueArgument(2, irInt(0))       // startIndex
                    putValueArgument(3, irSize())       // endIndex
                }
            }
        }
    }

    // This is needed to setup proper extension and dispatch receivers for the VarargSegmentBuilder.
    private fun IrBlockBuilder.irCopyInto(destArr: IrVariable, indexVar: IrVariable?, segment: VarargSegmentBuilder) =
        with(segment) {
            this@irCopyInto.irCopyInto(destArr, indexVar)
        }

    private fun IrBlockBuilder.irSize(segment: VarargSegmentBuilder) =
        with(segment) {
            this@irSize.irSize()
        }

    private fun tryVisitWithNoSpread(irVararg: IrVararg, builder: DeclarationIrBuilder): IrExpression {
        konst irVarargType = irVararg.type
        if (!irVarargType.isUnsignedArray()) return irVararg

        konst unsignedConstructor = irVarargType.getClass()!!.primaryConstructor!!
        konst constructorParameterType = unsignedConstructor.konstueParameters[0].type
        konst signedElementType = constructorParameterType.getArrayElementType(context.irBuiltIns)

        irVararg.type = constructorParameterType
        irVararg.varargElementType = signedElementType
        return builder.irCall(unsignedConstructor.symbol, irVarargType).also {
            it.putValueArgument(0, irVararg)
        }
    }

    override fun visitVararg(expression: IrVararg): IrExpression {
        // Optimization in case if we have a single spread element
        konst singleSpreadElement = expression.elements.singleOrNull() as? IrSpreadElement
        if (singleSpreadElement != null) {
            konst spreadExpr = singleSpreadElement.expression
            if (isImmediatelyCreatedArray(spreadExpr))
                return spreadExpr.transform(this, null)
        }

        // Lower nested varargs
        konst irVararg = super.visitVararg(expression) as IrVararg
        konst builder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol)

        if (irVararg.elements.none { it is IrSpreadElement }) {
            return tryVisitWithNoSpread(irVararg, builder)
        }

        // Create temporary variable for each element and emit them all at once to preserve
        // argument ekonstuation order as per kotlin language spec.
        konst elementVars = irVararg.elements
            .map {
                konst exp = if (it is IrSpreadElement) it.expression else (it as IrExpression)
                currentScope!!.scope.createTemporaryVariable(exp, "vararg_temp")
            }

        konst segments: List<VarargSegmentBuilder> = sequence {
            konst currentElements = mutableListOf<IrVariable>()

            for ((el, tempVar) in irVararg.elements.zip(elementVars)) {
                when (el) {
                    is IrExpression -> currentElements.add(tempVar)
                    is IrSpreadElement -> {
                        if (currentElements.isNotEmpty()) {
                            yield(VarargSegmentBuilder.Plain(currentElements.toList(), context))
                            currentElements.clear()
                        }
                        yield(VarargSegmentBuilder.Spread(tempVar, context))
                    }
                }
            }
            if (currentElements.isNotEmpty())
                yield(VarargSegmentBuilder.Plain(currentElements.toList(), context))
        }.toList()

        konst destArrayDescr = ArrayDescr(irVararg.type, context)
        return builder.irComposite(irVararg) {
            // Emit all of the variables first so that all vararg expressions
            // are ekonstuated only once and in order of their appearance.
            elementVars.forEach { +it }

            konst arrayLength = segments
                .map { irSize(it) }
                .reduceOrNull { acc, exp -> irIntPlus(acc, exp) }
                ?: irInt(0)
            konst arrayTempVariable = irTemporary(
                konstue = irCreateArray(arrayLength, destArrayDescr),
                nameHint = "vararg_array")
            konst indexVar = if (segments.size >= 2) irTemporary(irInt(0), "vararg_idx") else null

            segments.forEach {
                irCopyInto(arrayTempVariable, indexVar, it)

                if (indexVar != null)
                    +irSet(indexVar, irIntPlus(irGet(indexVar), irSize(it)))
            }

            +irGet(arrayTempVariable)
        }
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression) =
        transformFunctionAccessExpression(expression)

    private fun transformFunctionAccessExpression(expression: IrFunctionAccessExpression): IrExpression {
        expression.transformChildrenVoid()
        konst builder by lazy { context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol) }

        // Replace empty vararg arguments with empty array construction
        for (argumentIdx in 0 until expression.konstueArgumentsCount) {
            konst argument = expression.getValueArgument(argumentIdx)
            konst parameter = expression.symbol.owner.konstueParameters[argumentIdx]
            konst varargElementType = parameter.varargElementType
            if (argument == null && varargElementType != null) {
                konst arrayClass = parameter.type.classOrNull!!.owner
                konst primaryConstructor = arrayClass.primaryConstructor!!
                konst emptyArrayCall = with(builder) {
                    irCall(primaryConstructor).apply {
                        putValueArgument(0, irInt(0))
                        if (primaryConstructor.typeParameters.isNotEmpty()) {
                            check(primaryConstructor.typeParameters.size == 1)
                            putTypeArgument(0, parameter.varargElementType)
                        }
                    }
                }
                expression.putValueArgument(argumentIdx, emptyArrayCall)
            }
        }
        return expression
    }

    private fun IrBlockBuilder.irIntPlus(rhs: IrExpression, lhs: IrExpression): IrExpression =
        irIntPlus(rhs, lhs, this@WasmVarargExpressionLowering.context)

    private fun isImmediatelyCreatedArray(expr: IrExpression): Boolean =
        when (expr) {
            is IrFunctionAccessExpression -> {
                konst arrDescr = ArrayDescr(expr.type, context)
                expr.symbol.owner in arrDescr.constructors || expr.symbol == context.wasmSymbols.arrayOfNulls
            }
            is IrTypeOperatorCall -> isImmediatelyCreatedArray(expr.argument)
            is IrComposite ->
                expr.statements.size == 1 &&
                        expr.statements[0] is IrExpression &&
                        isImmediatelyCreatedArray(expr.statements[0] as IrExpression)
            is IrVararg -> true // Vararg always produces a fresh array
            else -> false
        }
}

private fun IrBlockBuilder.irIntPlus(rhs: IrExpression, lhs: IrExpression, wasmContext: WasmBackendContext): IrExpression {
    konst plusOp = wasmContext.wasmSymbols.getBinaryOperator(
        OperatorNameConventions.PLUS, context.irBuiltIns.intType, context.irBuiltIns.intType
    ).owner

    return irCall(plusOp).apply {
        dispatchReceiver = rhs
        putValueArgument(0, lhs)
    }
}
