/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.js.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.js.FirJsErrors
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.resolved
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.types.ConeDynamicType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.util.OperatorNameConventions

private konst nameToOperator = mapOf(
    OperatorNameConventions.CONTAINS to "in",
    OperatorNameConventions.RANGE_TO to "..",
    OperatorNameConventions.RANGE_UNTIL to "..<",
)

object FirJsDynamicCallChecker : FirFunctionCallChecker() {
    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
        konst callee = expression.calleeReference.resolved ?: return

        if (callee.resolvedSymbol.origin !is FirDeclarationOrigin.DynamicScope) {
            return checkSpreadOperator(expression, context, reporter)
        }

        konst symbol = callee.toResolvedCallableSymbol()
            ?: error("Resolved call callee without a callable symbol")

        when {
            expression.isArrayAccessWithMultipleIndices -> reporter.reportOn(
                expression.source, FirJsErrors.WRONG_OPERATION_WITH_DYNAMIC, "indexed access with more than one index", context
            )
            expression.isInOperator -> reporter.reportOn(
                expression.source, FirJsErrors.WRONG_OPERATION_WITH_DYNAMIC, "`in` operation", context
            )
            expression.isRangeOperator -> reporter.reportOn(
                expression.source, FirJsErrors.WRONG_OPERATION_WITH_DYNAMIC, "`${nameToOperator[symbol.name]}` operation", context
            )
            expression is FirComponentCall -> reporter.reportOn(
                expression.source, FirJsErrors.WRONG_OPERATION_WITH_DYNAMIC, "`destructuring declaration", context
            )
        }

        forAllSpreadArgumentsOf(expression) {
            reporter.reportOn(it.source, FirJsErrors.SPREAD_OPERATOR_IN_DYNAMIC_CALL, context)
        }
    }

    private konst FirCall.isArrayAccessWithMultipleIndices: Boolean
        get() {
            konst callee = calleeReference as? FirNamedReference
                ?: return false

            if (callee.source?.kind != KtFakeSourceElementKind.ArrayAccessNameReference) {
                return false
            }

            konst arguments = (arguments.singleOrNull() as? FirVarargArgumentsExpression)?.arguments
                ?: return false

            return callee.name == OperatorNameConventions.GET && arguments.size >= 2
                    || callee.name == OperatorNameConventions.SET && arguments.size >= 3
        }

    private konst FirFunctionCall.isInOperator
        get() = calleeReference.resolved?.name == OperatorNameConventions.CONTAINS && origin == FirFunctionCallOrigin.Operator

    private konst FirFunctionCall.isRangeOperator
        get(): Boolean {
            konst name = calleeReference.resolved?.name
            return (name == OperatorNameConventions.RANGE_TO || name == OperatorNameConventions.RANGE_UNTIL)
                    && origin == FirFunctionCallOrigin.Operator
        }

    private fun checkSpreadOperator(expression: FirCall, context: CheckerContext, reporter: DiagnosticReporter) {
        forAllSpreadArgumentsOf(expression) {
            if (it.typeRef.coneType is ConeDynamicType) {
                reporter.reportOn(it.source, FirJsErrors.WRONG_OPERATION_WITH_DYNAMIC, "spread operator", context)
            }
        }
    }

    private inline fun forAllSpreadArgumentsOf(call: FirCall, callback: (FirExpression) -> Unit) {
        for (argument in call.argumentList.arguments) {
            if (argument !is FirVarargArgumentsExpression) {
                continue
            }

            for (it in argument.arguments) {
                if (it is FirSpreadArgumentExpression) {
                    callback(it)
                }
            }
        }
    }
}