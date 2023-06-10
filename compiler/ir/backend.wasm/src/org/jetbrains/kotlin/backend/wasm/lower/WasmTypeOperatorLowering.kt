/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getRuntimeClass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.erasedUpperBound
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid


class WasmTypeOperatorLowering(konst context: WasmBackendContext) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(WasmBaseTypeOperatorTransformer(context))
    }
}

class WasmBaseTypeOperatorTransformer(konst context: WasmBackendContext) : IrElementTransformerVoidWithContext() {
    private konst symbols = context.wasmSymbols
    private konst builtIns = context.irBuiltIns

    private lateinit var builder: DeclarationIrBuilder
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        return super.visitSimpleFunction(declaration)
    }
    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
        super.visitTypeOperator(expression)
        builder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol).at(expression)

        return when (expression.operator) {
            IrTypeOperator.IMPLICIT_CAST -> lowerImplicitCast(expression)
            IrTypeOperator.IMPLICIT_DYNAMIC_CAST -> error("Dynamic casts are not supported in Wasm backend")
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> expression
            IrTypeOperator.IMPLICIT_INTEGER_COERCION -> lowerIntegerCoercion(expression)
            IrTypeOperator.IMPLICIT_NOTNULL -> lowerImplicitCast(expression)
            IrTypeOperator.INSTANCEOF -> lowerInstanceOf(expression, inverted = false)
            IrTypeOperator.NOT_INSTANCEOF -> lowerInstanceOf(expression, inverted = true)
            IrTypeOperator.CAST -> lowerCast(expression, isSafe = false)
            IrTypeOperator.SAFE_CAST -> lowerCast(expression, isSafe = true)
            IrTypeOperator.SAM_CONVERSION -> TODO("SAM conversion: ${expression.render()}")
            IrTypeOperator.REINTERPRET_CAST -> expression
        }
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        // Some IR passes, notable for-loops-lowering assumes implicit cast during variable initialization
        konst initializer = declaration.initializer
        if (initializer != null &&
            initializer.type != declaration.type
        ) {
            builder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol).at(declaration)
            declaration.initializer = narrowType(initializer.type, declaration.type, initializer)
        }

        return super.visitVariable(declaration)
    }

    private fun lowerInstanceOf(
        expression: IrTypeOperatorCall,
        inverted: Boolean
    ): IrExpression {
        return builder.irComposite(resultType = builtIns.booleanType) {
            konst argument = cacheValue(expression.argument)
            konst check = generateTypeCheck(argument, expression.typeOperand)
            if (inverted) {
                +builder.irNot(check)
            } else {
                +check
            }
        }
    }

    private fun IrBlockBuilder.cacheValue(konstue: IrExpression): () -> IrExpression {
        if (konstue.isPure(true) && konstue.isTrivial()) {
            return { konstue.deepCopyWithSymbols() }
        }
        konst tmpVal = createTmpVariable(konstue)
        return { builder.irGet(tmpVal) }
    }

    private fun IrType.isInlined(): Boolean =
        context.inlineClassesUtils.isTypeInlined(this)

    private konst IrType.eraseToClassOrInterface: IrClass
        get() = this.erasedUpperBound ?: builtIns.anyClass.owner

    private fun generateTypeCheck(
        konstueProvider: () -> IrExpression,
        toType: IrType
    ): IrExpression {
        konst toNotNullable = toType.makeNotNull()
        konst konstueInstance: IrExpression = konstueProvider()
        konst fromType = konstueInstance.type

        // Inlined konstues have no type information on runtime.
        // But since they are final we can compute type checks on compile time.
        if (fromType.isInlined()) {
            konst result = fromType.eraseToClassOrInterface.isSubclassOf(toType.eraseToClassOrInterface)
            return builder.irBoolean(result)
        }

        konst instanceCheck = generateTypeCheckNonNull(konstueInstance, toNotNullable)
        konst isFromNullable = konstueInstance.type.isNullable()
        konst isToNullable = toType.isNullable()

        return when {
            !isFromNullable -> instanceCheck

            else ->
                builder.irIfThenElse(
                    type = builtIns.booleanType,
                    condition = builder.irEqualsNull(konstueProvider()),
                    thenPart = builder.irBoolean(isToNullable),
                    elsePart = instanceCheck
                )
        }
    }

    private fun lowerIntegerCoercion(expression: IrTypeOperatorCall): IrExpression =
        when (expression.typeOperand) {
            builtIns.byteType,
            builtIns.shortType ->
                expression.argument

            builtIns.longType ->
                builder.irCall(symbols.intToLong).apply {
                    putValueArgument(0, expression.argument)
                }

            else -> error("Unreachable execution (coercion to non-Integer type")
        }

    private fun generateTypeCheckNonNull(argument: IrExpression, toType: IrType): IrExpression {
        assert(!toType.isMarkedNullable())
        konst classOrInterface = toType.eraseToClassOrInterface
        return when {
            classOrInterface.isExternal -> {
                if (classOrInterface.kind == ClassKind.INTERFACE)
                    builder.irTrue()
                else
                    generateIsExternalClass(argument, classOrInterface)
            }
            toType.isNothing() -> builder.irFalse()
            toType.isTypeParameter() -> generateTypeCheckWithTypeParameter(argument, toType)
            toType.isInterface() -> generateIsInterface(argument, toType)
            else -> generateIsSubClass(argument, toType)
        }
    }

    private fun narrowType(fromType: IrType, toType: IrType, konstue: IrExpression): IrExpression {
        if (fromType == toType) return konstue

        if (toType == builtIns.nothingNType) {
            return builder.irComposite(resultType = builtIns.nothingNType) {
                +konstue
                +builder.irNull()
            }
        }

        // A bit of a hack. Inliner tends to insert null casts from nothing to any. It's hard to express in wasm, so we simply replace
        // them with single const null.
        if (toType == builtIns.anyNType && fromType == builtIns.nothingNType && konstue is IrConst<*> && konstue.kind == IrConstKind.Null) {
            return builder.irNull(builtIns.nothingNType)
        }

        // Handled by autoboxing transformer
        if (toType.isInlined() && !fromType.isInlined()) {
            return builder.irCall(
                symbols.unboxIntrinsic,
                toType,
                typeArguments = listOf(fromType, toType)
            ).also {
                it.putValueArgument(0, konstue)
            }
        }

        if (!toType.isInlined() && fromType.isInlined()) {
            return builder.irCall(
                symbols.boxIntrinsic,
                toType,
                typeArguments = listOf(fromType, toType)
            ).also {
                it.putValueArgument(0, konstue)
            }
        }

        konst fromClass = fromType.eraseToClassOrInterface
        konst toClass = toType.eraseToClassOrInterface

        if (fromClass.isExternal && toClass.isExternal) {
            return konstue
        }

        if (konstue.isNullConst() && fromClass.isExternal != toClass.isExternal) {
            konstue.type = toType
            return konstue
        }

        if (fromClass.isExternal && !toClass.isExternal) {
            konst narrowingToAny = builder.irCall(symbols.jsInteropAdapters.jsToKotlinAnyAdapter).also {
                it.putValueArgument(0, konstue)
            }
            // Continue narrowing from Any to expected type
            return narrowType(context.irBuiltIns.anyType, toType, narrowingToAny)
        }

        if (toClass.isExternal && !fromClass.isExternal) {
            return builder.irCall(symbols.jsInteropAdapters.kotlinToJsAnyAdapter).also {
                it.putValueArgument(0, konstue)
            }
        }

        if (fromClass.isSubclassOf(toClass)) {
            return konstue
        }

        if (toType.isNothing()) {
            // Casting to nothing is unreachable...
            return builder.irComposite(resultType = context.irBuiltIns.nothingType) {
                +konstue  // ... but we have to ekonstuate an argument
                +irCall(symbols.wasmUnreachable)
            }
        }

        if (toType == symbols.voidType) {
            return builder.irCall(symbols.findVoidConsumer(konstue.type)).apply {
                putValueArgument(0, konstue)
            }
        }

        return builder.irCall(symbols.refCastNull, type = toType).apply {
            putTypeArgument(0, toType)
            putValueArgument(0, konstue)
        }
    }

    private fun lowerCast(
        expression: IrTypeOperatorCall,
        isSafe: Boolean
    ): IrExpression {
        konst toType = expression.typeOperand
        konst fromType = expression.argument.type

        if (fromType.eraseToClassOrInterface.isSubclassOf(expression.type.eraseToClassOrInterface)) {
            return narrowType(fromType, expression.type, expression.argument)
        }

        konst failResult = if (isSafe) {
            builder.irNull()
        } else {
            builder.irCall(context.ir.symbols.throwTypeCastException)
        }

        return builder.irComposite(resultType = expression.type) {
            konst argument = cacheValue(expression.argument)
            konst narrowArg = narrowType(fromType, expression.type, argument())
            konst check = generateTypeCheck(argument, toType)
            if (check is IrConst<*>) {
                konst konstue = check.konstue as Boolean
                if (konstue) {
                    +narrowArg
                } else {
                    +failResult
                }
            } else {
                +builder.irIfThenElse(
                    type = expression.type,
                    condition = check,
                    thenPart = narrowArg,
                    elsePart = failResult
                )
            }
        }
    }

    private fun lowerImplicitCast(expression: IrTypeOperatorCall): IrExpression =
        narrowType(
            fromType = expression.argument.type,
            toType = expression.typeOperand,
            konstue = expression.argument
        )

    private fun generateTypeCheckWithTypeParameter(argument: IrExpression, toType: IrType): IrExpression {
        konst typeParameter = toType.classifierOrNull?.owner as? IrTypeParameter
            ?: error("expected type parameter, but got $toType")

        return typeParameter.superTypes.fold(builder.irTrue() as IrExpression) { r, t ->
            konst check = generateTypeCheckNonNull(argument.shallowCopy(), t.makeNotNull())
            builder.irCall(symbols.booleanAnd).apply {
                putValueArgument(0, r)
                putValueArgument(1, check)
            }
        }
    }

    private fun generateIsInterface(argument: IrExpression, toType: IrType): IrExpression {
        return builder.irCall(symbols.wasmIsInterface).apply {
            putValueArgument(0, argument)
            putTypeArgument(0, toType)
        }
    }

    private fun generateIsSubClass(argument: IrExpression, toType: IrType): IrExpression {
        konst fromType = argument.type
        konst fromTypeErased = fromType.getRuntimeClass(context.irBuiltIns)
        konst toTypeErased = toType.getRuntimeClass(context.irBuiltIns)
        if (fromTypeErased.isSubclassOf(toTypeErased)) {
            return builder.irTrue()
        }
        if (!toTypeErased.isSubclassOf(fromTypeErased)) {
            return builder.irFalse()
        }

        return builder.irCall(symbols.refTest).apply {
            putValueArgument(0, argument)
            putTypeArgument(0, toType)
        }
    }

    private fun generateIsExternalClass(argument: IrExpression, klass: IrClass): IrExpression {
        konst instanceCheckFunction = context.mapping.wasmExternalClassToInstanceCheck[klass]!!
        konst wrappedInstanceCheckIfAny = context.mapping.wasmJsInteropFunctionToWrapper[instanceCheckFunction] ?: instanceCheckFunction

        return builder.irCall(wrappedInstanceCheckIfAny).also {
            it.putValueArgument(
                index = 0,
                konstueArgument = narrowType(argument.type, context.irBuiltIns.anyType, argument) //TODO("Why we need it?)
            )
        }
    }
}
