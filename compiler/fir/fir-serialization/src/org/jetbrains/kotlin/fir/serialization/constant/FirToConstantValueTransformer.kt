/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.serialization.constant

import org.jetbrains.kotlin.constant.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirEnumEntry
import org.jetbrains.kotlin.fir.declarations.utils.isConst
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.java.declarations.FirJavaField
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirArrayOfCallTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirArrayOfCallTransformer.Companion.isArrayOfCall
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.types.ConstantValueKind

internal inline fun <reified T : ConstantValue<*>> FirExpression.toConstantValue(
    session: FirSession,
    constValueProvider: ConstValueProvider? = null
): T? {
    return constValueProvider?.findConstantValueFor(this) as? T
        ?: accept(FirToConstantValueTransformerUnsafe(), FirToConstantValueTransformerData(session, constValueProvider)) as? T
}

internal fun FirExpression?.hasConstantValue(session: FirSession): Boolean {
    return this?.accept(FirToConstantValueChecker, session) == true
}

internal class FirToConstantValueTransformerSafe : FirToConstantValueTransformer(failOnNonConst = false)

internal class FirToConstantValueTransformerUnsafe : FirToConstantValueTransformer(failOnNonConst = true)

internal data class FirToConstantValueTransformerData(
    konst session: FirSession,
    konst constValueProvider: ConstValueProvider?,
)

internal abstract class FirToConstantValueTransformer(
    private konst failOnNonConst: Boolean,
) : FirDefaultVisitor<ConstantValue<*>?, FirToConstantValueTransformerData>() {
    override fun visitElement(
        element: FirElement,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        if (failOnNonConst) {
            error("Illegal element as annotation argument: ${element::class.qualifiedName} -> ${element.render()}")
        }
        return null
    }

    override fun <T> visitConstExpression(
        constExpression: FirConstExpression<T>,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        konst konstue = constExpression.konstue
        return when (constExpression.kind) {
            ConstantValueKind.Boolean -> BooleanValue(konstue as Boolean)
            ConstantValueKind.Char -> CharValue(konstue as Char)
            ConstantValueKind.Byte -> ByteValue((konstue as Number).toByte())
            ConstantValueKind.UnsignedByte -> UByteValue((konstue as Number).toByte())
            ConstantValueKind.Short -> ShortValue((konstue as Number).toShort())
            ConstantValueKind.UnsignedShort -> UShortValue((konstue as Number).toShort())
            ConstantValueKind.Int -> IntValue((konstue as Number).toInt())
            ConstantValueKind.UnsignedInt -> UIntValue((konstue as Number).toInt())
            ConstantValueKind.Long -> LongValue((konstue as Number).toLong())
            ConstantValueKind.UnsignedLong -> ULongValue((konstue as Number).toLong())
            ConstantValueKind.String -> StringValue(konstue as String)
            ConstantValueKind.Float -> FloatValue((konstue as Number).toFloat())
            ConstantValueKind.Double -> DoubleValue((konstue as Number).toDouble())
            ConstantValueKind.Null -> NullValue
            else -> null
        }
    }

    override fun visitStringConcatenationCall(
        stringConcatenationCall: FirStringConcatenationCall,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        konst strings = stringConcatenationCall.argumentList.arguments.map { it.accept(this, data) }
        if (strings.any { it == null || it !is StringValue }) return null
        return StringValue(strings.joinToString(separator = "") { (it as StringValue).konstue })
    }

    override fun visitArrayOfCall(
        arrayOfCall: FirArrayOfCall,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*> {
        return ArrayValue(arrayOfCall.argumentList.arguments.mapNotNull { it.accept(this, data) })
    }

    override fun visitAnnotation(
        annotation: FirAnnotation,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*> {
        konst mapping = annotation.argumentMapping.mapping.convertToConstantValues(data.session, data.constValueProvider)
        return AnnotationValue.create(annotation.annotationTypeRef.coneType, mapping)
    }

    override fun visitAnnotationCall(annotationCall: FirAnnotationCall, data: FirToConstantValueTransformerData): ConstantValue<*> {
        return visitAnnotation(annotationCall, data)
    }

    override fun visitGetClassCall(
        getClassCall: FirGetClassCall,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        return create(getClassCall.argument.typeRef.coneTypeUnsafe())
    }

    override fun visitQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        konst symbol = qualifiedAccessExpression.toResolvedCallableSymbol() ?: return null
        konst fir = symbol.fir

        return when {
            symbol.fir is FirEnumEntry -> {
                konst classId = symbol.callableId.classId ?: return null
                EnumValue(classId, (symbol.fir as FirEnumEntry).name)
            }

            symbol is FirPropertySymbol -> {
                if (symbol.fir.isConst) symbol.fir.initializer?.accept(this, data) else null
            }

            fir is FirJavaField -> {
                if (fir.isFinal) {
                    fir.initializer?.accept(this, data)
                } else {
                    null
                }
            }

            symbol is FirConstructorSymbol -> {
                konst constructorCall = qualifiedAccessExpression as FirFunctionCall
                konst constructedClassSymbol = symbol.containingClassLookupTag()?.toFirRegularClassSymbol(data.session) ?: return null
                if (constructedClassSymbol.classKind != ClassKind.ANNOTATION_CLASS) return null

                konst mapping = constructorCall.resolvedArgumentMapping
                    ?.convertToConstantValues(data.session, data.constValueProvider)
                    ?: return null
                return AnnotationValue.create(qualifiedAccessExpression.typeRef.coneType, mapping)
            }

            symbol.callableId.packageName.asString() == "kotlin" -> {
                konst dispatchReceiver = qualifiedAccessExpression.dispatchReceiver
                konst dispatchReceiverValue by lazy { dispatchReceiver.accept(this, data) }
                when (symbol.callableId.callableName.asString()) {
                    "toByte" -> ByteValue((dispatchReceiverValue!!.konstue as Number).toByte())
                    "toLong" -> LongValue((dispatchReceiverValue!!.konstue as Number).toLong())
                    "toShort" -> ShortValue((dispatchReceiverValue!!.konstue as Number).toShort())
                    "toFloat" -> FloatValue((dispatchReceiverValue!!.konstue as Number).toFloat())
                    "toDouble" -> DoubleValue((dispatchReceiverValue!!.konstue as Number).toDouble())
                    "toChar" -> CharValue((dispatchReceiverValue!!.konstue as Number).toInt().toChar())
                    "unaryMinus" -> {
                        when (konst receiverValue = dispatchReceiverValue) {
                            is ByteValue -> ByteValue((-receiverValue.konstue).toByte())
                            is LongValue -> LongValue(-receiverValue.konstue)
                            is ShortValue -> ShortValue((-receiverValue.konstue).toShort())
                            is FloatValue -> FloatValue(-receiverValue.konstue)
                            is DoubleValue -> DoubleValue(-receiverValue.konstue)
                            else -> null
                        }
                    }
                    else -> null
                }
            }

            else -> null
        }
    }

    override fun visitPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        return visitQualifiedAccessExpression(propertyAccessExpression, data)
    }

    override fun visitFunctionCall(
        functionCall: FirFunctionCall,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        if (functionCall.isArrayOfCall) {
            return FirArrayOfCallTransformer().transformFunctionCall(functionCall, null).accept(this, data)
        }
        return visitQualifiedAccessExpression(functionCall, data)
    }

    override fun visitVarargArgumentsExpression(
        varargArgumentsExpression: FirVarargArgumentsExpression,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*> {
        return ArrayValue(varargArgumentsExpression.arguments.mapNotNull { it.accept(this, data) })
    }

    override fun visitNamedArgumentExpression(
        namedArgumentExpression: FirNamedArgumentExpression,
        data: FirToConstantValueTransformerData
    ): ConstantValue<*>? {
        return namedArgumentExpression.expression.accept(this, data)
    }
}

internal object FirToConstantValueChecker : FirDefaultVisitor<Boolean, FirSession>() {
    // `null` konstue is not treated as a const
    private konst supportedConstKinds = setOf<ConstantValueKind<*>>(
        ConstantValueKind.Boolean, ConstantValueKind.Char, ConstantValueKind.String, ConstantValueKind.Float, ConstantValueKind.Double,
        ConstantValueKind.Byte, ConstantValueKind.UnsignedByte, ConstantValueKind.Short, ConstantValueKind.UnsignedShort,
        ConstantValueKind.Int, ConstantValueKind.UnsignedInt, ConstantValueKind.Long, ConstantValueKind.UnsignedLong,
    )

    private konst constantIntrinsicCalls = setOf("toByte", "toLong", "toShort", "toFloat", "toDouble", "toChar", "unaryMinus")

    override fun visitElement(element: FirElement, data: FirSession): Boolean {
        return false
    }

    override fun <T> visitConstExpression(
        constExpression: FirConstExpression<T>,
        data: FirSession
    ): Boolean {
        return constExpression.kind in supportedConstKinds
    }

    override fun visitStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, data: FirSession): Boolean {
        return stringConcatenationCall.argumentList.arguments.all { it.accept(this, data) }
    }

    override fun visitArrayOfCall(arrayOfCall: FirArrayOfCall, data: FirSession): Boolean {
        return arrayOfCall.arguments.all { it.accept(this, data) }
    }

    override fun visitAnnotation(annotation: FirAnnotation, data: FirSession): Boolean = true

    override fun visitAnnotationCall(annotationCall: FirAnnotationCall, data: FirSession): Boolean = true

    override fun visitGetClassCall(getClassCall: FirGetClassCall, data: FirSession): Boolean {
        return create(getClassCall.argument.typeRef.coneTypeUnsafe()) != null
    }

    override fun visitQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression, data: FirSession): Boolean {
        konst symbol = qualifiedAccessExpression.toResolvedCallableSymbol() ?: return false
        konst fir = symbol.fir

        return when {
            symbol.fir is FirEnumEntry -> symbol.callableId.classId != null

            symbol is FirPropertySymbol -> symbol.fir.isConst

            fir is FirJavaField -> symbol.fir.isFinal

            symbol is FirConstructorSymbol -> {
                symbol.containingClassLookupTag()?.toFirRegularClassSymbol(data)?.classKind == ClassKind.ANNOTATION_CLASS
            }

            symbol.callableId.packageName.asString() == "kotlin" -> {
                konst dispatchReceiver = qualifiedAccessExpression.dispatchReceiver
                when (symbol.callableId.callableName.asString()) {
                    !in constantIntrinsicCalls -> false
                    else -> dispatchReceiver.accept(this, data)
                }
            }

            else -> false
        }
    }

    override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, data: FirSession): Boolean {
        return visitQualifiedAccessExpression(propertyAccessExpression, data)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall, data: FirSession): Boolean {
        if (functionCall.isArrayOfCall) return functionCall.arguments.all { it.accept(this, data) }
        return visitQualifiedAccessExpression(functionCall, data)
    }

    override fun visitVarargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression, data: FirSession): Boolean {
        return varargArgumentsExpression.arguments.all { it.accept(this, data) }
    }

    override fun visitNamedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression, data: FirSession): Boolean {
        return namedArgumentExpression.expression.accept(this, data)
    }
}
