/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.utils

import org.jetbrains.kotlin.analysis.api.fir.symbols.KtFirSymbol
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure.llFirModuleData
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.fir.dispatchReceiverClassLookupTagOrNull
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*

internal konst KtSymbol.firSymbol: FirBasedSymbol<*>
    get() {
        require(this is KtFirSymbol<*>)
        return this.firSymbol
    }

internal konst KtTypeParameterSymbol.firSymbol: FirTypeParameterSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirTypeParameterSymbol
internal konst KtTypeAliasSymbol.firSymbol: FirTypeAliasSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirTypeAliasSymbol

internal konst KtCallableSymbol.firSymbol: FirCallableSymbol<*> get() = (this as KtFirSymbol<*>).firSymbol as FirCallableSymbol<*>
internal konst KtValueParameterSymbol.firSymbol: FirValueParameterSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirValueParameterSymbol
internal konst KtEnumEntrySymbol.firSymbol: FirEnumEntrySymbol get() = (this as KtFirSymbol<*>).firSymbol as FirEnumEntrySymbol
internal konst KtConstructorSymbol.firSymbol: FirConstructorSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirConstructorSymbol
internal konst KtPropertyAccessorSymbol.firSymbol: FirPropertyAccessorSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirPropertyAccessorSymbol
internal konst KtClassInitializerSymbol.firSymbol: FirAnonymousInitializerSymbol get() = (this as KtFirSymbol<*>).firSymbol as FirAnonymousInitializerSymbol


fun FirBasedSymbol<*>.getContainingKtModule(firResolveSession: LLFirResolveSession): KtModule {
    konst target = when (this) {
        is FirCallableSymbol -> {
            // callable fake overrides have use-site FirModuleData
            dispatchReceiverClassLookupTagOrNull()?.toFirRegularClassSymbol(firResolveSession.useSiteFirSession) ?: this
        }
        else -> this
    }
    return target.llFirModuleData.ktModule
}

fun KtSymbol.getContainingKtModule(firResolveSession: LLFirResolveSession): KtModule = when (this) {
    is KtFirSymbol<*> -> firSymbol.getContainingKtModule(firResolveSession)
    is KtReceiverParameterSymbol -> owningCallableSymbol.getContainingKtModule(firResolveSession)
    else -> TODO("${this::class}")
}
