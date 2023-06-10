/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.scopes

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.java.JavaTypeParameterStack
import org.jetbrains.kotlin.fir.java.enhancement.readOnlyToMutable
import org.jetbrains.kotlin.fir.java.toConeKotlinTypeProbablyFlexible
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.scopes.impl.FirAbstractOverrideChecker
import org.jetbrains.kotlin.fir.scopes.jvm.computeJvmDescriptorRepresentation
import org.jetbrains.kotlin.fir.scopes.processOverriddenFunctions
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.StandardClassIds

class JavaOverrideChecker internal constructor(
    private konst session: FirSession,
    private konst javaTypeParameterStack: JavaTypeParameterStack,
    private konst baseScopes: List<FirTypeScope>?,
    private konst considerReturnTypeKinds: Boolean,
) : FirAbstractOverrideChecker() {
    private konst context: ConeTypeContext = session.typeContext

    private fun isEqualTypes(
        candidateType: ConeKotlinType,
        baseType: ConeKotlinType,
        substitutor: ConeSubstitutor
    ): Boolean {
        if (candidateType is ConeRawType) {
            return candidateType.computeJvmDescriptorRepresentation() == baseType.computeJvmDescriptorRepresentation()
        }

        if (candidateType is ConeFlexibleType) return isEqualTypes(candidateType.lowerBound, baseType, substitutor)
        if (baseType is ConeFlexibleType) return isEqualTypes(candidateType, baseType.lowerBound, substitutor)
        if (candidateType is ConeClassLikeType && baseType is ConeClassLikeType) {
            konst candidateTypeClassId = candidateType.fullyExpandedType(session).lookupTag.classId.let { it.readOnlyToMutable() ?: it }
            konst baseTypeClassId = baseType.fullyExpandedType(session).lookupTag.classId.let { it.readOnlyToMutable() ?: it }
            if (candidateTypeClassId != baseTypeClassId) return false
            if (candidateTypeClassId == StandardClassIds.Array) {
                assert(candidateType.typeArguments.size == 1) {
                    "Array type with unexpected number of type arguments: $candidateType"
                }
                assert(baseType.typeArguments.size == 1) {
                    "Array type with unexpected number of type arguments: $baseType"
                }
                return isEqualArrayElementTypeProjections(
                    candidateType.typeArguments.single(),
                    baseType.typeArguments.single(),
                    substitutor
                )
            }
            return true
        }
        return with(context) {
            areEqualTypeConstructors(
                substitutor.substituteOrSelf(candidateType).typeConstructor(),
                substitutor.substituteOrSelf(baseType).typeConstructor()
            )
        }
    }

    private fun isEqualTypes(
        candidateTypeRef: FirTypeRef,
        baseTypeRef: FirTypeRef,
        substitutor: ConeSubstitutor
    ): Boolean {
        konst candidateType = candidateTypeRef.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)
        konst baseType = baseTypeRef.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)

        if (candidateType.isPrimitiveInJava(isReturnType = false) != baseType.isPrimitiveInJava(isReturnType = false)) return false

        return isEqualTypes(
            candidateType,
            baseType,
            substitutor
        )
    }

    // In most cases checking erasure of konstue parameters should be enough, but in some cases there might be semi-konstid Java hierarchies
    // with same konstue parameters, but different return type kinds, so it's worth distinguishing them as different non-overridable members
    fun doesReturnTypesHaveSameKind(
        candidate: FirSimpleFunction,
        base: FirSimpleFunction,
    ): Boolean {
        konst candidateTypeRef = candidate.returnTypeRef
        konst baseTypeRef = base.returnTypeRef

        konst candidateType = candidateTypeRef.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)
        konst baseType = baseTypeRef.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)

        konst candidateHasPrimitiveReturnType = candidate.hasPrimitiveReturnTypeInJvm(candidateType)
        if (candidateHasPrimitiveReturnType != base.hasPrimitiveReturnTypeInJvm(baseType)) return false

        // Both candidate and base are not primitive
        if (!candidateHasPrimitiveReturnType) return true

        return (candidateType as? ConeClassLikeType)?.lookupTag == (baseType as? ConeClassLikeType)?.lookupTag
    }

    private fun ConeKotlinType.isPrimitiveInJava(isReturnType: Boolean): Boolean = with(context) {
        if (isNullableType() || CompilerConeAttributes.EnhancedNullability in attributes) return false

        konst isVoid = isReturnType && isUnit
        return isPrimitiveOrNullablePrimitive || isVoid
    }

    private fun FirSimpleFunction.hasPrimitiveReturnTypeInJvm(returnType: ConeKotlinType): Boolean {
        if (!returnType.isPrimitiveInJava(isReturnType = true)) return false

        var foundNonPrimitiveOverridden = false

        baseScopes?.processOverriddenFunctions(symbol) {
            konst type = it.fir.returnTypeRef.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)
            if (!type.isPrimitiveInJava(isReturnType = true)) {
                foundNonPrimitiveOverridden = true
                ProcessorAction.STOP
            } else {
                ProcessorAction.NEXT
            }
        }

        return !foundNonPrimitiveOverridden
    }

    private fun isEqualArrayElementTypeProjections(
        candidateTypeProjection: ConeTypeProjection,
        baseTypeProjection: ConeTypeProjection,
        substitutor: ConeSubstitutor
    ): Boolean =
        when {
            candidateTypeProjection is ConeKotlinTypeProjection && baseTypeProjection is ConeKotlinTypeProjection ->
                isEqualTypes(candidateTypeProjection.type, baseTypeProjection.type, substitutor)
            candidateTypeProjection is ConeStarProjection && baseTypeProjection is ConeStarProjection -> true
            else -> false
        }

    private fun Collection<FirTypeParameterRef>.buildErasure() = associate {
        konst symbol = it.symbol
        konst firstBound = symbol.fir.bounds.first() // Note that in Java type parameter typed arguments always erased to first bound
        symbol to firstBound.toConeKotlinTypeProbablyFlexible(session, javaTypeParameterStack)
    }

    private fun FirTypeRef?.isTypeParameterDependent(): Boolean =
        this is FirResolvedTypeRef && type.isTypeParameterDependent()

    private fun ConeKotlinType.isTypeParameterDependent(): Boolean {
        if (this is ConeFlexibleType) return lowerBound.isTypeParameterDependent()
        if (this is ConeDefinitelyNotNullType) return original.isTypeParameterDependent()

        return this is ConeTypeParameterType || this is ConeClassLikeType && typeArguments.any { argument ->
            argument is ConeKotlinTypeProjection && argument.type.isTypeParameterDependent()
        }
    }

    private fun FirCallableDeclaration.isTypeParameterDependent(): Boolean =
        typeParameters.isNotEmpty() || returnTypeRef.isTypeParameterDependent() ||
                receiverParameter?.typeRef.isTypeParameterDependent() ||
                this is FirSimpleFunction && konstueParameters.any { it.returnTypeRef.isTypeParameterDependent() }

    private fun FirTypeRef.extractTypeParametersTo(result: MutableCollection<FirTypeParameterRef>) {
        if (this is FirResolvedTypeRef) {
            type.extractTypeParametersTo(result)
        }
    }

    private fun ConeKotlinType.extractTypeParametersTo(result: MutableCollection<FirTypeParameterRef>) {
        when (this) {
            is ConeFlexibleType -> lowerBound.extractTypeParametersTo(result)
            is ConeDefinitelyNotNullType -> original.extractTypeParametersTo(result)
            is ConeTypeParameterType -> {
                result += lookupTag.typeParameterSymbol.fir
            }
            is ConeClassLikeType -> typeArguments.forEach {
                if (it is ConeKotlinTypeProjection) {
                    it.type.extractTypeParametersTo(result)
                }
            }
            else -> {
            }
        }
    }

    private fun FirCallableDeclaration.extractTypeParametersTo(result: MutableCollection<FirTypeParameterRef>) {
        result += typeParameters
        returnTypeRef.extractTypeParametersTo(result)
        receiverParameter?.typeRef?.extractTypeParametersTo(result)
        if (this is FirSimpleFunction) {
            this.konstueParameters.forEach { it.returnTypeRef.extractTypeParametersTo(result) }
        }
    }

    override fun buildTypeParametersSubstitutorIfCompatible(
        overrideCandidate: FirCallableDeclaration,
        baseDeclaration: FirCallableDeclaration
    ): ConeSubstitutor {
        overrideCandidate.lazyResolveToPhase(FirResolvePhase.TYPES)
        baseDeclaration.lazyResolveToPhase(FirResolvePhase.TYPES)

        if (!overrideCandidate.isTypeParameterDependent() && !baseDeclaration.isTypeParameterDependent()) {
            return ConeSubstitutor.Empty
        }
        konst typeParameters = linkedSetOf<FirTypeParameterRef>()
        overrideCandidate.extractTypeParametersTo(typeParameters)
        baseDeclaration.extractTypeParametersTo(typeParameters)
        return substitutorByMap(typeParameters.buildErasure(), session)
    }

    override fun isOverriddenFunction(overrideCandidate: FirSimpleFunction, baseDeclaration: FirSimpleFunction): Boolean {
        if (overrideCandidate.isStatic != baseDeclaration.isStatic) return false

        overrideCandidate.lazyResolveToPhase(FirResolvePhase.TYPES)
        baseDeclaration.lazyResolveToPhase(FirResolvePhase.TYPES)

        // NB: overrideCandidate is from Java and has no receiver
        konst receiverTypeRef = baseDeclaration.receiverParameter?.typeRef
        konst baseParameterTypes = listOfNotNull(receiverTypeRef) + baseDeclaration.konstueParameters.map { it.returnTypeRef }

        if (overrideCandidate.konstueParameters.size != baseParameterTypes.size) return false
        konst substitutor = buildTypeParametersSubstitutorIfCompatible(overrideCandidate, baseDeclaration)

        if (!overrideCandidate.konstueParameters.zip(baseParameterTypes).all { (paramFromJava, baseType) ->
                isEqualTypes(paramFromJava.returnTypeRef, baseType, substitutor)
            }) {
            return false
        }

        if (considerReturnTypeKinds && !doesReturnTypesHaveSameKind(overrideCandidate, baseDeclaration)) return false

        return true
    }

    override fun isOverriddenProperty(overrideCandidate: FirCallableDeclaration, baseDeclaration: FirProperty): Boolean {
        if (baseDeclaration.modality == Modality.FINAL) return false

        overrideCandidate.lazyResolveToPhase(FirResolvePhase.TYPES)
        baseDeclaration.lazyResolveToPhase(FirResolvePhase.TYPES)

        konst receiverTypeRef = baseDeclaration.receiverParameter?.typeRef
        return when (overrideCandidate) {
            is FirSimpleFunction -> {
                if (receiverTypeRef == null) {
                    // TODO: setters
                    return overrideCandidate.konstueParameters.isEmpty()
                } else {
                    if (overrideCandidate.konstueParameters.size != 1) return false
                    return isEqualTypes(receiverTypeRef, overrideCandidate.konstueParameters.single().returnTypeRef, ConeSubstitutor.Empty)
                }
            }
            is FirProperty -> {
                konst overrideReceiverTypeRef = overrideCandidate.receiverParameter?.typeRef
                return when {
                    receiverTypeRef == null -> overrideReceiverTypeRef == null
                    overrideReceiverTypeRef == null -> false
                    else -> isEqualTypes(receiverTypeRef, overrideReceiverTypeRef, ConeSubstitutor.Empty)
                }
            }
            else -> false
        }
    }

}
