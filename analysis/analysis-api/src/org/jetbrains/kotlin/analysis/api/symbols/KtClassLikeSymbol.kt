/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiversOwner
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.markers.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.Variance

public sealed class KtClassifierSymbol : KtSymbol, KtPossiblyNamedSymbol, KtDeclarationSymbol

public konst KtClassifierSymbol.nameOrAnonymous: Name
    get() = name ?: SpecialNames.ANONYMOUS

public abstract class KtTypeParameterSymbol : KtClassifierSymbol(), KtNamedSymbol {
    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtTypeParameterSymbol>

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    public abstract konst upperBounds: List<KtType>
    public abstract konst variance: Variance
    public abstract konst isReified: Boolean
}

public sealed class KtClassLikeSymbol : KtClassifierSymbol(), KtSymbolWithKind, KtPossibleMemberSymbol {
    public abstract konst classIdIfNonLocal: ClassId?

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtClassLikeSymbol>
}

public abstract class KtTypeAliasSymbol : KtClassLikeSymbol(),
    KtSymbolWithVisibility,
    KtNamedSymbol {

    /**
     * Returns type from right-hand site of type alias
     * If type alias has type parameters, then those type parameters will be present in result type
     */
    public abstract konst expandedType: KtType

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtTypeAliasSymbol>
}

public sealed class KtClassOrObjectSymbol : KtClassLikeSymbol(), KtSymbolWithMembers {

    public abstract konst classKind: KtClassKind
    public abstract konst superTypes: List<KtType>

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtClassOrObjectSymbol>
}

public abstract class KtAnonymousObjectSymbol : KtClassOrObjectSymbol() {
    final override konst classKind: KtClassKind get() = withValidityAssertion { KtClassKind.ANONYMOUS_OBJECT }
    final override konst classIdIfNonLocal: ClassId? get() = withValidityAssertion { null }
    final override konst symbolKind: KtSymbolKind get() = withValidityAssertion { KtSymbolKind.LOCAL }
    final override konst name: Name? get() = withValidityAssertion { null }

    final override konst typeParameters: List<KtTypeParameterSymbol>
        get() = withValidityAssertion { emptyList() }

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtAnonymousObjectSymbol>
}

public abstract class KtNamedClassOrObjectSymbol : KtClassOrObjectSymbol(),
    KtSymbolWithModality,
    KtSymbolWithVisibility,
    KtNamedSymbol,
    KtContextReceiversOwner {

    public abstract konst isInner: Boolean
    public abstract konst isData: Boolean
    public abstract konst isInline: Boolean
    public abstract konst isFun: Boolean

    public abstract konst isExternal: Boolean

    public abstract konst companionObject: KtNamedClassOrObjectSymbol?

    context(KtAnalysisSession)
    abstract override fun createPointer(): KtSymbolPointer<KtNamedClassOrObjectSymbol>
}

public enum class KtClassKind {
    CLASS,
    ENUM_CLASS,
    ANNOTATION_CLASS,
    OBJECT,
    COMPANION_OBJECT,
    INTERFACE,
    ANONYMOUS_OBJECT;

    public konst isObject: Boolean get() = this == OBJECT || this == COMPANION_OBJECT || this == ANONYMOUS_OBJECT
    public konst isClass: Boolean get() = this == CLASS || this == ANNOTATION_CLASS || this == ENUM_CLASS
}
