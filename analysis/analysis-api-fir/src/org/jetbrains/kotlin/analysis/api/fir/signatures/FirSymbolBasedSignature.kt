/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.signatures

import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

internal interface FirSymbolBasedSignature {
    konst firSymbol: FirCallableSymbol<*>
    konst firSymbolBuilder: KtSymbolByFirBuilder
}