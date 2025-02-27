/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extended

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeNullability
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.CallableId

object UselessCallOnNotNullChecker : FirQualifiedAccessExpressionChecker() {
    // todo: add 'call may be reduced' in cases like 's?.isNullOrEmpty()' where 's: String? = ""'
    override fun check(expression: FirQualifiedAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        konst method = expression.getCallableId() ?: return
        konst calleeOn = expression.explicitReceiver ?: return
        konst calleePackageName = calleeOn.getPackage()
        konst calleeName = method.callableName.asString()
        if ("$calleePackageName.$calleeName" !in triggerOn) return

        if (calleeOn.getNullability() == ConeNullability.NOT_NULL) {
            reporter.reportOn(expression.source, FirErrors.USELESS_CALL_ON_NOT_NULL, context)
        }
    }

    private fun FirQualifiedAccessExpression.getCallableId(): CallableId? {
        return calleeReference.toResolvedFunctionSymbol()?.callableId
    }

    private fun FirExpression.getPackage(): String {
        return typeRef.coneType.classId?.packageFqName.toString()
    }

    private fun FirExpression.getNullability() = typeRef.coneType.nullability


    private konst triggerOn = setOf(
        "kotlin.collections.orEmpty",
        "kotlin.sequences.orEmpty",
        "kotlin.text.orEmpty",
        "kotlin.text.isNullOrEmpty",
        "kotlin.text.isNullOrEmpty",
        "kotlin.text.isNullOrBlank",
        "kotlin.isNullOrBlank",
        "kotlin.isNullOrEmpty",
        "kotlin.orEmpty"
    )

}
