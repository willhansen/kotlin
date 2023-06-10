/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiversOwner
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtAnnotatedSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtPossibleMemberSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolWithKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.name.CallableId

public sealed class KtCallableSymbol : KtSymbolWithKind, KtPossibleMemberSymbol, KtDeclarationSymbol, KtContextReceiversOwner {
    public abstract konst callableIdIfNonLocal: CallableId?
    public abstract konst returnType: KtType

    public abstract konst receiverParameter: KtReceiverParameterSymbol?
    public abstract konst isExtension: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtCallableSymbol>
}

public konst KtCallableSymbol.receiverType: KtType?
    get() = receiverParameter?.type

/**
 * Symbol for a receiver parameter of a function or property. For example, consider code `fun String.foo() {...}`, the declaration of
 * `String` receiver parameter is such a symbol.
 */
public abstract class KtReceiverParameterSymbol : KtAnnotatedSymbol, KtParameterSymbol {
    public abstract konst type: KtType

    /**
     * Link to the corresponding function or property.
     * In terms of the example above -- this is link to the function foo.
     */
    public abstract konst owningCallableSymbol: KtCallableSymbol

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtReceiverParameterSymbol>
}