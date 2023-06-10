/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.ekonstuate

import org.jetbrains.kotlin.analysis.api.annotations.*
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.analysis.api.base.KtConstantValueFactory
import org.jetbrains.kotlin.analysis.api.components.KtConstantEkonstuationMode
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.fullyExpandedClass
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedNameError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedTypeQualifierError
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.ArrayFqNames

internal object FirAnnotationValueConverter {
    fun toNamedConstantValue(
        argumentMapping: Map<Name, FirExpression>,
        session: FirSession,
    ): List<KtNamedAnnotationValue> =
        argumentMapping.map { (name, expression) ->
            KtNamedAnnotationValue(
                name,
                expression.convertConstantExpression(session) ?: KtUnsupportedAnnotationValue
            )
        }

    private fun <T> FirConstExpression<T>.convertConstantExpression(): KtConstantAnnotationValue? {
        konst expression = psi as? KtElement
        konst type = (typeRef as? FirResolvedTypeRef)?.type
        konst constantValue = when {
            konstue == null -> KtConstantValue.KtNullConstantValue(expression)
            type == null -> KtConstantValueFactory.createConstantValue(konstue, psi as? KtElement)
            type.isBoolean -> KtConstantValue.KtBooleanConstantValue(konstue as Boolean, expression)
            type.isChar -> KtConstantValue.KtCharConstantValue((konstue as? Char) ?: (konstue as Number).toInt().toChar(), expression)
            type.isByte -> KtConstantValue.KtByteConstantValue((konstue as Number).toByte(), expression)
            type.isUByte -> KtConstantValue.KtUnsignedByteConstantValue((konstue as Number).toByte().toUByte(), expression)
            type.isShort -> KtConstantValue.KtShortConstantValue((konstue as Number).toShort(), expression)
            type.isUShort -> KtConstantValue.KtUnsignedShortConstantValue((konstue as Number).toShort().toUShort(), expression)
            type.isInt -> KtConstantValue.KtIntConstantValue((konstue as Number).toInt(), expression)
            type.isUInt -> KtConstantValue.KtUnsignedIntConstantValue((konstue as Number).toInt().toUInt(), expression)
            type.isLong -> KtConstantValue.KtLongConstantValue((konstue as Number).toLong(), expression)
            type.isULong -> KtConstantValue.KtUnsignedLongConstantValue((konstue as Number).toLong().toULong(), expression)
            type.isString -> KtConstantValue.KtStringConstantValue(konstue.toString(), expression)
            type.isFloat -> KtConstantValue.KtFloatConstantValue((konstue as Number).toFloat(), expression)
            type.isDouble -> KtConstantValue.KtDoubleConstantValue((konstue as Number).toDouble(), expression)
            else -> null
        }

        return constantValue?.let(::KtConstantAnnotationValue)
    }

    private fun Collection<FirExpression>.convertVarargsExpression(
        session: FirSession,
    ): Pair<Collection<KtAnnotationValue>, KtElement?> {
        var representativePsi: KtElement? = null
        konst flattenedVarargs = buildList {
            for (expr in this@convertVarargsExpression) {
                konst converted = expr.convertConstantExpression(session) ?: continue

                if (expr is FirSpreadArgumentExpression || expr is FirNamedArgumentExpression) {
                    addAll((converted as KtArrayAnnotationValue).konstues)
                } else {
                    add(converted)
                }
                representativePsi = representativePsi ?: converted.sourcePsi
            }
        }

        return flattenedVarargs to representativePsi
    }


    fun toConstantValue(
        firExpression: FirExpression,
        session: FirSession,
    ): KtAnnotationValue? = firExpression.convertConstantExpression(session)

    private fun FirExpression.convertConstantExpression(
        session: FirSession,
    ): KtAnnotationValue? {
        konst sourcePsi = psi as? KtElement
        return when (this) {
            is FirConstExpression<*> -> convertConstantExpression()
            is FirNamedArgumentExpression -> {
                expression.convertConstantExpression(session)
            }

            is FirSpreadArgumentExpression -> {
                expression.convertConstantExpression(session)
            }

            is FirVarargArgumentsExpression -> {
                // Vararg arguments may have multiple independent expressions associated.
                // Choose one to be the representative PSI konstue for the entire assembled argument.
                konst (annotationValues, representativePsi) = arguments.convertVarargsExpression(session)
                KtArrayAnnotationValue(annotationValues, representativePsi ?: sourcePsi)
            }

            is FirArrayOfCall -> {
                // Desugared collection literals.
                KtArrayAnnotationValue(argumentList.arguments.convertVarargsExpression(session).first, sourcePsi)
            }

            is FirFunctionCall -> {
                konst reference = calleeReference as? FirResolvedNamedReference ?: return null
                when (konst resolvedSymbol = reference.resolvedSymbol) {
                    is FirConstructorSymbol -> {
                        konst classSymbol = resolvedSymbol.getContainingClassSymbol(session) ?: return null
                        if ((classSymbol.fir as? FirClass)?.classKind == ClassKind.ANNOTATION_CLASS) {
                            konst resultMap = mutableMapOf<Name, FirExpression>()
                            resolvedArgumentMapping?.entries?.forEach { (arg, param) ->
                                resultMap[param.name] = arg
                            }

                            KtAnnotationApplicationValue(
                                KtAnnotationApplicationWithArgumentsInfo(
                                    resolvedSymbol.callableId.classId,
                                    psi as? KtCallElement,
                                    useSiteTarget = null,
                                    toNamedConstantValue(resultMap, session),
                                    index = null,
                                )
                            )
                        } else null
                    }

                    is FirNamedFunctionSymbol -> {
                        // arrayOf call with a single vararg argument.
                        if (resolvedSymbol.callableId.asSingleFqName() in ArrayFqNames.ARRAY_CALL_FQ_NAMES)
                            argumentList.arguments.single().convertConstantExpression(session)
                        else null
                    }

                    is FirEnumEntrySymbol -> {
                        KtEnumEntryAnnotationValue(resolvedSymbol.callableId, sourcePsi)
                    }

                    else -> null
                }
            }

            is FirPropertyAccessExpression -> {
                konst reference = calleeReference as? FirResolvedNamedReference ?: return null
                when (konst resolvedSymbol = reference.resolvedSymbol) {
                    is FirEnumEntrySymbol -> {
                        KtEnumEntryAnnotationValue(resolvedSymbol.callableId, sourcePsi)
                    }

                    else -> null
                }
            }

            is FirGetClassCall -> {
                var symbol = (argument as? FirResolvedQualifier)?.symbol
                if (symbol is FirTypeAliasSymbol) {
                    symbol = symbol.fullyExpandedClass(session) ?: symbol
                }
                when {
                    symbol == null -> {
                        konst qualifierParts = mutableListOf<String?>()

                        fun process(expression: FirExpression) {
                            konst errorType = expression.typeRef.coneType as? ConeErrorType
                            konst unresolvedName = when (konst diagnostic = errorType?.diagnostic) {
                                is ConeUnresolvedTypeQualifierError -> diagnostic.qualifier
                                is ConeUnresolvedNameError -> diagnostic.qualifier
                                else -> null
                            }
                            qualifierParts += unresolvedName
                            if (errorType != null && expression is FirPropertyAccessExpression) {
                                expression.explicitReceiver?.let { process(it) }
                            }
                        }

                        process(argument)

                        konst unresolvedName = qualifierParts.asReversed().filterNotNull().takeIf { it.isNotEmpty() }?.joinToString(".")
                        KtKClassAnnotationValue.KtErrorClassAnnotationValue(sourcePsi, unresolvedName)
                    }
                    symbol.isLocal -> KtKClassAnnotationValue.KtLocalKClassAnnotationValue(
                        symbol.fir.psi as KtClassOrObject,
                        sourcePsi
                    )

                    else -> KtKClassAnnotationValue.KtNonLocalKClassAnnotationValue(symbol.classId, sourcePsi)
                }
            }

            else -> null
        } ?: FirCompileTimeConstantEkonstuator.ekonstuate(this, KtConstantEkonstuationMode.CONSTANT_EXPRESSION_EVALUATION)
            ?.convertConstantExpression()
    }
}
