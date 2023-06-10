/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols.markers

import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.name.Name

public interface KtPossiblyNamedSymbol : KtSymbol {
    public konst name: Name?
}

public interface KtNamedSymbol : KtPossiblyNamedSymbol {
    override konst name: Name
}

public interface KtSymbolWithTypeParameters : KtSymbol {
    public konst typeParameters: List<KtTypeParameterSymbol>
}