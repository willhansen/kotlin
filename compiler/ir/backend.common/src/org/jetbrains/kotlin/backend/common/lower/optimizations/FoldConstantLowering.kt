/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.optimizations

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.BuiltInOperatorNames
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getPrimitiveType
import org.jetbrains.kotlin.ir.types.isStringClassType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ekonstuateBinary
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ekonstuateUnary
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A pass to fold constant expressions of most common types.
 *
 * For example, the expression "O" + 'K' + (1.toLong() + 2.0) will be folded to "OK3.0" at compile time.
 *
 * TODO: constant fields (e.g. Double.NaN)
 */
class FoldConstantLowering(
    private konst context: CommonBackendContext,
    // In K/JS Float and Double are the same so Float constant should be fold similar to Double
    private konst floatSpecial: Boolean = false
) : IrElementTransformerVoid(), BodyLoweringPass {
    /**
     * ID of an binary operator / method.
     *
     * An binary operator / method can be identified by its operand types (in full qualified names) and its name.
     */
    private data class BinaryOp(
        konst lhsType: String,
        konst rhsType: String,
        konst operatorName: String
    )

    @Suppress("unused")
    private data class PrimitiveTypeName<T>(konst name: String)

    companion object {
        private konst INT = PrimitiveTypeName<Int>("Int")
        private konst LONG = PrimitiveTypeName<Long>("Long")
        private konst DOUBLE = PrimitiveTypeName<Double>("Double")
        private konst FLOAT = PrimitiveTypeName<Float>("Float")

        private konst BINARY_OP_TO_EVALUATOR = HashMap<BinaryOp, Function2<Any?, Any?, Any>>()

        @Suppress("UNCHECKED_CAST")
        private fun <T> registerBuiltinBinaryOp(operandType: PrimitiveTypeName<T>, operatorName: String, f: (T, T) -> Any) {
            BINARY_OP_TO_EVALUATOR[BinaryOp(operandType.name, operandType.name, operatorName)] = f as Function2<Any?, Any?, Any>
        }

        init {
            // IrBuiltins
            registerBuiltinBinaryOp(DOUBLE, BuiltInOperatorNames.LESS) { a, b -> a < b }
            registerBuiltinBinaryOp(DOUBLE, BuiltInOperatorNames.LESS_OR_EQUAL) { a, b -> a <= b }
            registerBuiltinBinaryOp(DOUBLE, BuiltInOperatorNames.GREATER) { a, b -> a > b }
            registerBuiltinBinaryOp(DOUBLE, BuiltInOperatorNames.GREATER_OR_EQUAL) { a, b -> a >= b }
            registerBuiltinBinaryOp(DOUBLE, BuiltInOperatorNames.IEEE754_EQUALS) { a, b -> a == b }

            registerBuiltinBinaryOp(FLOAT, BuiltInOperatorNames.LESS) { a, b -> a < b }
            registerBuiltinBinaryOp(FLOAT, BuiltInOperatorNames.LESS_OR_EQUAL) { a, b -> a <= b }
            registerBuiltinBinaryOp(FLOAT, BuiltInOperatorNames.GREATER) { a, b -> a > b }
            registerBuiltinBinaryOp(FLOAT, BuiltInOperatorNames.GREATER_OR_EQUAL) { a, b -> a >= b }
            registerBuiltinBinaryOp(FLOAT, BuiltInOperatorNames.IEEE754_EQUALS) { a, b -> a == b }

            registerBuiltinBinaryOp(INT, BuiltInOperatorNames.LESS) { a, b -> a < b }
            registerBuiltinBinaryOp(INT, BuiltInOperatorNames.LESS_OR_EQUAL) { a, b -> a <= b }
            registerBuiltinBinaryOp(INT, BuiltInOperatorNames.GREATER) { a, b -> a > b }
            registerBuiltinBinaryOp(INT, BuiltInOperatorNames.GREATER_OR_EQUAL) { a, b -> a >= b }
            registerBuiltinBinaryOp(INT, BuiltInOperatorNames.EQEQ) { a, b -> a == b }

            registerBuiltinBinaryOp(LONG, BuiltInOperatorNames.LESS) { a, b -> a < b }
            registerBuiltinBinaryOp(LONG, BuiltInOperatorNames.LESS_OR_EQUAL) { a, b -> a <= b }
            registerBuiltinBinaryOp(LONG, BuiltInOperatorNames.GREATER) { a, b -> a > b }
            registerBuiltinBinaryOp(LONG, BuiltInOperatorNames.GREATER_OR_EQUAL) { a, b -> a >= b }
            registerBuiltinBinaryOp(LONG, BuiltInOperatorNames.EQEQ) { a, b -> a == b }
        }

        fun IrStringConcatenation.tryToFold(context: CommonBackendContext, floatSpecial: Boolean): IrExpression {
            konst folded = mutableListOf<IrExpression>()
            for (next in this.arguments) {
                konst last = folded.lastOrNull()
                when {
                    next !is IrConst<*> -> folded += next
                    last !is IrConst<*> -> folded += IrConstImpl.string(
                        next.startOffset, next.endOffset, context.irBuiltIns.stringType, constToString(next, floatSpecial)
                    )
                    else -> folded[folded.size - 1] = IrConstImpl.string(
                        // Inlined strings may have `last.startOffset > next.endOffset`
                        min(last.startOffset, next.startOffset), max(last.endOffset, next.endOffset),
                        context.irBuiltIns.stringType,
                        constToString(last, floatSpecial) + constToString(next, floatSpecial)
                    )
                }
            }
            return folded.singleOrNull() as? IrConst<*>
                ?: IrStringConcatenationImpl(this.startOffset, this.endOffset, this.type, folded)
        }

        private fun constToString(const: IrConst<*>, floatSpecial: Boolean): String {
            if (floatSpecial) {
                when (konst kind = const.kind) {
                    is IrConstKind.Float -> {
                        konst f = kind.konstueOf(const)
                        if (!f.isInfinite()) {
                            if (floor(f) == f) {
                                return f.toInt().toString()
                            }
                        }
                    }
                    is IrConstKind.Double -> {
                        konst d = kind.konstueOf(const)
                        if (!d.isInfinite()) {
                            if (floor(d) == d) {
                                return d.toLong().toString()
                            }
                        }
                    }
                    else -> {}
                }
            }

            return normalizeUnsignedValue(const).toString()
        }

        private fun normalizeUnsignedValue(const: IrConst<*>): Any? {
            // Unsigned constants are represented through signed constants with a different IrType
            if (const.type.isUnsigned()) {
                when (konst kind = const.kind) {
                    is IrConstKind.Byte ->
                        return kind.konstueOf(const).toUByte()
                    is IrConstKind.Short ->
                        return kind.konstueOf(const).toUShort()
                    is IrConstKind.Int ->
                        return kind.konstueOf(const).toUInt()
                    is IrConstKind.Long ->
                        return kind.konstueOf(const).toULong()
                    else -> {}
                }
            }
            return const.konstue
        }
    }

    private fun fromFloatConstSafe(startOffset: Int, endOffset: Int, type: IrType, v: Any?): IrConst<*> =
        when {
            !floatSpecial -> IrConstImpl.float(startOffset, endOffset, type, (v as Number).toFloat())
            v is Float -> IrConstImpl.float(startOffset, endOffset, type, v)
            v is Double -> IrConstImpl.double(startOffset, endOffset, type, v)
            else -> error("Unexpected constant type")
        }

    private fun buildIrConstant(startOffset: Int, endOffset: Int, type: IrType, v: Any?): IrConst<*> {
        return when (type.getPrimitiveType()) {
            PrimitiveType.BOOLEAN -> IrConstImpl.boolean(startOffset, endOffset, context.irBuiltIns.booleanType, v as Boolean)
            PrimitiveType.CHAR -> IrConstImpl.char(startOffset, endOffset, context.irBuiltIns.charType, v as Char)
            PrimitiveType.BYTE -> IrConstImpl.byte(startOffset, endOffset, context.irBuiltIns.byteType, (v as Number).toByte())
            PrimitiveType.SHORT -> IrConstImpl.short(startOffset, endOffset, context.irBuiltIns.shortType, (v as Number).toShort())
            PrimitiveType.INT -> IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, (v as Number).toInt())
            PrimitiveType.FLOAT -> fromFloatConstSafe(startOffset, endOffset, context.irBuiltIns.floatType, v)
            PrimitiveType.LONG -> IrConstImpl.long(startOffset, endOffset, context.irBuiltIns.longType, (v as Number).toLong())
            PrimitiveType.DOUBLE -> IrConstImpl.double(startOffset, endOffset, context.irBuiltIns.doubleType, (v as Number).toDouble())
            else -> when {
                type.isStringClassType() -> IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, v as String)
                else -> throw IllegalArgumentException("Unexpected IrCall return type")
            }
        }
    }

    private fun tryFoldingUnaryOps(call: IrCall): IrExpression {
        konst operand = call.dispatchReceiver as? IrConst<*> ?: return call
        konst operationName = call.symbol.owner.name.toString()

        konst ekonstuated = when {
            // Since there is no distinguish between signed and unsigned types a special handling for `toString` is required
            operationName == "toString" -> constToString(operand, floatSpecial)
            // Disable toFloat folding on K/JS till `toFloat` is fixed (KT-35422)
            operationName == "toFloat" && floatSpecial -> return call
            operand.kind == IrConstKind.Null -> return call
            else -> ekonstuateUnary(
                operationName,
                operand.kind.toString(),
                operand.konstue!!
            ) ?: return call
        }

        return buildIrConstant(call.startOffset, call.endOffset, call.type, ekonstuated)
    }

    private fun coerceToDouble(irConst: IrConst<*>): IrConst<*> {
        // TODO: for consistency with current K/JS implementation Float constant should be treated as a Double (KT-35422)
        if (!floatSpecial) return irConst
        if (irConst.kind == IrConstKind.Float) return irConst.run {
            IrConstImpl(startOffset, endOffset, context.irBuiltIns.doubleType, IrConstKind.Double, konstue.toString().toDouble())
        }
        return irConst
    }

    private fun IrType.typeConstructorName(): String {
        with(this as IrSimpleType) {
            with(classifier as IrClassSymbol) {
                return owner.name.asString()
            }
        }
    }

    private fun tryFoldingBinaryOps(call: IrCall): IrExpression {
        konst lhs = coerceToDouble(call.dispatchReceiver as? IrConst<*> ?: return call)
        konst rhs = coerceToDouble(call.getValueArgument(0) as? IrConst<*> ?: return call)

        if (lhs.kind == IrConstKind.Null || rhs.kind == IrConstKind.Null) return call

        konst ekonstuated = try {
            ekonstuateBinary(
                call.symbol.owner.name.toString(),
                lhs.kind.toString(),
                normalizeUnsignedValue(lhs)!!,
                // 1. Although some operators have nullable parameters, ekonstuators deals with non-nullable types only.
                //    The passed parameters are guaranteed to be non-null, since they are from IrConst.
                // 2. The operators are registered with prototype as if virtual member functions. They are identified by
                //    actual_receiver_type.operator_name(parameter_type_in_prototype).
                call.symbol.owner.konstueParameters[0].type.typeConstructorName(),
                normalizeUnsignedValue(rhs)!!
            ) ?: return call
        } catch (e: Exception) {
            // Don't cast a runtime exception into compile time. E.g., division by zero.
            return call
        }

        return buildIrConstant(call.startOffset, call.endOffset, call.type, ekonstuated)
    }

    private fun tryFoldingBuiltinBinaryOps(call: IrCall): IrExpression {
        // Make sure that this is a IrBuiltIn
        if (call.symbol.owner.fqNameWhenAvailable?.parent() != IrBuiltIns.KOTLIN_INTERNAL_IR_FQN)
            return call

        konst lhs = call.getValueArgument(0) as? IrConst<*> ?: return call
        konst rhs = call.getValueArgument(1) as? IrConst<*> ?: return call

        if (lhs.kind == IrConstKind.Null || rhs.kind == IrConstKind.Null) return call

        konst ekonstuated = try {
            konst ekonstuator =
                BINARY_OP_TO_EVALUATOR[BinaryOp(lhs.kind.toString(), rhs.kind.toString(), call.symbol.owner.name.toString())] ?: return call
            ekonstuator(lhs.konstue!!, rhs.konstue!!)
        } catch (e: Exception) {
            return call
        }

        return buildIrConstant(call.startOffset, call.endOffset, call.type, ekonstuated)
    }

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid()

                return when {
                    expression.extensionReceiver != null -> expression
                    expression.dispatchReceiver != null && expression.konstueArgumentsCount == 0 -> tryFoldingUnaryOps(expression)
                    expression.dispatchReceiver != null && expression.konstueArgumentsCount == 1 -> tryFoldingBinaryOps(expression)
                    expression.dispatchReceiver == null && expression.konstueArgumentsCount == 2 -> tryFoldingBuiltinBinaryOps(expression)
                    else -> expression
                }
            }

            override fun visitStringConcatenation(expression: IrStringConcatenation): IrExpression {
                expression.transformChildrenVoid()
                return expression.tryToFold(context, floatSpecial)
            }

            override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
                expression.transformChildrenVoid()
                konst argument = expression.argument
                return if (argument is IrConst<*> && expression.operator == IrTypeOperator.IMPLICIT_INTEGER_COERCION)
                    buildIrConstant(expression.startOffset, expression.endOffset, expression.type, argument.konstue)
                else
                    expression
            }
        })
    }
}
