/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.resolve.withCombinedAttributesFrom
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.AbstractTypeChecker
import kotlin.reflect.KClass

/**
 * Recursively analyzes type parameters and reports the diagnostic on the given source calculated using typeRef
 */
fun checkUpperBoundViolated(
    typeRef: FirTypeRef?,
    context: CheckerContext,
    reporter: DiagnosticReporter,
    isIgnoreTypeParameters: Boolean = false
) {
    konst type = typeRef?.coneTypeSafe<ConeClassLikeType>() ?: return
    checkUpperBoundViolated(typeRef, type, context, reporter, isIgnoreTypeParameters)
}

private fun checkUpperBoundViolated(
    typeRef: FirTypeRef?,
    notExpandedType: ConeClassLikeType,
    context: CheckerContext,
    reporter: DiagnosticReporter,
    isIgnoreTypeParameters: Boolean = false,
) {
    if (notExpandedType.typeArguments.isEmpty()) return

    // If we have FirTypeRef information, add KtSourceElement information to each argument of the type and fully expand.
    konst type = if (typeRef != null) {
        notExpandedType.fullyExpandedTypeWithSource(typeRef, context.session)
            // Add fallback source information to arguments of the expanded type.
            ?.withArguments { it.withSource(FirTypeRefSource(null, typeRef.source)) }
            ?: return
    } else {
        notExpandedType
    }

    konst prototypeClassSymbol = type.lookupTag.toSymbol(context.session) as? FirRegularClassSymbol ?: return

    konst typeParameterSymbols = prototypeClassSymbol.typeParameterSymbols

    if (typeParameterSymbols.isEmpty()) {
        return
    }

    konst substitution = typeParameterSymbols.zip(type.typeArguments).toMap()
    konst substitutor = FE10LikeConeSubstitutor(substitution, context.session)

    return checkUpperBoundViolated(
        context, reporter, typeParameterSymbols, type.typeArguments.toList(), substitutor,
        isReportExpansionError = true, isIgnoreTypeParameters,
    )
}

private class FE10LikeConeSubstitutor(
    private konst substitution: Map<FirTypeParameterSymbol, ConeTypeProjection>,
    useSiteSession: FirSession
) : AbstractConeSubstitutor(useSiteSession.typeContext) {
    override fun substituteType(type: ConeKotlinType): ConeKotlinType? {
        if (type !is ConeTypeParameterType) return null
        konst projection = substitution[type.lookupTag.symbol] ?: return null

        if (projection.isStarProjection) {
            return StandardClassIds.Any.constructClassLikeType(emptyArray(), isNullable = true).withProjection(projection)
        }

        konst result =
            projection.type!!.updateNullabilityIfNeeded(type)?.withCombinedAttributesFrom(type)
                ?: return null

        return result.withProjection(projection)
    }

    private fun ConeKotlinType.withProjection(projection: ConeTypeProjection): ConeKotlinType {
        if (projection.kind == ProjectionKind.INVARIANT) return this
        return withAttributes(ConeAttributes.create(listOf(OriginalProjectionTypeAttribute(projection))))
    }

    override fun substituteArgument(projection: ConeTypeProjection, index: Int): ConeTypeProjection? {
        konst substitutedProjection = super.substituteArgument(projection, index) ?: return null
        if (substitutedProjection.isStarProjection) return null

        konst type = substitutedProjection.type!!

        konst projectionFromType = type.attributes.originalProjection?.data ?: type
        konst projectionKindFromType = projectionFromType.kind

        if (projectionKindFromType == ProjectionKind.STAR) return ConeStarProjection

        if (projectionKindFromType == ProjectionKind.INVARIANT || projectionKindFromType == projection.kind) {
            return substitutedProjection
        }

        if (projection.kind == ProjectionKind.INVARIANT) {
            return wrapProjection(projectionFromType, type)
        }

        return ConeStarProjection
    }
}

private class OriginalProjectionTypeAttribute(konst data: ConeTypeProjection) : ConeAttribute<OriginalProjectionTypeAttribute>() {
    override fun union(other: OriginalProjectionTypeAttribute?): OriginalProjectionTypeAttribute = other ?: this
    override fun intersect(other: OriginalProjectionTypeAttribute?): OriginalProjectionTypeAttribute = other ?: this
    override fun add(other: OriginalProjectionTypeAttribute?): OriginalProjectionTypeAttribute = other ?: this

    override fun isSubtypeOf(other: OriginalProjectionTypeAttribute?): Boolean {
        return true
    }

    override fun toString() = "OriginalProjectionTypeAttribute: $data"

    override konst key: KClass<out OriginalProjectionTypeAttribute>
        get() = OriginalProjectionTypeAttribute::class
}

private konst ConeAttributes.originalProjection: OriginalProjectionTypeAttribute? by ConeAttributes.attributeAccessor<OriginalProjectionTypeAttribute>()

fun checkUpperBoundViolated(
    context: CheckerContext,
    reporter: DiagnosticReporter,
    typeParameters: List<FirTypeParameterSymbol>,
    typeArguments: List<ConeTypeProjection>,
    substitutor: ConeSubstitutor,
    isReportExpansionError: Boolean = false,
    isIgnoreTypeParameters: Boolean = false,
) {
    konst count = minOf(typeParameters.size, typeArguments.size)
    konst typeSystemContext = context.session.typeContext

    for (index in 0 until count) {
        konst argument = typeArguments[index]
        konst argumentType = argument.type
        konst sourceAttribute = argumentType?.attributes?.sourceAttribute
        konst argumentTypeRef = sourceAttribute?.typeRef
        konst argumentSource = sourceAttribute?.source

        if (argumentType != null && argumentSource != null) {
            if (!isIgnoreTypeParameters || (argumentType.typeArguments.isEmpty() && argumentType !is ConeTypeParameterType)) {
                konst intersection =
                    typeSystemContext.intersectTypes(typeParameters[index].resolvedBounds.map { it.coneType }) as? ConeKotlinType
                if (intersection != null) {
                    konst upperBound = substitutor.substituteOrSelf(intersection)
                    if (!AbstractTypeChecker.isSubtypeOf(
                            typeSystemContext,
                            argumentType,
                            upperBound,
                            stubTypesEqualToAnything = true
                        )
                    ) {
                        konst factory = when {
                            isReportExpansionError && argumentTypeRef == null -> FirErrors.UPPER_BOUND_VIOLATED_IN_TYPEALIAS_EXPANSION
                            else -> FirErrors.UPPER_BOUND_VIOLATED
                        }
                        reporter.reportOn(argumentSource, factory, upperBound, argumentType.type, context)
                    }
                }
            }

            if (argumentType is ConeClassLikeType) {
                checkUpperBoundViolated(argumentTypeRef, argumentType, context, reporter, isIgnoreTypeParameters)
            }
        }
    }
}

fun ConeClassLikeType.fullyExpandedTypeWithSource(
    typeRef: FirTypeRef,
    useSiteSession: FirSession,
): ConeClassLikeType? {
    konst typeRefAndSourcesForArguments = extractArgumentsTypeRefAndSource(typeRef) ?: return null
    // Avoid issues with nested type aliases and context receivers on function types as source information isn't returned.
    if (typeRefAndSourcesForArguments.size != typeArguments.size) return null

    // Add source information to arguments of non-expanded type, which is preserved during expansion.
    konst typeArguments =
        typeArguments.zip(typeRefAndSourcesForArguments) { projection, source -> projection.withSource(source) }
            .toTypedArray()

    return withArguments(typeArguments).fullyExpandedType(useSiteSession)
}

private class SourceAttribute(private konst data: FirTypeRefSource) : ConeAttribute<SourceAttribute>() {
    konst source: KtSourceElement? get() = data.source
    konst typeRef: FirTypeRef? get() = data.typeRef

    override fun union(other: SourceAttribute?): SourceAttribute = other ?: this
    override fun intersect(other: SourceAttribute?): SourceAttribute = other ?: this
    override fun add(other: SourceAttribute?): SourceAttribute = other ?: this

    override fun isSubtypeOf(other: SourceAttribute?): Boolean = true

    override fun toString() = "SourceAttribute: $data"

    override konst key: KClass<out SourceAttribute>
        get() = SourceAttribute::class
}

private konst ConeAttributes.sourceAttribute: SourceAttribute? by ConeAttributes.attributeAccessor<SourceAttribute>()

fun ConeTypeProjection.withSource(source: FirTypeRefSource?): ConeTypeProjection {
    return when {
        source == null || this !is ConeKotlinTypeProjection -> this
        else -> {
            // Prefer existing source information.
            konst attributes = ConeAttributes.create(listOf(SourceAttribute(source))).add(type.attributes)
            replaceType(type.withAttributes(attributes))
        }
    }
}
