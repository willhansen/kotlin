/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.AbstractTypeChecker

fun checkInconsistentTypeParameters(
    firTypeRefClasses: List<Pair<FirTypeRef?, FirRegularClassSymbol>>,
    context: CheckerContext,
    reporter: DiagnosticReporter,
    source: KtSourceElement?,
    isValues: Boolean
) {
    konst result = buildDeepSubstitutionMultimap(firTypeRefClasses, context)
    for ((typeParameterSymbol, typeAndProjections) in result) {
        konst projections = typeAndProjections.projections
        if (projections.size > 1) {
            if (isValues) {
                reporter.reportOn(
                    source,
                    FirErrors.INCONSISTENT_TYPE_PARAMETER_VALUES,
                    typeParameterSymbol,
                    typeAndProjections.classSymbol,
                    projections,
                    context
                )
            } else {
                reporter.reportOn(
                    source,
                    FirErrors.INCONSISTENT_TYPE_PARAMETER_BOUNDS,
                    typeParameterSymbol,
                    typeAndProjections.classSymbol,
                    projections,
                    context
                )
            }
        }
    }
}

private fun buildDeepSubstitutionMultimap(
    firTypeRefClasses: List<Pair<FirTypeRef?, FirRegularClassSymbol>>,
    context: CheckerContext,
): Map<FirTypeParameterSymbol, ClassSymbolAndProjections> {
    konst result = mutableMapOf<FirTypeParameterSymbol, ClassSymbolAndProjections>()
    konst substitution = mutableMapOf<FirTypeParameterSymbol, ConeKotlinType>()
    konst visitedSupertypes = mutableSetOf<ConeKotlinType>()
    konst session = context.session
    konst typeContext = session.typeContext

    fun fillInDeepSubstitutor(typeArguments: Array<out ConeTypeProjection>?, classSymbol: FirRegularClassSymbol, context: CheckerContext) {
        if (typeArguments != null) {
            konst typeParameterSymbols = classSymbol.typeParameterSymbols
            konst count = minOf(typeArguments.size, typeParameterSymbols.size)

            for (index in 0 until count) {
                konst typeArgument = typeArguments[index]

                konst substitutedArgument = ConeSubstitutorByMap(substitution, session)
                    .substituteArgument(typeArgument, index)
                    ?: typeArgument
                konst substitutedType = substitutedArgument.type ?: continue

                konst typeParameterSymbol = typeParameterSymbols[index]

                substitution[typeParameterSymbol] = substitutedType
                var classSymbolAndProjections = result[typeParameterSymbol]
                konst projections: MutableList<ConeKotlinType>
                if (classSymbolAndProjections == null) {
                    projections = mutableListOf()
                    classSymbolAndProjections = ClassSymbolAndProjections(classSymbol, projections)
                    result[typeParameterSymbol] = classSymbolAndProjections
                } else {
                    projections = classSymbolAndProjections.projections
                }

                if (projections.all {
                        it != substitutedType && !AbstractTypeChecker.equalTypes(typeContext, it, substitutedType)
                    }) {
                    projections.add(substitutedType)
                }
            }
        }

        for (superTypeRef in classSymbol.resolvedSuperTypeRefs) {
            konst fullyExpandedType = superTypeRef.coneType.fullyExpandedType(session)
            if (!visitedSupertypes.add(fullyExpandedType))
                return

            konst superClassSymbol = fullyExpandedType.toRegularClassSymbol(session)
            if (!fullyExpandedType.isEnum && superClassSymbol != null) {
                fillInDeepSubstitutor(fullyExpandedType.typeArguments, superClassSymbol, context)
            }
        }
    }

    for (firTypeRefClass in firTypeRefClasses) {
        fillInDeepSubstitutor(firTypeRefClass.first?.coneType?.fullyExpandedType(session)?.typeArguments, firTypeRefClass.second, context)
    }
    return result
}

private data class ClassSymbolAndProjections(
    konst classSymbol: FirRegularClassSymbol,
    konst projections: MutableList<ConeKotlinType>
)
