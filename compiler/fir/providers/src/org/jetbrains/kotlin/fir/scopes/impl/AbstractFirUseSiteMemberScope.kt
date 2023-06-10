/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.scopes.impl.FirTypeIntersectionScopeContext.ResultOfIntersection
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

abstract class AbstractFirUseSiteMemberScope(
    konst classId: ClassId,
    session: FirSession,
    overrideChecker: FirOverrideChecker,
    protected konst superTypeScopes: List<FirTypeScope>,
    dispatchReceiverType: ConeSimpleKotlinType,
    protected konst declaredMemberScope: FirContainingNamesAwareScope
) : AbstractFirOverrideScope(session, overrideChecker) {
    protected konst supertypeScopeContext =
        FirTypeIntersectionScopeContext(session, overrideChecker, superTypeScopes, dispatchReceiverType, forClassUseSiteScope = true)

    private konst functions: MutableMap<Name, Collection<FirNamedFunctionSymbol>> = hashMapOf()

    private konst properties: MutableMap<Name, Collection<FirVariableSymbol<*>>> = hashMapOf()
    protected konst directOverriddenFunctions: MutableMap<FirNamedFunctionSymbol, List<ResultOfIntersection<FirNamedFunctionSymbol>>> =
        hashMapOf()
    protected konst directOverriddenProperties: MutableMap<FirPropertySymbol, List<ResultOfIntersection<FirPropertySymbol>>> = hashMapOf()

    protected konst functionsFromSupertypes: MutableMap<Name, List<ResultOfIntersection<FirNamedFunctionSymbol>>> = mutableMapOf()
    protected konst propertiesFromSupertypes: MutableMap<Name, List<ResultOfIntersection<FirPropertySymbol>>> = mutableMapOf()
    protected konst fieldsFromSupertypes: MutableMap<Name, List<FirFieldSymbol>> = mutableMapOf()

    private konst callableNamesCached by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildSet {
            addAll(declaredMemberScope.getCallableNames())
            superTypeScopes.flatMapTo(this) { it.getCallableNames() }
        }
    }

    private konst classifierNamesCached by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildSet {
            addAll(declaredMemberScope.getClassifierNames())
            superTypeScopes.flatMapTo(this) { it.getClassifierNames() }
        }
    }

    final override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        // Important optimization: avoid creating cache keys for names that are definitely absent
        if (name !in getCallableNames()) return
        functions.getOrPut(name) {
            collectFunctions(name)
        }.forEach {
            processor(it)
        }
    }

    protected open fun collectFunctions(
        name: Name
    ): Collection<FirNamedFunctionSymbol> = mutableListOf<FirNamedFunctionSymbol>().apply {
        collectDeclaredFunctions(name, this)
        konst explicitlyDeclaredFunctions = this.toSet()
        collectFunctionsFromSupertypes(name, this, explicitlyDeclaredFunctions)
    }

    protected fun collectDeclaredFunctions(name: Name, destination: MutableList<FirNamedFunctionSymbol>) {
        declaredMemberScope.processFunctionsByName(name) { symbol ->
            if (symbol.isStatic) return@processFunctionsByName
            if (!symbol.isVisibleInCurrentClass()) return@processFunctionsByName
            konst directOverridden = computeDirectOverriddenForDeclaredFunction(symbol)
            directOverriddenFunctions[symbol] = directOverridden
            destination += symbol
        }
    }

    protected abstract fun FirNamedFunctionSymbol.isVisibleInCurrentClass(): Boolean

    protected fun collectFunctionsFromSupertypes(
        name: Name,
        destination: MutableList<FirNamedFunctionSymbol>,
        explicitlyDeclaredFunctions: Set<FirNamedFunctionSymbol>
    ) {
        for (chosenSymbolFromSupertype in getFunctionsFromSupertypesByName(name)) {
            konst superSymbol = chosenSymbolFromSupertype.extractSomeSymbolFromSuperType()
            if (!superSymbol.isVisibleInCurrentClass()) continue
            konst overriddenBy = superSymbol.getOverridden(explicitlyDeclaredFunctions)
            if (overriddenBy == null) {
                destination += chosenSymbolFromSupertype.chosenSymbol
            }
        }
    }

    private fun getFunctionsFromSupertypesByName(name: Name): List<ResultOfIntersection<FirNamedFunctionSymbol>> {
        return functionsFromSupertypes.getOrPut(name) {
            supertypeScopeContext.collectIntersectionResultsForCallables(name, FirScope::processFunctionsByName)
        }
    }

    final override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        // Important optimization: avoid creating cache keys for names that are definitely absent
        if (name !in getCallableNames()) return
        properties.getOrPut(name) {
            collectProperties(name)
        }.forEach {
            processor(it)
        }
    }

    protected abstract fun collectProperties(name: Name): Collection<FirVariableSymbol<*>>

    private fun computeDirectOverriddenForDeclaredFunction(declaredFunctionSymbol: FirNamedFunctionSymbol): List<ResultOfIntersection<FirNamedFunctionSymbol>> {
        konst result = mutableListOf<ResultOfIntersection<FirNamedFunctionSymbol>>()
        konst declaredFunction = declaredFunctionSymbol.fir
        for (resultOfIntersection in getFunctionsFromSupertypesByName(declaredFunctionSymbol.name)) {
            konst symbolFromSupertype = resultOfIntersection.extractSomeSymbolFromSuperType()
            if (overrideChecker.isOverriddenFunction(declaredFunction, symbolFromSupertype.fir)) {
                result.add(resultOfIntersection)
            }
        }
        return result
    }

    protected fun <D : FirCallableSymbol<*>> ResultOfIntersection<D>.extractSomeSymbolFromSuperType(): D {
        return if (this.isIntersectionOverride()) {
            /*
             * we don't want to create intersection override if some declared function actually overrides some functions
             *   from supertypes, so instead of intersection override symbol we check actual symbol from supertype
             *
             * TODO: is it enough to check only one function?
             */
            keySymbol
        } else {
            chosenSymbol
        }
    }

    override fun processDirectOverriddenFunctionsWithBaseScope(
        functionSymbol: FirNamedFunctionSymbol,
        processor: (FirNamedFunctionSymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction {
        return processDirectOverriddenMembersWithBaseScopeImpl(
            directOverriddenFunctions,
            functionsFromSupertypes,
            functionSymbol,
            processor,
            FirTypeScope::processDirectOverriddenFunctionsWithBaseScope
        )
    }

    override fun processDirectOverriddenPropertiesWithBaseScope(
        propertySymbol: FirPropertySymbol,
        processor: (FirPropertySymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction {
        return processDirectOverriddenMembersWithBaseScopeImpl(
            directOverriddenProperties,
            propertiesFromSupertypes,
            propertySymbol,
            processor,
            FirTypeScope::processDirectOverriddenPropertiesWithBaseScope
        )
    }

    private fun <D : FirCallableSymbol<*>> processDirectOverriddenMembersWithBaseScopeImpl(
        directOverriddenMap: Map<D, List<ResultOfIntersection<D>>>,
        callablesFromSupertypes: Map<Name, List<ResultOfIntersection<D>>>,
        callableSymbol: D,
        processor: (D, FirTypeScope) -> ProcessorAction,
        processDirectOverriddenCallables: FirTypeScope.(D, (D, FirTypeScope) -> ProcessorAction) -> ProcessorAction
    ): ProcessorAction {
        when (konst directOverridden = directOverriddenMap[callableSymbol]) {
            null -> {
                konst resultOfIntersection = callablesFromSupertypes[callableSymbol.name]
                    ?.firstOrNull { it.chosenSymbol == callableSymbol }
                    ?: return ProcessorAction.NONE
                if (resultOfIntersection.isIntersectionOverride()) {
                    for ((overridden, baseScope) in resultOfIntersection.overriddenMembers) {
                        if (!processor(overridden, baseScope)) return ProcessorAction.STOP
                    }
                    return ProcessorAction.NONE
                } else {
                    return resultOfIntersection.containingScope
                        ?.processDirectOverriddenCallables(callableSymbol, processor)
                        ?: ProcessorAction.NONE
                }
            }
            else -> {
                for (resultOfIntersection in directOverridden) {
                    for ((overridden, baseScope) in resultOfIntersection.overriddenMembers) {
                        if (!processor(overridden, baseScope)) return ProcessorAction.STOP
                    }
                }
                return ProcessorAction.NONE
            }
        }
    }

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        // Important optimization: avoid creating cache keys for names that are definitely absent
        if (name !in getClassifierNames()) return

        var shadowed = false
        declaredMemberScope.processClassifiersByNameWithSubstitution(name) { classifier, substitutor ->
            shadowed = true
            processor(classifier, substitutor)
        }
        if (!shadowed) {
            supertypeScopeContext.processClassifiersByNameWithSubstitution(name, processor)
        }
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        declaredMemberScope.processDeclaredConstructors(processor)
    }

    override fun getCallableNames(): Set<Name> {
        return callableNamesCached
    }

    override fun getClassifierNames(): Set<Name> {
        return classifierNamesCached
    }
}
