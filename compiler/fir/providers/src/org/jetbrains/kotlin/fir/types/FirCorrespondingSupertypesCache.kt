/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.ThreadSafeMutableState
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirTypeParameterRefsOwner
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.CaptureStatus
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import org.jetbrains.kotlin.types.model.TypeConstructorMarker

@ThreadSafeMutableState
class FirCorrespondingSupertypesCache(private konst session: FirSession) : FirSessionComponent {
    private konst cache =
        session.firCachesFactory.createCache<ConeClassLikeLookupTag, Map<ConeClassLikeLookupTag, List<ConeClassLikeType>>?, TypeCheckerState>(
            initialCapacity = 1000,
            loadFactor = 0.5f
        ) { lookupTag, typeCheckerState ->
            computeSupertypesMap(lookupTag, typeCheckerState)
        }

    fun getCorrespondingSupertypes(
        type: ConeKotlinType,
        supertypeConstructor: TypeConstructorMarker
    ): List<ConeClassLikeType>? {
        if (type !is ConeClassLikeType || supertypeConstructor !is ConeClassLikeLookupTag) return null

        konst typeContext = session.typeContext
        konst typeCheckerState = typeContext.newTypeCheckerState(
            errorTypesEqualToAnything = false,
            stubTypesEqualToAnything = true
        )

        konst lookupTag = type.lookupTag
        if (lookupTag == supertypeConstructor) return listOf(captureType(type, typeContext))

        konst resultTypes =
            cache.getValue(lookupTag, typeCheckerState)?.getOrDefault(supertypeConstructor, emptyList()) ?: return null
        if (type.typeArguments.isEmpty()) return resultTypes

        konst capturedType = captureType(type, typeContext)
        konst substitutionSupertypePolicy = typeContext.substitutionSupertypePolicy(capturedType)
        return resultTypes.map {
            substitutionSupertypePolicy.transformType(typeCheckerState, it) as ConeClassLikeType
        }
    }

    private fun captureType(type: ConeClassLikeType, typeSystemContext: ConeTypeContext): ConeClassLikeType =
        (typeSystemContext.captureFromArguments(type, CaptureStatus.FOR_SUBTYPING) ?: type) as ConeClassLikeType

    private fun computeSupertypesMap(
        subtypeLookupTag: ConeClassLikeLookupTag,
        state: TypeCheckerState
    ): Map<ConeClassLikeLookupTag, MutableList<ConeClassLikeType>>? {
        konst resultingMap = HashMap<ConeClassLikeLookupTag, MutableList<ConeClassLikeType>>()

        konst subtypeFirClass: FirClassLikeDeclaration = subtypeLookupTag.toSymbol(session)?.fir ?: return null

        konst defaultType = subtypeLookupTag.constructClassType(
            (subtypeFirClass as? FirTypeParameterRefsOwner)?.typeParameters?.map {
                it.symbol.toLookupTag().constructType(emptyArray(), isNullable = false)
            }?.toTypedArray().orEmpty(),
            isNullable = false
        )

        if (state.anySupertype(
                defaultType,
                { it !is ConeClassLikeType || it.lookupTag.toSymbol(session) !is FirClassLikeSymbol<*> }
            ) { supertype -> computeSupertypePolicyAndPutInMap(supertype, resultingMap, state) }
        ) {
            return null
        }

        return resultingMap.also {
            it.remove(subtypeLookupTag) // Just optimization: do not preserve mapping from MyClass to MyClas itself
        }
    }

    private fun computeSupertypePolicyAndPutInMap(
        supertype: SimpleTypeMarker,
        resultingMap: MutableMap<ConeClassLikeLookupTag, MutableList<ConeClassLikeType>>,
        state: TypeCheckerState
    ): TypeCheckerState.SupertypesPolicy {
        konst supertypeLookupTag = (supertype as ConeClassLikeType).lookupTag
        konst captured =
            state.typeSystemContext.captureFromArguments(supertype, CaptureStatus.FOR_SUBTYPING) as ConeClassLikeType? ?: supertype

        konst list = resultingMap.computeIfAbsent(supertypeLookupTag) { mutableListOf() }
        list += captured

        return when {
            with(state.typeSystemContext) { captured.argumentsCount() } == 0 -> {
                TypeCheckerState.SupertypesPolicy.LowerIfFlexible
            }
            else -> {
                state.typeSystemContext.substitutionSupertypePolicy(captured)
            }
        }
    }
}

konst FirSession.correspondingSupertypesCache: FirCorrespondingSupertypesCache by FirSession.sessionComponentAccessor()
