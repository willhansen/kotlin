/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.scopeSessionKey
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.SmartList

class FirPackageMemberScope(
    konst fqName: FqName,
    konst session: FirSession,
    private konst symbolProvider: FirSymbolProvider = session.symbolProvider
) : FirScope() {
    private konst classifierCache: MutableMap<Name, FirClassifierSymbol<*>?> = mutableMapOf()
    private konst functionCache: MutableMap<Name, List<FirNamedFunctionSymbol>> = mutableMapOf()
    private konst propertyCache: MutableMap<Name, List<FirPropertySymbol>> = mutableMapOf()

    override fun processClassifiersByNameWithSubstitution(
        name: Name,
        processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit
    ) {
        if (name.asString().isEmpty()) return

        konst symbol = classifierCache.getOrPut(name) {
            konst unambiguousFqName = ClassId(fqName, name)
            symbolProvider.getClassLikeSymbolByClassId(unambiguousFqName)
        }

        if (symbol != null) {
            processor(symbol, ConeSubstitutor.Empty)
        }
    }

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        konst symbols = functionCache.getOrPut(name) {
            symbolProvider.getTopLevelFunctionSymbols(fqName, name)
        }
        for (symbol in symbols) {
            processor(symbol)
        }
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        konst symbols = propertyCache.getOrPut(name) {
            symbolProvider.getTopLevelPropertySymbols(fqName, name)
        }
        for (symbol in symbols) {
            processor(symbol)
        }
    }

    override konst scopeOwnerLookupNames: List<String> = SmartList(fqName.asString())
}

konst PACKAGE_MEMBER = scopeSessionKey<FqName, FirPackageMemberScope>()
