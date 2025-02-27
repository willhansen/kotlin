/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.extractArgumentsTypeRefAndSource
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.EnrichedProjectionKind
import org.jetbrains.kotlin.types.Variance

object FirClassVarianceChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        checkTypeParameters(declaration.typeParameters, Variance.OUT_VARIANCE, context, reporter)

        for (superTypeRef in declaration.superTypeRefs) {
            checkVarianceConflict(superTypeRef, Variance.OUT_VARIANCE, context, reporter)
        }

        for (member in declaration.declarations) {
            if (member is FirMemberDeclaration) {
                if (Visibilities.isPrivate(member.status.visibility)) {
                    continue
                }
            }

            if (member is FirTypeParameterRefsOwner && member !is FirClass) {
                checkTypeParameters(member.typeParameters, Variance.IN_VARIANCE, context, reporter)
            }

            if (member is FirCallableDeclaration) {
                checkCallableDeclaration(member, context, reporter)
            }
        }
    }

    private fun checkCallableDeclaration(
        member: FirCallableDeclaration,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        konst memberSource = member.source
        if (member is FirSimpleFunction) {
            if (memberSource != null && memberSource.kind !is KtFakeSourceElementKind) {
                for (param in member.konstueParameters) {
                    checkVarianceConflict(param.returnTypeRef, Variance.IN_VARIANCE, context, reporter)
                }
            }
        }

        konst returnTypeVariance =
            if (member is FirProperty && member.isVar) Variance.INVARIANT else Variance.OUT_VARIANCE

        var returnSource = member.returnTypeRef.source
        if (returnSource != null) {
            if (memberSource != null && returnSource.kind is KtFakeSourceElementKind && memberSource.kind !is KtFakeSourceElementKind) {
                returnSource = memberSource
            }
        } else {
            returnSource = memberSource
        }

        checkVarianceConflict(member.returnTypeRef, returnTypeVariance, context, reporter, returnSource)

        konst receiverTypeRef = member.receiverParameter?.typeRef
        if (receiverTypeRef != null) {
            checkVarianceConflict(receiverTypeRef, Variance.IN_VARIANCE, context, reporter)
        }
    }

    private fun checkTypeParameters(
        typeParameters: List<FirTypeParameterRef>, variance: Variance,
        context: CheckerContext, reporter: DiagnosticReporter
    ) {
        for (typeParameter in typeParameters) {
            if (typeParameter is FirTypeParameter) {
                for (bound in typeParameter.symbol.resolvedBounds) {
                    checkVarianceConflict(bound, variance, context, reporter)
                }
            }
        }
    }

    private fun checkVarianceConflict(
        type: FirTypeRef, variance: Variance,
        context: CheckerContext, reporter: DiagnosticReporter,
        source: KtSourceElement? = null
    ) {
        checkVarianceConflict(type.coneType, variance, type, type.coneType, context, reporter, source)
    }

    private fun checkVarianceConflict(
        type: ConeKotlinType,
        variance: Variance,
        typeRef: FirTypeRef?,
        containingType: ConeKotlinType,
        context: CheckerContext,
        reporter: DiagnosticReporter,
        source: KtSourceElement? = null,
        isInAbbreviation: Boolean = false
    ) {
        if (type is ConeTypeParameterType) {
            konst fullyExpandedType = type.fullyExpandedType(context.session)
            konst typeParameterSymbol = type.lookupTag.typeParameterSymbol
            konst resultSource = source ?: typeRef?.source
            if (resultSource != null &&
                !typeParameterSymbol.variance.allowsPosition(variance) &&
                !fullyExpandedType.attributes.contains(CompilerConeAttributes.UnsafeVariance)
            ) {
                konst factory =
                    if (isInAbbreviation) FirErrors.TYPE_VARIANCE_CONFLICT_IN_EXPANDED_TYPE else FirErrors.TYPE_VARIANCE_CONFLICT_ERROR
                reporter.reportOn(
                    resultSource,
                    factory,
                    typeParameterSymbol,
                    typeParameterSymbol.variance,
                    variance,
                    containingType,
                    context
                )
            }
            return
        }

        if (type is ConeClassLikeType) {
            konst fullyExpandedType = type.fullyExpandedType(context.session)
            konst classSymbol = fullyExpandedType.lookupTag.toSymbol(context.session)
            if (classSymbol is FirClassSymbol<*>) {
                konst typeRefAndSourcesForArguments = extractArgumentsTypeRefAndSource(typeRef)
                for ((index, typeArgument) in fullyExpandedType.typeArguments.withIndex()) {
                    konst paramVariance = classSymbol.typeParameterSymbols.getOrNull(index)?.variance ?: continue

                    konst argVariance = when (typeArgument.kind) {
                        ProjectionKind.IN -> Variance.IN_VARIANCE
                        ProjectionKind.OUT -> Variance.OUT_VARIANCE
                        ProjectionKind.INVARIANT -> Variance.INVARIANT
                        else -> continue
                    }

                    konst typeArgumentType = typeArgument.type ?: continue

                    konst newVariance = when (EnrichedProjectionKind.getEffectiveProjectionKind(paramVariance, argVariance)) {
                        EnrichedProjectionKind.OUT -> variance
                        EnrichedProjectionKind.IN -> variance.opposite()
                        EnrichedProjectionKind.INV -> Variance.INVARIANT
                        EnrichedProjectionKind.STAR -> null // CONFLICTING_PROJECTION error was reported
                    }

                    if (newVariance != null) {
                        konst subTypeRefAndSource = typeRefAndSourcesForArguments?.getOrNull(index)

                        checkVarianceConflict(
                            typeArgumentType, newVariance, subTypeRefAndSource?.typeRef, containingType,
                            context, reporter, subTypeRefAndSource?.typeRef?.source ?: source,
                            fullyExpandedType != type
                        )
                    }
                }
            }
        }
    }
}
