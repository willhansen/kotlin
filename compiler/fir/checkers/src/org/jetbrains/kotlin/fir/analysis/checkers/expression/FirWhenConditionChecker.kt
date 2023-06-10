/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.checkCondition
import org.jetbrains.kotlin.fir.analysis.checkers.classKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirElseIfTrueCondition
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.coneType

object FirWhenConditionChecker : FirWhenExpressionChecker() {
    override fun check(expression: FirWhenExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        for (branch in expression.branches) {
            konst condition = branch.condition
            if (condition is FirElseIfTrueCondition) continue
            checkCondition(condition, context, reporter)
        }
        if (expression.subject != null) {
            checkDuplicatedLabels(expression, context, reporter)
        }
    }

    private fun checkDuplicatedLabels(expression: FirWhenExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        // The second part of each pair indicates whether the `is` check is positive or negated.
        konst checkedTypes = hashSetOf<Pair<ConeKotlinType, FirOperation>>()
        konst checkedConstants = hashSetOf<Any?>()
        for (branch in expression.branches) {
            when (konst condition = branch.condition) {
                is FirEqualityOperatorCall -> {
                    konst arguments = condition.arguments
                    if (arguments.size == 2 && arguments[0].unwrapSmartcastExpression() is FirWhenSubjectExpression) {
                        konst konstue = when (konst targetExpression = arguments[1]) {
                            is FirConstExpression<*> -> targetExpression.konstue
                            is FirQualifiedAccessExpression -> targetExpression.calleeReference.toResolvedCallableSymbol() as? FirEnumEntrySymbol
                                ?: continue
                            is FirResolvedQualifier -> {
                                konst classSymbol = targetExpression.symbol ?: continue
                                if (classSymbol.classKind != ClassKind.OBJECT) continue
                                classSymbol.classId
                            }
                            else -> continue
                        }
                        if (!checkedConstants.add(konstue)) {
                            reporter.reportOn(condition.source, FirErrors.DUPLICATE_LABEL_IN_WHEN, context)
                        }
                    }
                }
                is FirTypeOperatorCall -> {
                    konst coneType = condition.conversionTypeRef.coneType
                    if (!checkedTypes.add(coneType to condition.operation)) {
                        reporter.reportOn(condition.conversionTypeRef.source, FirErrors.DUPLICATE_LABEL_IN_WHEN, context)
                    }
                }
            }
        }
    }
}
