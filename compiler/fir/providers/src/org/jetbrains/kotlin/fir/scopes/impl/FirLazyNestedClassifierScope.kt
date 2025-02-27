/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.FirContainingNamesAwareScope
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

// TODO: we could get rid of this scope and use FirNestedClassifierScope instead,
// but in this case we should make JavaSymbolProvider greedy related to nested classifiers
// (or make possible to calculate nested classifiers on-the-fly)
class FirLazyNestedClassifierScope(
    konst classId: ClassId,
    private konst existingNames: List<Name>,
    private konst symbolProvider: FirSymbolProvider
) : FirContainingNamesAwareScope() {
    override fun processClassifiersByNameWithSubstitution(
        name: Name,
        processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit
    ) {
        if (name !in existingNames) {
            return
        }
        konst child = classId.createNestedClassId(name)
        konst symbol = symbolProvider.getClassLikeSymbolByClassId(child) ?: return

        processor(symbol, ConeSubstitutor.Empty)
    }

    override fun getClassifierNames(): Set<Name> = existingNames.toSet()

    override fun getCallableNames(): Set<Name> = emptySet()
}
