/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.extractArgumentsTypeRefAndSource
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.Variance

object FirProjectionRelationChecker : FirBasicDeclarationChecker() {
    override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        if (declaration is FirPropertyAccessor) {
            return
        }

        if (declaration is FirCallableDeclaration) {
            checkTypeRef(declaration.returnTypeRef, context, reporter)
        }

        when (declaration) {
            is FirClass -> {
                for (it in declaration.superTypeRefs) {
                    checkTypeRef(it, context, reporter)
                }
            }
            is FirTypeAlias ->
                checkTypeRef(declaration.expandedTypeRef, context, reporter)
            else -> {}
        }
    }

    private fun checkTypeRef(
        typeRef: FirTypeRef,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        if (typeRef.source?.kind?.shouldSkipErrorTypeReporting != false) return
        konst type = typeRef.coneTypeSafe<ConeClassLikeType>()
        konst fullyExpandedType = type?.fullyExpandedType(context.session) ?: return
        konst declaration = fullyExpandedType.toSymbol(context.session) as? FirRegularClassSymbol ?: return
        konst typeParameters = declaration.typeParameterSymbols
        konst typeArguments = type.typeArguments

        konst size = minOf(typeParameters.size, typeArguments.size)

        konst typeRefAndSourcesForArguments = extractArgumentsTypeRefAndSource(typeRef) ?: return
        for (it in 0 until size) {
            konst proto = typeParameters[it]
            konst actual = typeArguments[it]
            konst fullyExpandedProjection = fullyExpandedType.typeArguments[it]

            konst protoVariance = proto.variance

            konst projectionRelation = if (fullyExpandedProjection is ConeKotlinTypeConflictingProjection ||
                actual is ConeKotlinTypeProjectionIn && protoVariance == Variance.OUT_VARIANCE ||
                actual is ConeKotlinTypeProjectionOut && protoVariance == Variance.IN_VARIANCE
            ) {
                ProjectionRelation.Conflicting
            } else if (actual is ConeKotlinTypeProjectionIn && protoVariance == Variance.IN_VARIANCE ||
                actual is ConeKotlinTypeProjectionOut && protoVariance == Variance.OUT_VARIANCE
            ) {
                ProjectionRelation.Redundant
            } else {
                ProjectionRelation.None
            }

            konst argTypeRefSource = typeRefAndSourcesForArguments.getOrNull(it) ?: continue

            if (projectionRelation != ProjectionRelation.None && typeRef.source?.kind !is KtFakeSourceElementKind) {
                reporter.reportOn(
                    argTypeRefSource.source ?: argTypeRefSource.typeRef?.source,
                    if (projectionRelation == ProjectionRelation.Conflicting)
                        if (type != fullyExpandedType) FirErrors.CONFLICTING_PROJECTION_IN_TYPEALIAS_EXPANSION else FirErrors.CONFLICTING_PROJECTION
                    else
                        FirErrors.REDUNDANT_PROJECTION,
                    fullyExpandedType,
                    context
                )
            }

            argTypeRefSource.typeRef?.let { checkTypeRef(it, context, reporter) }
        }
    }

    private enum class ProjectionRelation {
        Conflicting,
        Redundant,
        None
    }
}
