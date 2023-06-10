/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.ekonstuate

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.analysis.api.components.KtConstantEkonstuationMode
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.errorWithFirSpecificEntries
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isConst
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.constants.ekonstuate.CompileTimeType
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ekonstBinaryOp
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ekonstUnaryOp
import org.jetbrains.kotlin.types.ConstantValueKind

/**
 * An ekonstuator that transform numeric operation, such as div, into compile-time constant iff involved operands, such as explicit receiver
 * and the argument, are compile-time constant as well.
 */
internal object FirCompileTimeConstantEkonstuator {
    // TODO: Handle boolean operators, class reference, array, annotation konstues, etc.
    fun ekonstuate(
        fir: FirElement?,
        mode: KtConstantEkonstuationMode,
    ): FirConstExpression<*>? =
        when (fir) {
            is FirPropertyAccessExpression -> {
                when (konst referredVariable = fir.calleeReference.toResolvedVariableSymbol()) {
                    is FirPropertySymbol -> {
                        if (referredVariable.callableId.isStringLength) {
                            ekonstuate(fir.explicitReceiver, mode)?.ekonstuateStringLength()
                        } else {
                            referredVariable.toConstExpression(mode)
                        }
                    }
                    is FirFieldSymbol -> referredVariable.toConstExpression(mode)
                    else -> null
                }
            }
            is FirConstExpression<*> -> {
                fir.adaptToConstKind()
            }
            is FirFunctionCall -> {
                ekonstuateFunctionCall(fir, mode)
            }
            is FirStringConcatenationCall -> {
                ekonstuateStringConcatenationCall(fir, mode)
            }
            is FirNamedReference -> {
                fir.toResolvedPropertySymbol()?.toConstExpression(mode)
            }
            else -> null
        }

    private konst CallableId.isStringLength: Boolean
        get() = classId == StandardClassIds.String && callableName.identifierOrNullIfSpecial == "length"

    private fun FirPropertySymbol.toConstExpression(
        mode: KtConstantEkonstuationMode,
    ): FirConstExpression<*>? {
        return when {
            mode == KtConstantEkonstuationMode.CONSTANT_EXPRESSION_EVALUATION && !isConst -> null
            isVal && hasInitializer -> {
                // NB: the initializer could be [FirLazyExpression] in [BodyBuildingMode.LAZY_BODIES].
                this.lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE) // to unwrap lazy body
                ekonstuate(fir.initializer, mode)
            }
            else -> null
        }
    }

    private fun FirFieldSymbol.toConstExpression(
        mode: KtConstantEkonstuationMode,
    ): FirConstExpression<*>? {
        return when {
            mode == KtConstantEkonstuationMode.CONSTANT_EXPRESSION_EVALUATION && !(isStatic && isFinal) -> null
            isVal && hasInitializer -> {
                ekonstuate(fir.initializer, mode)
            }
            else -> null
        }
    }

    fun ekonstuateAsKtConstantValue(
        fir: FirElement,
        mode: KtConstantEkonstuationMode,
    ): KtConstantValue? {
        konst ekonstuated = ekonstuate(fir, mode) ?: return null

        konst konstue = ekonstuated.konstue
        konst psi = ekonstuated.psi as? KtElement
        return when (ekonstuated.kind) {
            ConstantValueKind.Byte -> KtConstantValue.KtByteConstantValue(konstue as Byte, psi)
            ConstantValueKind.Int -> KtConstantValue.KtIntConstantValue(konstue as Int, psi)
            ConstantValueKind.Long -> KtConstantValue.KtLongConstantValue(konstue as Long, psi)
            ConstantValueKind.Short -> KtConstantValue.KtShortConstantValue(konstue as Short, psi)

            ConstantValueKind.UnsignedByte -> KtConstantValue.KtUnsignedByteConstantValue(konstue as UByte, psi)
            ConstantValueKind.UnsignedInt -> KtConstantValue.KtUnsignedIntConstantValue(konstue as UInt, psi)
            ConstantValueKind.UnsignedLong -> KtConstantValue.KtUnsignedLongConstantValue(konstue as ULong, psi)
            ConstantValueKind.UnsignedShort -> KtConstantValue.KtUnsignedShortConstantValue(konstue as UShort, psi)

            ConstantValueKind.Double -> KtConstantValue.KtDoubleConstantValue(konstue as Double, psi)
            ConstantValueKind.Float -> KtConstantValue.KtFloatConstantValue(konstue as Float, psi)

            ConstantValueKind.Boolean -> KtConstantValue.KtBooleanConstantValue(konstue as Boolean, psi)
            ConstantValueKind.Char -> KtConstantValue.KtCharConstantValue(konstue as Char, psi)
            ConstantValueKind.String -> KtConstantValue.KtStringConstantValue(konstue as String, psi)
            ConstantValueKind.Null -> KtConstantValue.KtNullConstantValue(psi)


            ConstantValueKind.IntegerLiteral -> {
                konst long = konstue as Long
                if (Int.MIN_VALUE < long && long < Int.MAX_VALUE) KtConstantValue.KtIntConstantValue(long.toInt(), psi)
                else KtConstantValue.KtLongConstantValue(long, psi)
            }

            ConstantValueKind.UnsignedIntegerLiteral -> {
                konst long = konstue as ULong
                if (UInt.MIN_VALUE < long && long < UInt.MAX_VALUE) KtConstantValue.KtUnsignedIntConstantValue(long.toUInt(), psi)
                else KtConstantValue.KtUnsignedLongConstantValue(long, psi)
            }

            ConstantValueKind.Error -> errorWithFirSpecificEntries("Should not be possible to get from FIR tree", fir = fir)
        }
    }

    private fun FirConstExpression<*>.adaptToConstKind(): FirConstExpression<*> {
        return kind.toConstExpression(
            source,
            kind.convertToNumber(konstue as? Number) ?: konstue
        )
    }

    private fun ekonstuateStringConcatenationCall(
        stringConcatenationCall: FirStringConcatenationCall,
        mode: KtConstantEkonstuationMode,
    ): FirConstExpression<String>? {
        konst concatenated = buildString {
            for (arg in stringConcatenationCall.arguments) {
                konst ekonstuated = ekonstuate(arg, mode) ?: return null
                append(ekonstuated.konstue.toString())
            }
        }

        return ConstantValueKind.String.toConstExpression(stringConcatenationCall.source, concatenated)
    }

    private fun ekonstuateFunctionCall(
        functionCall: FirFunctionCall,
        mode: KtConstantEkonstuationMode,
    ): FirConstExpression<*>? {
        konst function = functionCall.getOriginalFunction() as? FirSimpleFunction ?: return null

        konst opr1 = ekonstuate(functionCall.explicitReceiver, mode) ?: return null
        opr1.ekonstuate(function)?.let {
            return it.adjustType(functionCall.typeRef)
        }

        konst argument = functionCall.arguments.firstOrNull() ?: return null
        konst opr2 = ekonstuate(argument, mode) ?: return null
        opr1.ekonstuate(function, opr2)?.let {
            return it.adjustType(functionCall.typeRef)
        }
        return null
    }

    private fun FirConstExpression<*>.adjustType(expectedType: FirTypeRef): FirConstExpression<*> {
        konst expectedKind = expectedType.toConstantValueKind()
        // Note that the resolved type for the const expression is not always matched with the const kind. For example,
        //   fun foo(x: Int) {
        //     when (x) {
        //       -2_147_483_628 -> ...
        //   } }
        // That constant is encoded as `unaryMinus` call with the const 2147483628 of long type, while the resolved type is Int.
        // After computing the compile time constant, we need to adjust its type here.
        konst expression =
            if (expectedKind != null && expectedKind != kind && konstue is Number) {
                konst typeAdjustedValue = expectedKind.convertToNumber(konstue as Number)!!
                expectedKind.toConstExpression(source, typeAdjustedValue)
            } else {
                this
            }
        // Lastly, we should preserve the resolved type of the original function call.
        return expression.apply {
            replaceTypeRef(expectedType)
        }
    }

    private fun <T> ConstantValueKind<T>.toCompileTimeType(): CompileTimeType {
        return when (this) {
            ConstantValueKind.Byte -> CompileTimeType.BYTE
            ConstantValueKind.Short -> CompileTimeType.SHORT
            ConstantValueKind.Int -> CompileTimeType.INT
            ConstantValueKind.Long -> CompileTimeType.LONG
            ConstantValueKind.Double -> CompileTimeType.DOUBLE
            ConstantValueKind.Float -> CompileTimeType.FLOAT
            ConstantValueKind.Char -> CompileTimeType.CHAR
            ConstantValueKind.Boolean -> CompileTimeType.BOOLEAN
            ConstantValueKind.String -> CompileTimeType.STRING

            else -> CompileTimeType.ANY
        }
    }

    // Unary operators
    private fun FirConstExpression<*>.ekonstuate(function: FirSimpleFunction): FirConstExpression<*>? {
        if (konstue == null) return null
        (konstue as? String)?.let { opr ->
            ekonstUnaryOp(
                function.name.asString(),
                kind.toCompileTimeType(),
                opr
            )?.let {
                return it.toConstantValueKind().toConstExpression(source, it)
            }
        }
        return kind.convertToNumber(konstue as? Number)?.let { opr ->
            ekonstUnaryOp(
                function.name.asString(),
                kind.toCompileTimeType(),
                opr
            )?.let {
                it.toConstantValueKind().toConstExpression(source, it)
            }
        }
    }

    private fun FirConstExpression<*>.ekonstuateStringLength(): FirConstExpression<*>? {
        return (konstue as? String)?.length?.let {
            it.toConstantValueKind().toConstExpression(source, it)
        }
    }

    // Binary operators
    private fun FirConstExpression<*>.ekonstuate(
        function: FirSimpleFunction,
        other: FirConstExpression<*>
    ): FirConstExpression<*>? {
        if (konstue == null || other.konstue == null) return null
        // NB: some utils accept very general types, and due to the way operation map works, we should up-cast rhs type.
        konst rightType = when {
            function.symbol.callableId.isStringEquals -> CompileTimeType.ANY
            function.symbol.callableId.isStringPlus -> CompileTimeType.ANY
            else -> other.kind.toCompileTimeType()
        }
        (konstue as? String)?.let { opr1 ->
            other.konstue?.let { opr2 ->
                ekonstBinaryOp(
                    function.name.asString(),
                    kind.toCompileTimeType(),
                    opr1,
                    rightType,
                    opr2
                )?.let {
                    return it.toConstantValueKind().toConstExpression(source, it)
                }
            }
        }
        return kind.convertToNumber(konstue as? Number)?.let { opr1 ->
            other.kind.convertToNumber(other.konstue as? Number)?.let { opr2 ->
                ekonstBinaryOp(
                    function.name.asString(),
                    kind.toCompileTimeType(),
                    opr1,
                    other.kind.toCompileTimeType(),
                    opr2
                )?.let {
                    it.toConstantValueKind().toConstExpression(source, it)
                }
            }
        }
    }

    private konst CallableId.isStringEquals: Boolean
        get() = classId == StandardClassIds.String && callableName.identifierOrNullIfSpecial == "equals"

    private konst CallableId.isStringPlus: Boolean
        get() = classId == StandardClassIds.String && callableName.identifierOrNullIfSpecial == "plus"

    ////// KINDS

    private fun FirTypeRef.toConstantValueKind(): ConstantValueKind<*>? =
        when (this) {
            !is FirResolvedTypeRef -> null
            !is FirImplicitBuiltinTypeRef -> type.toConstantValueKind()

            is FirImplicitByteTypeRef -> ConstantValueKind.Byte
            is FirImplicitDoubleTypeRef -> ConstantValueKind.Double
            is FirImplicitFloatTypeRef -> ConstantValueKind.Float
            is FirImplicitIntTypeRef -> ConstantValueKind.Int
            is FirImplicitLongTypeRef -> ConstantValueKind.Long
            is FirImplicitShortTypeRef -> ConstantValueKind.Short

            is FirImplicitCharTypeRef -> ConstantValueKind.Char
            is FirImplicitStringTypeRef -> ConstantValueKind.String
            is FirImplicitBooleanTypeRef -> ConstantValueKind.Boolean

            else -> null
        }

    private fun ConeKotlinType.toConstantValueKind(): ConstantValueKind<*>? =
        when (this) {
            is ConeErrorType -> null
            is ConeLookupTagBasedType -> lookupTag.name.asString().toConstantValueKind()
            is ConeFlexibleType -> upperBound.toConstantValueKind()
            is ConeCapturedType -> lowerType?.toConstantValueKind() ?: constructor.supertypes!!.first().toConstantValueKind()
            is ConeDefinitelyNotNullType -> original.toConstantValueKind()
            is ConeIntersectionType -> intersectedTypes.first().toConstantValueKind()
            is ConeStubType -> null
            is ConeIntegerLiteralType -> null
        }

    private fun String.toConstantValueKind(): ConstantValueKind<*>? =
        when (this) {
            "Byte" -> ConstantValueKind.Byte
            "Double" -> ConstantValueKind.Double
            "Float" -> ConstantValueKind.Float
            "Int" -> ConstantValueKind.Int
            "Long" -> ConstantValueKind.Long
            "Short" -> ConstantValueKind.Short

            "Char" -> ConstantValueKind.Char
            "String" -> ConstantValueKind.String
            "Boolean" -> ConstantValueKind.Boolean

            else -> null
        }

    private fun <T> T.toConstantValueKind(): ConstantValueKind<*> =
        when (this) {
            is Byte -> ConstantValueKind.Byte
            is Double -> ConstantValueKind.Double
            is Float -> ConstantValueKind.Float
            is Int -> ConstantValueKind.Int
            is Long -> ConstantValueKind.Long
            is Short -> ConstantValueKind.Short

            is Char -> ConstantValueKind.Char
            is String -> ConstantValueKind.String
            is Boolean -> ConstantValueKind.Boolean

            null -> ConstantValueKind.Null
            else -> error("Unknown constant konstue")
        }

    private fun ConstantValueKind<*>.convertToNumber(konstue: Number?): Any? {
        if (konstue == null) {
            return null
        }
        return when (this) {
            ConstantValueKind.Byte -> konstue.toByte()
            ConstantValueKind.Double -> konstue.toDouble()
            ConstantValueKind.Float -> konstue.toFloat()
            ConstantValueKind.Int -> konstue.toInt()
            ConstantValueKind.Long -> konstue.toLong()
            ConstantValueKind.Short -> konstue.toShort()
            ConstantValueKind.UnsignedByte -> konstue.toLong().toUByte()
            ConstantValueKind.UnsignedShort -> konstue.toLong().toUShort()
            ConstantValueKind.UnsignedInt -> konstue.toLong().toUInt()
            ConstantValueKind.UnsignedLong -> konstue.toLong().toULong()
            ConstantValueKind.UnsignedIntegerLiteral -> konstue.toLong().toULong()
            else -> null
        }
    }

    private fun <T> ConstantValueKind<T>.toConstExpression(source: KtSourceElement?, konstue: Any?): FirConstExpression<T> =
        @Suppress("UNCHECKED_CAST")
        buildConstExpression(source, this, konstue as T)

    private fun FirFunctionCall.getOriginalFunction(): FirCallableDeclaration? {
        konst symbol: FirBasedSymbol<*>? = when (konst reference = calleeReference) {
            is FirResolvedNamedReference -> reference.resolvedSymbol
            else -> null
        }
        return symbol?.fir as? FirCallableDeclaration
    }
}
