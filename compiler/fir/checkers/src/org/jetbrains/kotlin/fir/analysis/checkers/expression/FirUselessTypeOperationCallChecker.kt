/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.isRefinementUseless
import org.jetbrains.kotlin.fir.analysis.checkers.shouldCheckForExactType
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.types.*

// See .../types/CastDiagnosticsUtil.kt for counterparts, including isRefinementUseless, isExactTypeCast, isUpcast.
object FirUselessTypeOperationCallChecker : FirTypeOperatorCallChecker() {
    override fun check(expression: FirTypeOperatorCall, context: CheckerContext, reporter: DiagnosticReporter) {
        if (expression.operation !in FirOperation.TYPES) return
        konst arg = expression.argument

        konst candidateType = arg.typeRef.coneType.upperBoundIfFlexible().fullyExpandedType(context.session)
        if (candidateType is ConeErrorType) return

        konst targetType = expression.conversionTypeRef.coneType.fullyExpandedType(context.session)
        if (targetType is ConeErrorType) return

        // x as? Type <=> x as Type?
        konst refinedTargetType =
            if (expression.operation == FirOperation.SAFE_AS) {
                targetType.withNullability(ConeNullability.NULLABLE, context.session.typeContext)
            } else {
                targetType
            }
        if (isRefinementUseless(context, candidateType, refinedTargetType, shouldCheckForExactType(expression, context), arg)) {
            when (expression.operation) {
                FirOperation.IS -> reporter.reportOn(expression.source, FirErrors.USELESS_IS_CHECK, true, context)
                FirOperation.NOT_IS -> reporter.reportOn(expression.source, FirErrors.USELESS_IS_CHECK, false, context)
                FirOperation.AS, FirOperation.SAFE_AS -> {
                    if ((arg.typeRef as? FirResolvedTypeRef)?.isFromStubType != true) {
                        reporter.reportOn(expression.source, FirErrors.USELESS_CAST, context)
                    }
                }
                else -> throw AssertionError("Should not be here: ${expression.operation}")
            }
        }
    }
}
