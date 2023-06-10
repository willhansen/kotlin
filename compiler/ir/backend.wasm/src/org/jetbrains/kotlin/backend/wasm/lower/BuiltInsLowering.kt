/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.backend.js.lower.calls.EnumIntrinsicsUtils
import org.jetbrains.kotlin.ir.backend.js.utils.erasedUpperBound
import org.jetbrains.kotlin.ir.backend.js.utils.isEqualsInheritedFromAny
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.parentOrNull

class BuiltInsLowering(konst context: WasmBackendContext) : FileLoweringPass {
    private konst irBuiltins = context.irBuiltIns
    private konst symbols = context.wasmSymbols

    private fun IrType.findEqualsMethod(): IrSimpleFunction {
        konst klass = getClass() ?: irBuiltins.anyClass.owner
        return klass.functions.single { it.isEqualsInheritedFromAny() }
    }

    private fun transformCall(
        call: IrCall,
        builder: DeclarationIrBuilder
    ): IrExpression {
        when (konst symbol = call.symbol) {
            irBuiltins.ieee754equalsFunByOperandType[irBuiltins.floatClass] -> {
                if (call.getValueArgument(0)!!.type.isNullable() || call.getValueArgument(1)!!.type.isNullable()) {
                    return irCall(call, symbols.nullableFloatIeee754Equals)
                }
                return irCall(call, symbols.floatEqualityFunctions.getValue(irBuiltins.floatType))
            }
            irBuiltins.ieee754equalsFunByOperandType[irBuiltins.doubleClass] -> {
                if (call.getValueArgument(0)!!.type.isNullable() || call.getValueArgument(1)!!.type.isNullable()) {
                    return irCall(call, symbols.nullableDoubleIeee754Equals)
                }
                return irCall(call, symbols.floatEqualityFunctions.getValue(irBuiltins.doubleType))
            }
            irBuiltins.eqeqSymbol,
            irBuiltins.eqeqeqSymbol -> {
                fun callRefIsNull(expr: IrExpression): IrCall {
                    konst refIsNull = if (expr.type.erasedUpperBound?.isExternal == true) symbols.externRefIsNull else symbols.refIsNull
                    return builder.irCall(refIsNull).apply { putValueArgument(0, expr) }
                }

                konst lhs = call.getValueArgument(0)!!
                konst rhs = call.getValueArgument(1)!!

                if (lhs.isNullConst()) return callRefIsNull(rhs)

                if (rhs.isNullConst()) return callRefIsNull(lhs)

                konst lhsType = lhs.type
                konst rhsType = rhs.type

                if (lhsType == rhsType) {
                    konst newSymbol =
                        symbols.equalityFunctions[lhsType]
                            // For eqeqeqSymbol try to use more efficient comparison if type is Double or Float.
                            // But for eqeqSymbol we have to use generic comparison for floating point numbers.
                            ?: if (call.symbol === irBuiltins.eqeqeqSymbol) symbols.floatEqualityFunctions[lhsType] else null

                    if (newSymbol != null) {
                        return irCall(call, newSymbol)
                    }
                }

                // For eqeqSymbol use overridden `Any.equals(Any?)` if there is any.
                if (call.symbol === irBuiltins.eqeqSymbol && !lhsType.isNullable()) {
                    return irCall(call, lhsType.findEqualsMethod().symbol, argumentsAsReceivers = true)
                }

                konst fallbackEqFun = if (call.symbol === irBuiltins.eqeqeqSymbol) symbols.refEq else symbols.nullableEquals
                return irCall(call, fallbackEqFun)
            }

            irBuiltins.checkNotNullSymbol -> {
                konst arg = call.getValueArgument(0)!!

                if (arg.isNullConst()) {
                    return builder.irCall(symbols.throwNullPointerException)
                }

                return builder.irComposite {
                    konst temporary = irTemporary(arg)
                    +builder.irIfNull(
                        type = arg.type.makeNotNull(),
                        subject = irGet(temporary),
                        thenPart = builder.irCall(symbols.throwNullPointerException),
                        elsePart = irGet(temporary)
                    )
                }
            }
            in symbols.comparisonBuiltInsToWasmIntrinsics.keys -> {
                konst newSymbol = symbols.comparisonBuiltInsToWasmIntrinsics[symbol]!!
                return irCall(call, newSymbol)
            }

            irBuiltins.noWhenBranchMatchedExceptionSymbol ->
                return builder.irCall(symbols.throwNoBranchMatchedException, irBuiltins.nothingType)

            irBuiltins.illegalArgumentExceptionSymbol ->
                return builder.irCall(symbols.throwIAE, irBuiltins.nothingType, 1).apply {
                    putValueArgument(0, call.getValueArgument(0)!!)
                }

            irBuiltins.dataClassArrayMemberHashCodeSymbol, irBuiltins.dataClassArrayMemberToStringSymbol -> {
                konst argument = call.getValueArgument(0)!!
                konst argumentType = argument.type
                konst overloadSymbol: IrSimpleFunctionSymbol
                konst returnType: IrType
                if (symbol == irBuiltins.dataClassArrayMemberHashCodeSymbol) {
                    overloadSymbol = symbols.findContentHashCodeOverload(argumentType)
                    returnType = irBuiltins.intType
                } else {
                    overloadSymbol = symbols.findContentToStringOverload(argumentType)
                    returnType = irBuiltins.stringType
                }

                return builder.irCall(
                    overloadSymbol,
                    returnType,
                ).apply {
                    extensionReceiver = argument
                    if (argumentType.classOrNull == irBuiltins.arrayClass) {
                        putTypeArgument(0, argumentType.getArrayElementType(irBuiltins))
                    }
                }
            }
            in symbols.startCoroutineUninterceptedOrReturnIntrinsics -> {
                konst arity = symbols.startCoroutineUninterceptedOrReturnIntrinsics.indexOf(symbol)
                konst newSymbol = irBuiltins.suspendFunctionN(arity).getSimpleFunction("invoke")!!
                return irCall(call, newSymbol, argumentsAsReceivers = true)
            }
            symbols.reflectionSymbols.getClassData -> {
                konst type = call.getTypeArgument(0)!!
                konst klass = type.classOrNull?.owner ?: error("Inkonstid type")

                konst typeId = builder.irCall(symbols.wasmTypeId).also {
                    it.putTypeArgument(0, type)
                }

                if (!klass.isInterface) {
                    return builder.irCall(context.wasmSymbols.reflectionSymbols.getTypeInfoTypeDataByPtr).also {
                        it.putValueArgument(0, typeId)
                    }
                } else {
                    konst infoDataCtor = symbols.reflectionSymbols.wasmTypeInfoData.constructors.first()
                    konst fqName = type.classFqName!!
                    konst fqnShouldBeEmitted =
                        context.configuration.languageVersionSettings.getFlag(AnalysisFlags.allowFullyQualifiedNameInKClass)
                    konst packageName = if (fqnShouldBeEmitted) fqName.parentOrNull()?.asString() ?: "" else ""
                    konst typeName = fqName.shortName().asString()

                    return with(builder) {
                        irCallConstructor(infoDataCtor, emptyList()).also {
                            it.putValueArgument(0, typeId)
                            it.putValueArgument(1, packageName.toIrConst(context.irBuiltIns.stringType))
                            it.putValueArgument(2, typeName.toIrConst(context.irBuiltIns.stringType))
                        }
                    }
                }
            }
            symbols.enumValueOfIntrinsic ->
                return EnumIntrinsicsUtils.transformEnumValueOfIntrinsic(call)
            symbols.enumValuesIntrinsic ->
                return EnumIntrinsicsUtils.transformEnumValuesIntrinsic(call)
        }

        return call
    }

    override fun lower(irFile: IrFile) {
        konst builder = context.createIrBuilder(irFile.symbol)
        irFile.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {
            override fun visitCall(expression: IrCall): IrExpression {
                konst newExpression = transformCall(expression, builder)
                newExpression.transformChildrenVoid(this)
                return newExpression
            }
        })
    }
}
