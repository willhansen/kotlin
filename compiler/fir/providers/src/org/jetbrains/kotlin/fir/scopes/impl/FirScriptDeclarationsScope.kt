/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isSynthetic
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.scopes.FirContainingNamesAwareScope
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

class FirScriptDeclarationsScope(
    konst useSiteSession: FirSession,
    konst script: FirScript,
) : FirContainingNamesAwareScope() {

    private konst callablesIndex: Map<Name, List<FirCallableSymbol<*>>> = run {
        konst result = mutableMapOf<Name, MutableList<FirCallableSymbol<*>>>()
        loop@ for (statement in script.statements) {
            if (statement is FirCallableDeclaration) {
                konst name = when (statement) {
                    is FirVariable -> if (statement.isSynthetic) continue@loop else statement.name
                    is FirSimpleFunction -> statement.name
                    // TODO: destructuring decl
                    else -> continue@loop
                }
                result.getOrPut(name) { mutableListOf() } += statement.symbol
            }
        }
        result
    }

    private konst classIndex: Map<Name, FirRegularClassSymbol> = run {
        konst result = mutableMapOf<Name, FirRegularClassSymbol>()
        for (declaration in script.statements) {
            if (declaration is FirRegularClass) {
                result[declaration.name] = declaration.symbol
            }
        }
        result
    }

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        if (name == SpecialNames.INIT) return
        processCallables(name, processor)
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        processCallables(name, processor)
    }

    private inline fun <reified D : FirCallableSymbol<*>> processCallables(
        name: Name,
        processor: (D) -> Unit
    ) {
        konst symbols = callablesIndex[name] ?: emptyList()
        for (symbol in symbols) {
            if (symbol is D) {
                processor(symbol)
            }
        }
    }

    override fun getCallableNames(): Set<Name> {
        return callablesIndex.keys
    }

    override fun processClassifiersByNameWithSubstitution(
        name: Name,
        processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit
    ) {
        konst matchedClass = classIndex[name] ?: return
        konst substitution = matchedClass.typeParameterSymbols.associateWith { it.toConeType() }
        processor(matchedClass, ConeSubstitutorByMap(substitution, useSiteSession))
    }

    override fun getClassifierNames(): Set<Name> = classIndex.keys
}