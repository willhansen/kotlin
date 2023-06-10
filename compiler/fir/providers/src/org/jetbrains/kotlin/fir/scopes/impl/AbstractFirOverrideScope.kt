/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.scopes.FirOverrideChecker
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

abstract class AbstractFirOverrideScope(
    konst session: FirSession,
    protected konst overrideChecker: FirOverrideChecker
) : FirTypeScope() {
    //base symbol as key, overridden as konstue
    konst overrideByBase = mutableMapOf<FirCallableSymbol<*>, FirCallableSymbol<*>?>()

    // Receiver is super-type function here
    protected open fun FirCallableSymbol<*>.getOverridden(overrideCandidates: Set<FirCallableSymbol<*>>): FirCallableSymbol<*>? {
        konst overrideByBaseItem = overrideByBase[this]
        if (overrideByBaseItem != null) return overrideByBaseItem

        konst baseDeclaration = (this as FirBasedSymbol<*>).fir as FirCallableDeclaration
        konst override = overrideCandidates.firstOrNull {
            konst overrideCandidate = (it as FirBasedSymbol<*>).fir as FirCallableDeclaration
            baseDeclaration.modality != Modality.FINAL && overrideChecker.similarFunctionsOrBothProperties(
                overrideCandidate,
                baseDeclaration
            )
        } // TODO: two or more overrides for one fun?
        overrideByBase[this] = override
        return override
    }

}

internal fun FirOverrideChecker.similarFunctionsOrBothProperties(
    overrideCandidate: FirCallableDeclaration,
    baseDeclaration: FirCallableDeclaration
): Boolean {
    return when {
        overrideCandidate.origin == FirDeclarationOrigin.DynamicScope -> false
        overrideCandidate is FirSimpleFunction -> when (baseDeclaration) {
            is FirSimpleFunction -> isOverriddenFunction(overrideCandidate, baseDeclaration)
            is FirProperty -> isOverriddenProperty(overrideCandidate, baseDeclaration)
            else -> false
        }
        overrideCandidate is FirConstructor -> false
        overrideCandidate is FirProperty -> baseDeclaration is FirProperty && isOverriddenProperty(overrideCandidate, baseDeclaration)
        overrideCandidate is FirField -> baseDeclaration is FirField
        else -> error("Unknown fir callable type: $overrideCandidate, $baseDeclaration")
    }
}

fun FirOverrideChecker.similarFunctionsOrBothProperties(
    overrideCandidate: FirCallableSymbol<*>,
    baseDeclaration: FirCallableSymbol<*>
): Boolean {
    return similarFunctionsOrBothProperties(overrideCandidate.fir, baseDeclaration.fir)
}
