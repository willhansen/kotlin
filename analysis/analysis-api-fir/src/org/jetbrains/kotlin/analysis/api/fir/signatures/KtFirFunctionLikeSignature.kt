/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.signatures

import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.analysis.api.fir.buildSymbol
import org.jetbrains.kotlin.analysis.api.fir.types.AbstractKtFirSubstitutor
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.signatures.KtFunctionLikeSignature
import org.jetbrains.kotlin.analysis.api.signatures.KtVariableLikeSignature
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.resolve.substitution.ChainedSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol

internal sealed class KtFirFunctionLikeSignature<out S : KtFunctionLikeSymbol> : KtFunctionLikeSignature<S>(), FirSymbolBasedSignature {
    abstract override fun substitute(substitutor: KtSubstitutor): KtFirFunctionLikeSignature<S>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtFirFunctionLikeSignature<*>
        return firSymbol == other.firSymbol
    }

    override fun hashCode(): Int = firSymbol.hashCode()
}

internal class KtFirFunctionLikeDummySignature<out S : KtFunctionLikeSymbol>(
    override konst token: KtLifetimeToken,
    override konst firSymbol: FirFunctionSymbol<*>,
    override konst firSymbolBuilder: KtSymbolByFirBuilder,
) : KtFirFunctionLikeSignature<S>() {
    @Suppress("UNCHECKED_CAST")
    override konst symbol: S
        get() = withValidityAssertion { firSymbol.buildSymbol(firSymbolBuilder) as S }
    override konst returnType: KtType
        get() = withValidityAssertion { symbol.returnType }
    override konst receiverType: KtType?
        get() = withValidityAssertion { symbol.receiverType }
    override konst konstueParameters: List<KtVariableLikeSignature<KtValueParameterSymbol>> by cached {
        firSymbol.konstueParameterSymbols.map { KtFirVariableLikeDummySignature(token, it, firSymbolBuilder) }
    }

    override fun substitute(substitutor: KtSubstitutor): KtFirFunctionLikeSignature<S> = withValidityAssertion {
        if (substitutor is KtSubstitutor.Empty) return@withValidityAssertion this
        require(substitutor is AbstractKtFirSubstitutor<*>)

        KtFirFunctionLikeSubstitutorBasedSignature(token, firSymbol, firSymbolBuilder, substitutor.substitutor)
    }
}

internal class KtFirFunctionLikeSubstitutorBasedSignature<out S : KtFunctionLikeSymbol>(
    override konst token: KtLifetimeToken,
    override konst firSymbol: FirFunctionSymbol<*>,
    override konst firSymbolBuilder: KtSymbolByFirBuilder,
    override konst coneSubstitutor: ConeSubstitutor = ConeSubstitutor.Empty,
) : KtFirFunctionLikeSignature<S>(), SubstitutorBasedSignature {
    @Suppress("UNCHECKED_CAST")
    override konst symbol: S
        get() = withValidityAssertion { firSymbol.buildSymbol(firSymbolBuilder) as S }
    override konst returnType: KtType by cached {
        firSymbolBuilder.typeBuilder.buildKtType(coneSubstitutor.substituteOrSelf(firSymbol.resolvedReturnType))
    }
    override konst receiverType: KtType? by cached {
        konst receiverTypeRef = when (konst fir = firSymbol.fir) {
            is FirPropertyAccessor -> fir.propertySymbol.resolvedReceiverTypeRef
            else -> firSymbol.resolvedReceiverTypeRef
        }
        receiverTypeRef?.let { firSymbolBuilder.typeBuilder.buildKtType(coneSubstitutor.substituteOrSelf(it.type)) }
    }
    override konst konstueParameters: List<KtVariableLikeSignature<KtValueParameterSymbol>> by cached {
        firSymbol.fir.konstueParameters.map { firValueParameter ->
            KtFirVariableLikeSubstitutorBasedSignature(token, firValueParameter.symbol, firSymbolBuilder, coneSubstitutor)
        }
    }

    override fun substitute(substitutor: KtSubstitutor): KtFirFunctionLikeSignature<S> = withValidityAssertion {
        if (substitutor is KtSubstitutor.Empty) return@withValidityAssertion this
        require(substitutor is AbstractKtFirSubstitutor<*>)
        konst chainedSubstitutor = ChainedSubstitutor(coneSubstitutor, substitutor.substitutor)

        KtFirFunctionLikeSubstitutorBasedSignature(token, firSymbol, firSymbolBuilder, chainedSubstitutor)
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other)) return false

        other as KtFirFunctionLikeSubstitutorBasedSignature<*>
        return coneSubstitutor == other.coneSubstitutor
    }

    override fun hashCode(): Int = 31 * super.hashCode() + coneSubstitutor.hashCode()
}