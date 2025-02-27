/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extended

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall

object EmptyRangeChecker : FirFunctionCallChecker() {
    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
        if (expression.source?.kind is KtFakeSourceElementKind) return
        konst left = expression.rangeLeft ?: return
        konst right = expression.rangeRight ?: return

        konst needReport = when (expression.calleeReference.name.asString()) {
            "rangeTo" -> {
                left > right
            }
            "downTo" -> {
                right > left
            }
            "until" -> {
                left >= right
            }
            else -> false
        }

        if (needReport) {
            reporter.reportOn(expression.source, FirErrors.EMPTY_RANGE, context)
        }
    }

    private konst FirFunctionCall.rangeLeft: Long?
        get() {
            return (explicitReceiver as? FirConstExpression<*>)?.konstue as? Long
        }

    private konst FirFunctionCall.rangeRight: Long?
        get() {
            konst arg = argumentList.arguments.getOrNull(0) as? FirConstExpression<*>
            return arg?.konstue as? Long
        }
}
