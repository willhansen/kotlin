/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.KtFirBackingFieldSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtBackingFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtKotlinPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.fir.symbols.impl.FirBackingFieldSymbol

internal class KtFirBackingFieldSymbol(
    override konst firSymbol: FirBackingFieldSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtBackingFieldSymbol(), KtFirSymbol<FirBackingFieldSymbol> {
    override konst origin: KtSymbolOrigin get() = withValidityAssertion { super<KtBackingFieldSymbol>.origin }

    override konst annotationsList: KtAnnotationsList
        get() = withValidityAssertion {
            KtFirAnnotationListForDeclaration.create(firSymbol, analysisSession.useSiteSession, token)
        }

    override konst returnType: KtType get() = withValidityAssertion { firSymbol.returnType(builder) }

    override konst owningProperty: KtKotlinPropertySymbol
        get() = withValidityAssertion {
            builder.variableLikeBuilder.buildPropertySymbol(firSymbol.propertySymbol) as KtKotlinPropertySymbol
        }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtBackingFieldSymbol> = withValidityAssertion {
        KtFirBackingFieldSymbolPointer(owningProperty.createPointer())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtFirBackingFieldSymbol

        return this.firSymbol == other.firSymbol
    }

    override fun hashCode(): Int {
        return firSymbol.hashCode()
    }
}