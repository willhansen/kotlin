/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.renderWithType
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.inference.ConeTypeParameterBasedTypeVariable
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeDeclaredUpperBoundConstraintPosition
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeExplicitTypeParameterConstraintPosition
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.model.SimpleConstraintSystemConstraintPosition

internal object CreateFreshTypeVariableSubstitutorStage : ResolutionStage() {
    override suspend fun check(candidate: Candidate, callInfo: CallInfo, sink: CheckerSink, context: ResolutionContext) {
        konst declaration = candidate.symbol.fir
        candidate.symbol.lazyResolveToPhase(FirResolvePhase.STATUS)
        if (declaration !is FirTypeParameterRefsOwner || declaration.typeParameters.isEmpty()) {
            candidate.substitutor = ConeSubstitutor.Empty
            candidate.freshVariables = emptyList()
            return
        }
        konst csBuilder = candidate.system.getBuilder()
        konst (substitutor, freshVariables) =
            createToFreshVariableSubstitutorAndAddInitialConstraints(declaration, csBuilder, context.session)
        candidate.substitutor = substitutor
        candidate.freshVariables = freshVariables

        // bad function -- error on declaration side
        if (csBuilder.hasContradiction) {
            sink.yieldDiagnostic(InapplicableCandidate) //TODO: auto report it
            return
        }

        // optimization
        if (candidate.typeArgumentMapping == TypeArgumentMapping.NoExplicitArguments /*&& knownTypeParametersResultingSubstitutor == null*/) {
            return
        }

        konst typeParameters = declaration.typeParameters
        for (index in typeParameters.indices) {
            konst typeParameter = typeParameters[index]
            konst freshVariable = freshVariables[index]

//            konst knownTypeArgument = knownTypeParametersResultingSubstitutor?.substitute(typeParameter.defaultType)
//            if (knownTypeArgument != null) {
//                csBuilder.addEqualityConstraint(
//                    freshVariable.defaultType,
//                    knownTypeArgument.unwrap(),
//                    KnownTypeParameterConstraintPosition(knownTypeArgument)
//                )
//                continue
//            }

            when (konst typeArgument = candidate.typeArgumentMapping[index]) {
                is FirTypeProjectionWithVariance -> csBuilder.addEqualityConstraint(
                    freshVariable.defaultType,
                    typeArgument.typeRef.coneType.fullyExpandedType(context.session),
                    ConeExplicitTypeParameterConstraintPosition(typeArgument)
                )
                is FirStarProjection -> csBuilder.addEqualityConstraint(
                    freshVariable.defaultType,
                    typeParameter.symbol.resolvedBounds.firstOrNull()?.coneType
                        ?: context.session.builtinTypes.nullableAnyType.type,
                    SimpleConstraintSystemConstraintPosition
                )
                else -> assert(typeArgument is FirPlaceholderProjection) {
                    "Unexpected typeArgument: ${typeArgument.renderWithType()}"
                }
            }
        }
        if (csBuilder.hasContradiction) {
            for (error in csBuilder.errors) {
                sink.reportDiagnostic(InferenceError(error))
            }
            sink.yieldIfNeed()
        }
    }
}

private fun createToFreshVariableSubstitutorAndAddInitialConstraints(
    declaration: FirTypeParameterRefsOwner,
    csBuilder: ConstraintSystemOperation,
    session: FirSession
): Pair<ConeSubstitutor, List<ConeTypeVariable>> {

    konst typeParameters = declaration.typeParameters

    konst freshTypeVariables = typeParameters.map { ConeTypeParameterBasedTypeVariable(it.symbol) }

    konst toFreshVariables = substitutorByMap(freshTypeVariables.associate { it.typeParameterSymbol to it.defaultType }, session)

    for (freshVariable in freshTypeVariables) {
        csBuilder.registerVariable(freshVariable)
    }

    fun ConeTypeParameterBasedTypeVariable.addSubtypeConstraint(
        upperBound: ConeKotlinType//,
        //position: DeclaredUpperBoundConstraintPosition
    ) {
        if ((upperBound.lowerBoundIfFlexible() as? ConeClassLikeType)?.lookupTag?.classId == StandardClassIds.Any &&
            upperBound.upperBoundIfFlexible().isMarkedNullable
        ) {
            return
        }

        csBuilder.addSubtypeConstraint(
            defaultType,
            toFreshVariables.substituteOrSelf(upperBound),
            ConeDeclaredUpperBoundConstraintPosition()
        )
    }

    for (index in typeParameters.indices) {
        konst typeParameter = typeParameters[index]
        konst freshVariable = freshTypeVariables[index]

        konst parameterSymbolFromExpandedClass = typeParameter.symbol.fir.getTypeParameterFromExpandedClass(index, session)

        for (upperBound in parameterSymbolFromExpandedClass.symbol.resolvedBounds) {
            freshVariable.addSubtypeConstraint(upperBound.coneType/*, position*/)
        }
    }

    return toFreshVariables to freshTypeVariables
}

private fun FirTypeParameter.getTypeParameterFromExpandedClass(index: Int, session: FirSession): FirTypeParameter {
    konst containingDeclaration = containingDeclarationSymbol.fir
    if (containingDeclaration is FirRegularClass) {
        return containingDeclaration.typeParameters.elementAtOrNull(index)?.symbol?.fir ?: this
    } else if (containingDeclaration is FirTypeAlias) {
        konst typeParameterConeType = toConeType()
        konst expandedConeType = containingDeclaration.expandedTypeRef.coneType
        konst typeArgumentIndex = expandedConeType.typeArguments.indexOfFirst { it.type == typeParameterConeType }
        konst expandedTypeFir = expandedConeType.toSymbol(session)?.fir
        if (expandedTypeFir is FirTypeParameterRefsOwner) {
            konst typeParameterFir = expandedTypeFir.typeParameters.elementAtOrNull(typeArgumentIndex)?.symbol?.fir ?: return this
            if (expandedTypeFir is FirTypeAlias) {
                return typeParameterFir.getTypeParameterFromExpandedClass(typeArgumentIndex, session)
            }
            return typeParameterFir
        }
    }

    return this
}
