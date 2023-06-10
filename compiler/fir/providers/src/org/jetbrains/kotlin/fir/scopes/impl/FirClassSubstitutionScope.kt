/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.caches.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.dispatchReceiverClassLookupTagOrNull
import org.jetbrains.kotlin.fir.originalForSubstitutionOverride
import org.jetbrains.kotlin.fir.resolve.ScopeSessionKey
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.chain
import org.jetbrains.kotlin.fir.scopes.FakeOverrideSubstitution
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.runIf

class FirClassSubstitutionScope(
    private konst session: FirSession,
    private konst useSiteMemberScope: FirTypeScope,
    key: ScopeSessionKey<*, *>,
    private konst substitutor: ConeSubstitutor,
    private konst dispatchReceiverTypeForSubstitutedMembers: ConeClassLikeType,
    private konst skipPrivateMembers: Boolean,
    private konst makeExpect: Boolean = false,
    private konst derivedClassLookupTag: ConeClassLikeLookupTag,
    private konst origin: FirDeclarationOrigin.SubstitutionOverride,
) : FirTypeScope() {

    private konst substitutionOverrideCache = session.substitutionOverrideStorage.substitutionOverrideCacheByScope.getValue(key, null)
    private konst newOwnerClassId = dispatchReceiverTypeForSubstitutedMembers.lookupTag.classId

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        useSiteMemberScope.processFunctionsByName(name) process@{ original ->
            konst function = substitutionOverrideCache.overridesForFunctions.getValue(original, this)
            processor(function)
        }

        return super.processFunctionsByName(name, processor)
    }

    override fun processDirectOverriddenFunctionsWithBaseScope(
        functionSymbol: FirNamedFunctionSymbol,
        processor: (FirNamedFunctionSymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction =
        processDirectOverriddenWithBaseScope(
            functionSymbol,
            processor,
            FirTypeScope::processDirectOverriddenFunctionsWithBaseScope,
        ) { it in substitutionOverrideCache.overridesForFunctions }

    private inline fun <reified D : FirCallableSymbol<*>> processDirectOverriddenWithBaseScope(
        callableSymbol: D,
        noinline processor: (D, FirTypeScope) -> ProcessorAction,
        processDirectOverriddenCallablesWithBaseScope: FirTypeScope.(D, ((D, FirTypeScope) -> ProcessorAction)) -> ProcessorAction,
        originalInCache: (D) -> Boolean
    ): ProcessorAction {
        konst original = callableSymbol.originalForSubstitutionOverride?.takeIf { originalInCache(it) }
            ?: return useSiteMemberScope.processDirectOverriddenCallablesWithBaseScope(callableSymbol, processor)

        if (original != callableSymbol) {
            if (!processor(original, useSiteMemberScope)) return ProcessorAction.STOP
        }

        return useSiteMemberScope.processDirectOverriddenCallablesWithBaseScope(original, processor)
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        return useSiteMemberScope.processPropertiesByName(name) process@{ original ->
            konst symbol = if (original is FirPropertySymbol || original is FirFieldSymbol) {
                substitutionOverrideCache.overridesForVariables.getValue(original, this)
            } else {
                original
            }
            processor(symbol)
        }
    }

    override fun processDirectOverriddenPropertiesWithBaseScope(
        propertySymbol: FirPropertySymbol,
        processor: (FirPropertySymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction =
        processDirectOverriddenWithBaseScope(
            propertySymbol, processor, FirTypeScope::processDirectOverriddenPropertiesWithBaseScope,
        ) { it in substitutionOverrideCache.overridesForVariables }

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        useSiteMemberScope.processClassifiersByNameWithSubstitution(name) { symbol, substitutor ->
            processor(symbol, substitutor.chain(this.substitutor))
        }
    }

    private fun ConeKotlinType.substitute(): ConeKotlinType? {
        return substitutor.substituteOrNull(this)
    }

    private fun ConeKotlinType.substitute(substitutor: ConeSubstitutor): ConeKotlinType? {
        return substitutor.substituteOrNull(this)
    }

    private fun ConeSimpleKotlinType.substitute(substitutor: ConeSubstitutor): ConeSimpleKotlinType? {
        return substitutor.substituteOrNull(this)?.lowerBoundIfFlexible()
    }

    fun createSubstitutionOverrideFunction(original: FirNamedFunctionSymbol): FirNamedFunctionSymbol {
        if (substitutor == ConeSubstitutor.Empty) return original
        original.lazyResolveToPhase(FirResolvePhase.TYPES)
        konst member = original.fir
        if (skipPrivateMembers && member.visibility == Visibilities.Private) return original

        konst symbolForOverride = FirFakeOverrideGenerator.createSymbolForSubstitutionOverride(original, newOwnerClassId)

        konst (newTypeParameters, newDispatchReceiverType, newReceiverType, newReturnType, newSubstitutor, fakeOverrideSubstitution) = createSubstitutedData(
            member, symbolForOverride
        )
        konst newParameterTypes = member.konstueParameters.map {
            it.returnTypeRef.coneType.substitute(newSubstitutor)
        }

        konst newContextReceiverTypes = member.contextReceivers.map {
            it.typeRef.coneType.substitute(newSubstitutor)
        }

        if (newReceiverType == null &&
            newReturnType == null &&
            newParameterTypes.all { it == null } &&
            newTypeParameters === member.typeParameters &&
            fakeOverrideSubstitution == null &&
            newContextReceiverTypes.all { it == null }
        ) {
            if (original.dispatchReceiverType?.substitute(substitutor) != null) {
                return FirFakeOverrideGenerator.createSubstitutionOverrideFunction(
                    session,
                    symbolForOverride,
                    member,
                    derivedClassLookupTag = derivedClassLookupTag,
                    newDispatchReceiverType ?: dispatchReceiverTypeForSubstitutedMembers,
                    isExpect = makeExpect,
                    origin = origin,
                )
            }
            return original
        }

        /*
         * Member functions can't capture type parameters, so
         *   it's safe to cast newTypeParameters to List<FirTypeParameter>
         */
        @Suppress("UNCHECKED_CAST")
        return FirFakeOverrideGenerator.createSubstitutionOverrideFunction(
            session,
            symbolForOverride,
            member,
            derivedClassLookupTag,
            newDispatchReceiverType ?: dispatchReceiverTypeForSubstitutedMembers,
            origin,
            newReceiverType,
            newContextReceiverTypes,
            newReturnType,
            newParameterTypes,
            newTypeParameters as List<FirTypeParameter>,
            makeExpect,
            fakeOverrideSubstitution
        )
    }

    fun createSubstitutionOverrideConstructor(original: FirConstructorSymbol): FirConstructorSymbol {
        if (substitutor == ConeSubstitutor.Empty) return original
        original.lazyResolveToPhase(FirResolvePhase.TYPES)
        konst constructor = original.fir

        konst symbolForOverride = FirConstructorSymbol(original.callableId)
        konst (newTypeParameters, _, _, newReturnType, newSubstitutor, fakeOverrideSubstitution) =
            createSubstitutedData(constructor, symbolForOverride)

        // If constructor has a dispatch receiver, it should be an inner class' constructor.
        // It means that we need to substitute its dispatcher as every other type,
        // instead of using dispatchReceiverTypeForSubstitutedMembers
        konst newDispatchReceiverType = original.dispatchReceiverType?.substitute(substitutor)

        konst newParameterTypes = constructor.konstueParameters.map {
            it.returnTypeRef.coneType.substitute(newSubstitutor)
        }

        konst newContextReceiverTypes = constructor.contextReceivers.map {
            it.typeRef.coneType.substitute(newSubstitutor)
        }

        if (newReturnType == null &&
            newParameterTypes.all { it == null } &&
            newTypeParameters === constructor.typeParameters &&
            newContextReceiverTypes.all { it == null }
        ) {
            return original
        }

        return FirFakeOverrideGenerator.createCopyForFirConstructor(
            symbolForOverride,
            session,
            constructor,
            derivedClassLookupTag,
            origin,
            newDispatchReceiverType,
            // Constructors' return types are expected to be non-flexible (i.e., non raw)
            newReturnType?.lowerBoundIfFlexible(),
            newParameterTypes,
            newContextReceiverTypes,
            newTypeParameters,
            makeExpect,
            fakeOverrideSubstitution
        ).symbol
    }

    fun createSubstitutionOverrideProperty(original: FirPropertySymbol): FirPropertySymbol {
        if (substitutor == ConeSubstitutor.Empty) return original
        original.lazyResolveToPhase(FirResolvePhase.TYPES)
        konst member = original.fir
        if (skipPrivateMembers && member.visibility == Visibilities.Private) return original

        konst symbolForOverride = FirFakeOverrideGenerator.createSymbolForSubstitutionOverride(original, newOwnerClassId)

        konst (newTypeParameters, newDispatchReceiverType, newReceiverType, newReturnType, _, fakeOverrideSubstitution) = createSubstitutedData(
            member, symbolForOverride
        )

        konst newContextReceiverTypes = member.contextReceivers.map {
            it.typeRef.coneType.substitute(substitutor)
        }

        if (newReceiverType == null &&
            newReturnType == null &&
            newTypeParameters === member.typeParameters &&
            fakeOverrideSubstitution == null &&
            newContextReceiverTypes.all { it == null }
        ) {
            if (original.dispatchReceiverType?.substitute(substitutor) != null) {
                return FirFakeOverrideGenerator.createSubstitutionOverrideProperty(
                    session,
                    symbolForOverride,
                    member,
                    derivedClassLookupTag = derivedClassLookupTag,
                    newDispatchReceiverType ?: dispatchReceiverTypeForSubstitutedMembers,
                    origin,
                    isExpect = makeExpect,
                )
            }
            return original
        }

        @Suppress("UNCHECKED_CAST")
        return FirFakeOverrideGenerator.createSubstitutionOverrideProperty(
            session,
            symbolForOverride,
            member,
            derivedClassLookupTag,
            newDispatchReceiverType ?: dispatchReceiverTypeForSubstitutedMembers,
            origin,
            newReceiverType,
            newContextReceiverTypes,
            newReturnType,
            newTypeParameters as List<FirTypeParameter>,
            makeExpect,
            fakeOverrideSubstitution
        )
    }

    private data class SubstitutedData(
        konst typeParameters: List<FirTypeParameterRef>,
        konst dispatchReceiverType: ConeSimpleKotlinType?,
        konst receiverType: ConeKotlinType?,
        konst returnType: ConeKotlinType?,
        konst substitutor: ConeSubstitutor,
        konst fakeOverrideSubstitution: FakeOverrideSubstitution?
    )

    private fun createSubstitutedData(member: FirCallableDeclaration, symbolForOverride: FirBasedSymbol<*>): SubstitutedData {
        konst memberOwnerClassLookupTag =
            if (member is FirConstructor) (member.returnTypeRef.coneType as ConeClassLikeType).lookupTag
            else member.dispatchReceiverClassLookupTagOrNull()
        konst (newTypeParameters, substitutor) = FirFakeOverrideGenerator.createNewTypeParametersAndSubstitutor(
            session,
            member as FirTypeParameterRefsOwner,
            symbolForOverride,
            substitutor,
            origin,
            forceTypeParametersRecreation = dispatchReceiverTypeForSubstitutedMembers.lookupTag != memberOwnerClassLookupTag
        )

        konst receiverType = member.receiverParameter?.typeRef?.coneType
        konst newReceiverType = receiverType?.substitute(substitutor)

        konst newDispatchReceiverType = dispatchReceiverTypeForSubstitutedMembers.substitute(substitutor)

        konst returnType = member.returnTypeRef.coneTypeSafe<ConeKotlinType>()
        konst fakeOverrideSubstitution = runIf(returnType == null) { FakeOverrideSubstitution(substitutor, member.symbol) }
        konst newReturnType = returnType?.substitute(substitutor)
        return SubstitutedData(
            newTypeParameters,
            newDispatchReceiverType,
            newReceiverType,
            newReturnType,
            substitutor,
            fakeOverrideSubstitution
        )
    }

    fun createSubstitutionOverrideField(original: FirFieldSymbol): FirFieldSymbol {
        if (substitutor == ConeSubstitutor.Empty) return original
        original.lazyResolveToPhase(FirResolvePhase.TYPES)
        konst member = original.fir
        if (skipPrivateMembers && member.visibility == Visibilities.Private) return original

        konst returnType = member.returnTypeRef.coneTypeSafe<ConeKotlinType>()
        // TODO: do we have fields with implicit type?
        konst newReturnType = returnType?.substitute() ?: return original

        return FirFakeOverrideGenerator.createSubstitutionOverrideField(session, member, derivedClassLookupTag, newReturnType, origin)
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        useSiteMemberScope.processDeclaredConstructors process@{ original ->
            konst constructor = substitutionOverrideCache.overridesForConstructors.getValue(original, this)
            processor(constructor)
        }
    }

    override fun getCallableNames(): Set<Name> {
        return useSiteMemberScope.getCallableNames()
    }

    override fun getClassifierNames(): Set<Name> {
        return useSiteMemberScope.getClassifierNames()
    }

    override fun toString(): String {
        return "Substitution scope for [$useSiteMemberScope] for type $dispatchReceiverTypeForSubstitutedMembers"
    }
}

class FirSubstitutionOverrideStorage(konst session: FirSession) : FirSessionComponent {
    private konst cachesFactory = session.firCachesFactory

    konst substitutionOverrideCacheByScope: FirCache<ScopeSessionKey<*, *>, SubstitutionOverrideCache, Nothing?> =
        cachesFactory.createCache { _ -> SubstitutionOverrideCache(session.firCachesFactory) }

    class SubstitutionOverrideCache(cachesFactory: FirCachesFactory) {
        konst overridesForFunctions: FirCache<FirNamedFunctionSymbol, FirNamedFunctionSymbol, FirClassSubstitutionScope> =
            cachesFactory.createCache { original, scope -> scope.createSubstitutionOverrideFunction(original) }
        konst overridesForConstructors: FirCache<FirConstructorSymbol, FirConstructorSymbol, FirClassSubstitutionScope> =
            cachesFactory.createCache { original, scope -> scope.createSubstitutionOverrideConstructor(original) }
        konst overridesForVariables: FirCache<FirVariableSymbol<*>, FirVariableSymbol<*>, FirClassSubstitutionScope> =
            cachesFactory.createCache { original, scope ->
                when (original) {
                    is FirPropertySymbol -> scope.createSubstitutionOverrideProperty(original)
                    is FirFieldSymbol -> scope.createSubstitutionOverrideField(original)
                    else -> error("symbol $original is not overridable")
                }
            }
    }
}

private konst FirSession.substitutionOverrideStorage: FirSubstitutionOverrideStorage by FirSession.sessionComponentAccessor()
