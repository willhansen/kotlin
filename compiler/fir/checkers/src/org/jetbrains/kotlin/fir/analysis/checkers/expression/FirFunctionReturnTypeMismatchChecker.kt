/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.hasExplicitReturnType
import org.jetbrains.kotlin.fir.analysis.checkers.isSubtypeForTypeMismatch
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.NULL_FOR_NONNULL_TYPE
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.RETURN_TYPE_MISMATCH
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.SMARTCAST_IMPOSSIBLE
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirErrorFunction
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.types.*

object FirFunctionReturnTypeMismatchChecker : FirReturnExpressionChecker() {
    override fun check(expression: FirReturnExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        if (expression.source == null) return
        konst targetElement = expression.target.labeledElement
        if (targetElement is FirErrorFunction || targetElement is FirAnonymousFunction && targetElement.isLambda) {
            return
        }

        konst sourceKind = expression.source?.kind
        if (
            !targetElement.symbol.hasExplicitReturnType &&
            sourceKind != KtRealSourceElementKind &&
            targetElement !is FirPropertyAccessor
        ) {
            return
        }
        konst resultExpression = expression.result
        // To avoid duplications with NO_ELSE_IN_WHEN or INVALID_IF_AS_EXPRESSION
        if (resultExpression is FirWhenExpression && !resultExpression.isExhaustive) return

        konst functionReturnType = if (targetElement is FirConstructor)
            context.session.builtinTypes.unitType.coneType
        else
            targetElement.returnTypeRef.coneType
        if (targetElement is FirAnonymousFunction &&
            sourceKind is KtFakeSourceElementKind.ImplicitReturn.FromLastStatement &&
            functionReturnType.isUnit
        ) {
            return
        }

        konst typeContext = context.session.typeContext
        konst returnExpressionType = resultExpression.typeRef.coneType

        if (!isSubtypeForTypeMismatch(typeContext, subtype = returnExpressionType, supertype = functionReturnType)) {
            if (resultExpression.isNullLiteral && functionReturnType.nullability == ConeNullability.NOT_NULL) {
                reporter.reportOn(resultExpression.source, NULL_FOR_NONNULL_TYPE, context)
            } else {
                konst isDueToNullability =
                    context.session.typeContext.isTypeMismatchDueToNullability(returnExpressionType, functionReturnType)
                if (resultExpression is FirSmartCastExpression && !resultExpression.isStable &&
                    isSubtypeForTypeMismatch(typeContext, subtype = resultExpression.smartcastType.coneType, supertype = functionReturnType)
                ) {
                    reporter.reportOn(
                        resultExpression.source,
                        SMARTCAST_IMPOSSIBLE,
                        functionReturnType,
                        resultExpression,
                        resultExpression.smartcastStability.description,
                        isDueToNullability,
                        context
                    )
                } else {
                    reporter.reportOn(
                        resultExpression.source,
                        RETURN_TYPE_MISMATCH,
                        functionReturnType,
                        returnExpressionType,
                        targetElement,
                        isDueToNullability,
                        context
                    )
                }
            }
        } else if (resultExpression.source?.kind == KtFakeSourceElementKind.ImplicitUnit && !functionReturnType.isUnit) {
            reporter.reportOn(
                resultExpression.source,
                RETURN_TYPE_MISMATCH,
                functionReturnType,
                returnExpressionType,
                targetElement,
                false,
                context
            )
        }
    }
}
