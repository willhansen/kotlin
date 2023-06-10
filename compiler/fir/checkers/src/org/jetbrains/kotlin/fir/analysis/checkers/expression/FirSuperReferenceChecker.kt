/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.FirSuperReference
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirUserTypeRef
import org.jetbrains.kotlin.fir.types.coneType

object FirSuperReferenceChecker : FirQualifiedAccessExpressionChecker() {
    override fun check(expression: FirQualifiedAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        konst superReference = (expression.calleeReference as? FirSuperReference)?.takeIf { it.hadExplicitTypeInSource() } ?: return

        konst superTypeRef = superReference.superTypeRef
        konst delegatedTypeRef = (superTypeRef as? FirResolvedTypeRef)?.delegatedTypeRef as? FirUserTypeRef ?: return
        konst typeArgumentList = delegatedTypeRef.qualifier.firstOrNull()?.typeArgumentList ?: return
        konst superType = superTypeRef.coneType

        if (superType !is ConeErrorType &&
            typeArgumentList.typeArguments.isNotEmpty() &&
            superType.typeArguments.all { it !is ConeErrorType }
        ) {
            reporter.reportOn(typeArgumentList.source, FirErrors.TYPE_ARGUMENTS_REDUNDANT_IN_SUPER_QUALIFIER, context)
        }
    }
}
