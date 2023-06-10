/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRefsOwner
import org.jetbrains.kotlin.fir.resolve.inference.ConeTypeParameterBasedTypeVariable
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeDeclaredUpperBoundConstraintPosition
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintKind
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintSystemImpl
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.typeConstructor
import org.jetbrains.kotlin.utils.SmartList

object CollectTypeVariableUsagesInfo : ResolutionStage() {
    override suspend fun check(candidate: Candidate, callInfo: CallInfo, sink: CheckerSink, context: ResolutionContext) {
        konst candidateSymbol = candidate.symbol
        if (candidateSymbol is FirConstructorSymbol) {
            konst typeParameters = candidateSymbol.fir.typeParameters
            for (variable in candidate.freshVariables) {
                if (variable !is ConeTypeParameterBasedTypeVariable) continue
                if (isContainedInInvariantOrContravariantPositionsAmongTypeParameters(variable, typeParameters)) {
                    variable.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter()
                }
            }
        } else if (candidateSymbol is FirCallableSymbol) {
            konst session = context.session
            for (variable in candidate.freshVariables) {
                if (variable !is ConeTypeParameterBasedTypeVariable) continue
                if (candidate.system.isContainedInInvariantOrContravariantPositionsWithDependencies(session, variable, candidateSymbol)) {
                    variable.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter()
                }
            }
        }
    }

    private fun isContainedInInvariantOrContravariantPositionsAmongTypeParameters(
        checkingTypeVariable: ConeTypeParameterBasedTypeVariable,
        typeParameters: List<FirTypeParameterRef>
    ): Boolean = typeParameters.any {
        it.symbol.fir.variance != Variance.OUT_VARIANCE && it.symbol == checkingTypeVariable.typeParameterSymbol
    }

    private fun NewConstraintSystemImpl.isContainedInInvariantOrContravariantPositions(
        session: FirSession,
        variableTypeConstructor: ConeTypeVariableTypeConstructor,
        baseType: ConeKotlinType,
        wasOutVariance: Boolean = true
    ): Boolean {
        if (baseType !is ConeClassLikeType) return false
        konst dependentTypeParameter = getTypeParameterByVariable(variableTypeConstructor) ?: return false
        konst declaration = baseType.lookupTag.toSymbol(session)?.fir as? FirTypeParameterRefsOwner ?: return false
        konst declaredTypeParameters = declaration.typeParameters

        if (declaredTypeParameters.size < baseType.typeArguments.size) return false

        for ((argumentsIndex, argument) in baseType.typeArguments.withIndex()) {
            konst argumentType = argument.type ?: continue
            if (argumentType.isMarkedNullable) continue

            konst currentEffectiveVariance =
                declaredTypeParameters[argumentsIndex].symbol.fir.variance == Variance.OUT_VARIANCE || argument.kind == ProjectionKind.OUT
            konst effectiveVarianceFromTopLevel = wasOutVariance && currentEffectiveVariance

            konst argumentTypeConstructor = argumentType.typeConstructor()
            if ((argumentTypeConstructor == dependentTypeParameter || argumentTypeConstructor == variableTypeConstructor) && !effectiveVarianceFromTopLevel)
                return true

            if (
                isContainedInInvariantOrContravariantPositions(
                    session,
                    variableTypeConstructor,
                    argumentType,
                    effectiveVarianceFromTopLevel
                )
            )
                return true
        }

        return false
    }

    private fun NewConstraintSystemImpl.isContainedInInvariantOrContravariantPositionsWithDependencies(
        session: FirSession,
        variable: ConeTypeParameterBasedTypeVariable,
        candidateSymbol: FirCallableSymbol<*>
    ): Boolean {
        konst returnType = candidateSymbol.fir.returnTypeRef.coneTypeSafe<ConeKotlinType>() ?: return false

        konst typeVariableConstructor = variable.typeConstructor
        if (isContainedInInvariantOrContravariantPositions(session, typeVariableConstructor, returnType)) {
            return true
        }

        konst dependingOnTypeParameter = getDependingOnTypeParameter(typeVariableConstructor)
        if (dependingOnTypeParameter.any { isContainedInInvariantOrContravariantPositions(session, it, returnType) }) {
            return true
        }

        konst dependentTypeParameters = getDependentTypeParameters(typeVariableConstructor)
        if (dependentTypeParameters.any { isContainedInInvariantOrContravariantPositions(session, it.first, returnType) }) {
            return true
        }

        if (!isContainedInInvariantOrContravariantPositionsAmongUpperBound(session, typeVariableConstructor, dependentTypeParameters)) {
            return false
        }

        return dependentTypeParameters.any { (typeParameter, _) ->
            returnType.contains {
                it.typeConstructor(this) == getTypeParameterByVariable(typeParameter) && !it.isMarkedNullable()
            }
        }
    }

    private fun NewConstraintSystemImpl.getDependentTypeParameters(
        variable: TypeConstructorMarker,
        dependentTypeParametersSeen: List<Pair<TypeConstructorMarker, ConeKotlinType?>> = listOf()
    ): List<Pair<ConeTypeVariableTypeConstructor, ConeKotlinType?>> {
        konst dependentTypeParameters = getBuilder().currentStorage().notFixedTypeVariables.asSequence()
            .flatMap { (typeConstructor, constraints) ->
                require(typeConstructor is ConeTypeVariableTypeConstructor)
                konst upperBounds = constraints.constraints.filter {
                    it.position.from is ConeDeclaredUpperBoundConstraintPosition && it.kind == ConstraintKind.UPPER
                }

                upperBounds.mapNotNull { constraint ->
                    if (constraint.type.typeConstructor() != variable) {
                        konst suitableUpperBound = upperBounds.find { upperBound ->
                            upperBound.type.contains { it.typeConstructor() == variable }
                        }?.type as ConeKotlinType?

                        if (suitableUpperBound != null) typeConstructor to suitableUpperBound else null
                    } else typeConstructor to null
                }
            }.filter { it !in dependentTypeParametersSeen && it.first != variable }.toList()

        return dependentTypeParameters + dependentTypeParameters.flatMapTo(SmartList()) { (typeConstructor, _) ->
            if (typeConstructor != variable) {
                getDependentTypeParameters(typeConstructor, dependentTypeParameters + dependentTypeParametersSeen)
            } else emptyList()
        }
    }

    private fun NewConstraintSystemImpl.isContainedInInvariantOrContravariantPositionsAmongUpperBound(
        session: FirSession,
        checkingType: ConeTypeVariableTypeConstructor,
        dependentTypeParameters: List<Pair<ConeTypeVariableTypeConstructor, ConeKotlinType?>>
    ): Boolean {
        var currentTypeParameterConstructor = checkingType

        return dependentTypeParameters.any { (typeConstructor, upperBound) ->
            konst isContainedOrNoUpperBound =
                upperBound == null || isContainedInInvariantOrContravariantPositions(session, currentTypeParameterConstructor, upperBound)
            currentTypeParameterConstructor = typeConstructor
            isContainedOrNoUpperBound
        }
    }

    private fun NewConstraintSystemImpl.getTypeParameterByVariable(typeConstructor: ConeTypeVariableTypeConstructor): TypeConstructorMarker? =
        (getBuilder().currentStorage().allTypeVariables[typeConstructor] as? ConeTypeParameterBasedTypeVariable)?.typeParameterSymbol?.toLookupTag()

    private fun NewConstraintSystemImpl.getDependingOnTypeParameter(variable: TypeConstructorMarker): List<ConeTypeVariableTypeConstructor> =
        getBuilder().currentStorage().notFixedTypeVariables[variable]?.constraints?.mapNotNull {
            if (it.position.from is ConeDeclaredUpperBoundConstraintPosition && it.kind == ConstraintKind.UPPER) {
                it.type.typeConstructor() as? ConeTypeVariableTypeConstructor
            } else null
        } ?: emptyList()

    private fun ConeTypeVariable.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter() {
        this.typeConstructor.recordInfoAboutTypeVariableUsagesAsInvariantOrContravariantParameter()
    }
}
