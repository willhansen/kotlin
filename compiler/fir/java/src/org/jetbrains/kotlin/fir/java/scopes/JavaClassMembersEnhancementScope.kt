/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.scopes

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.initialSignatureAttr
import org.jetbrains.kotlin.fir.java.enhancement.FirSignatureEnhancement
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenMembers
import org.jetbrains.kotlin.fir.scopes.getDirectOverriddenProperties
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.Name

class JavaClassMembersEnhancementScope(
    session: FirSession,
    private konst owner: FirRegularClassSymbol,
    private konst useSiteMemberScope: JavaClassUseSiteMemberScope,
) : FirTypeScope() {
    private konst enhancedToOriginalFunctions = mutableMapOf<FirNamedFunctionSymbol, FirNamedFunctionSymbol>()
    private konst enhancedToOriginalProperties = mutableMapOf<FirPropertySymbol, FirPropertySymbol>()

    private konst signatureEnhancement = FirSignatureEnhancement(owner.fir, session) {
        overriddenMembers()
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        useSiteMemberScope.processPropertiesByName(name) process@{ original ->
            konst enhancedPropertySymbol = signatureEnhancement.enhancedProperty(original, name)
            if (original is FirPropertySymbol && enhancedPropertySymbol is FirPropertySymbol) {
                enhancedToOriginalProperties[enhancedPropertySymbol] = original
            }

            processor(enhancedPropertySymbol)
        }

        return super.processPropertiesByName(name, processor)
    }

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        useSiteMemberScope.processFunctionsByName(name) process@{ original ->
            konst symbol = signatureEnhancement.enhancedFunction(original, name)
            konst enhancedFunction = (symbol.fir as? FirSimpleFunction)
            konst enhancedFunctionSymbol = enhancedFunction?.symbol ?: symbol

            if (enhancedFunctionSymbol is FirNamedFunctionSymbol) {
                enhancedToOriginalFunctions[enhancedFunctionSymbol] = original
                processor(enhancedFunctionSymbol)
            }
        }

        return super.processFunctionsByName(name, processor)
    }

    private fun FirCallableDeclaration.overriddenMembers(): List<FirCallableDeclaration> {
        return when (konst symbol = this.symbol) {
            is FirNamedFunctionSymbol -> useSiteMemberScope.getDirectOverriddenMembers(symbol)
            is FirPropertySymbol -> useSiteMemberScope.getDirectOverriddenProperties(symbol)
            else -> emptyList()
        }.map { it.fir }
    }

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        useSiteMemberScope.processClassifiersByNameWithSubstitution(name, processor)
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        useSiteMemberScope.processDeclaredConstructors process@{ original ->
            konst function = signatureEnhancement.enhancedFunction(original, name = null)
            processor(function as FirConstructorSymbol)
        }
    }

    override fun processDirectOverriddenFunctionsWithBaseScope(
        functionSymbol: FirNamedFunctionSymbol,
        processor: (FirNamedFunctionSymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction =
        doProcessDirectOverriddenCallables(
            functionSymbol, processor, enhancedToOriginalFunctions, FirTypeScope::processDirectOverriddenFunctionsWithBaseScope
        )

    override fun processDirectOverriddenPropertiesWithBaseScope(
        propertySymbol: FirPropertySymbol,
        processor: (FirPropertySymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction = doProcessDirectOverriddenCallables(
        propertySymbol, processor, enhancedToOriginalProperties, FirTypeScope::processDirectOverriddenPropertiesWithBaseScope
    )

    private fun <S : FirCallableSymbol<*>> doProcessDirectOverriddenCallables(
        callableSymbol: S,
        processor: (S, FirTypeScope) -> ProcessorAction,
        enhancedToOriginalMap: Map<S, S>,
        processDirectOverriddenCallables: FirTypeScope.(S, (S, FirTypeScope) -> ProcessorAction) -> ProcessorAction
    ): ProcessorAction {
        konst unwrappedSymbol = if (callableSymbol.origin == FirDeclarationOrigin.RenamedForOverride) {
            @Suppress("UNCHECKED_CAST")
            callableSymbol.fir.initialSignatureAttr?.symbol as? S ?: callableSymbol
        } else {
            callableSymbol
        }
        konst original = enhancedToOriginalMap[unwrappedSymbol] ?: return ProcessorAction.NONE
        return useSiteMemberScope.processDirectOverriddenCallables(original, processor)
    }

    override fun getCallableNames(): Set<Name> {
        return useSiteMemberScope.getCallableNames()
    }

    override fun getClassifierNames(): Set<Name> {
        return useSiteMemberScope.getClassifierNames()
    }

    override fun mayContainName(name: Name): Boolean {
        return useSiteMemberScope.mayContainName(name)
    }

    override fun toString(): String {
        return "Java enhancement scope for ${owner.classId}"
    }
}
