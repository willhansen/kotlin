/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirThrowExpression
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.isTypeMismatchDueToNullability
import org.jetbrains.kotlin.fir.types.typeContext

object FirThrowExpressionTypeChecker : FirThrowExpressionChecker() {
    override fun check(expression: FirThrowExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        konst expectedType = context.session.builtinTypes.throwableType.coneType
        konst actualType = expression.exception.typeRef.coneType

        if (!actualType.isSubtypeOf(expectedType, context.session)) {
            reporter.reportOn(
                expression.exception.source,
                FirErrors.TYPE_MISMATCH,
                expectedType,
                actualType,
                context.session.typeContext.isTypeMismatchDueToNullability(expectedType, actualType),
                context,
            )
        }
    }
}