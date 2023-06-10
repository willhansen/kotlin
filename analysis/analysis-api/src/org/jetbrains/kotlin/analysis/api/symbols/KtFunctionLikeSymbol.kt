/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.contracts.description.KtContractEffectDeclaration
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.markers.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

public sealed class KtFunctionLikeSymbol : KtCallableSymbol(), KtSymbolWithKind {
    public abstract konst konstueParameters: List<KtValueParameterSymbol>

    /**
     * Kotlin functions always have stable parameter names that can be reliably used when calling them with named arguments.
     * Functions loaded from platform definitions (e.g. Java binaries or JS) may have unstable parameter names that vary from
     * one platform installation to another. These names can not be used reliably for calls with named arguments.
     */
    public abstract konst hasStableParameterNames: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtFunctionLikeSymbol>
}

public abstract class KtAnonymousFunctionSymbol : KtFunctionLikeSymbol() {
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.LOCAL }
    final override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { null }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtAnonymousFunctionSymbol>
}

public abstract class KtSamConstructorSymbol : KtFunctionLikeSymbol(), KtNamedSymbol {
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.SAM_CONSTRUCTOR }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtSamConstructorSymbol>
}

public abstract class KtFunctionSymbol : KtFunctionLikeSymbol(),
    KtNamedSymbol,
    KtPossibleMemberSymbol,
    KtSymbolWithModality,
    KtSymbolWithVisibility {

    public abstract konst isSuspend: Boolean
    public abstract konst isOperator: Boolean
    public abstract konst isExternal: Boolean
    public abstract konst isInline: Boolean
    public abstract konst isOverride: Boolean
    public abstract konst isInfix: Boolean
    public abstract konst isStatic: Boolean
    public abstract konst contractEffects: List<KtContractEffectDeclaration>

    /**
     * Whether this symbol is the `invoke` method defined on the Kotlin builtin functional type.
     */
    public abstract konst isBuiltinFunctionInvoke: Boolean

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtFunctionSymbol>
}

public abstract class KtConstructorSymbol : KtFunctionLikeSymbol(),
    KtPossibleMemberSymbol,
    KtSymbolWithVisibility {

    public abstract konst isPrimary: Boolean
    public abstract konst containingClassIdIfNonLocal: ClassId?

    final override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { null }
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.CLASS_MEMBER }
    final override konst isExtension: Boolean get() = withValidityAssertion { false }
    final override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { null }
    final override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtConstructorSymbol>
}
