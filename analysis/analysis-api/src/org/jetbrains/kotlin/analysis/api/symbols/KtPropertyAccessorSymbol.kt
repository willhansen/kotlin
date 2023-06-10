/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.markers.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer

public sealed class KtPropertyAccessorSymbol : KtFunctionLikeSymbol(),
    KtPossibleMemberSymbol,
    KtSymbolWithModality,
    KtSymbolWithVisibility,
    KtSymbolWithKind {

    final override konst isExtension: Boolean get() = withValidityAssertion { false }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    public abstract konst isDefault: Boolean
    public abstract konst isInline: Boolean
    public abstract konst isOverride: Boolean
    public abstract konst hasBody: Boolean

    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.ACCESSOR }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtPropertyAccessorSymbol>
}

public abstract class KtPropertyGetterSymbol : KtPropertyAccessorSymbol() {
    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtPropertyGetterSymbol>
}

public abstract class KtPropertySetterSymbol : KtPropertyAccessorSymbol() {
    public abstract konst parameter: KtValueParameterSymbol

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtPropertySetterSymbol>
}
