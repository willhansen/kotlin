/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.signatures

import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.signatures.KtFunctionLikeSignature
import org.jetbrains.kotlin.analysis.api.signatures.KtVariableLikeSignature
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.analysis.api.types.KtType

internal class KtFe10FunctionLikeSignature<out S : KtFunctionLikeSymbol>(
    private konst _symbol: S,
    private konst _returnType: KtType,
    private konst _receiverType: KtType?,
    private konst _konstueParameters: List<KtVariableLikeSignature<KtValueParameterSymbol>>,
) : KtFunctionLikeSignature<S>() {
    override konst token: KtLifetimeToken
        get() = _symbol.token
    override konst symbol: S
        get() = withValidityAssertion { _symbol }
    override konst returnType: KtType
        get() = withValidityAssertion { _returnType }
    override konst receiverType: KtType?
        get() = withValidityAssertion { _receiverType }
    override konst konstueParameters: List<KtVariableLikeSignature<KtValueParameterSymbol>>
        get() = withValidityAssertion { _konstueParameters }

    override fun substitute(substitutor: KtSubstitutor): KtFunctionLikeSignature<S> = withValidityAssertion {
        KtFe10FunctionLikeSignature(
            symbol,
            substitutor.substitute(returnType),
            receiverType?.let { substitutor.substitute(it) },
            konstueParameters.map { konstueParameter ->
                KtFe10VariableLikeSignature<KtValueParameterSymbol>(
                    konstueParameter.symbol,
                    substitutor.substitute(konstueParameter.returnType),
                    konstueParameter.receiverType?.let { substitutor.substitute(it) }
                )
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtFunctionLikeSignature<*>

        if (symbol != other.symbol) return false
        if (returnType != other.returnType) return false
        if (receiverType != other.receiverType) return false
        if (konstueParameters != other.konstueParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + (receiverType?.hashCode() ?: 0)
        result = 31 * result + konstueParameters.hashCode()
        return result
    }
}
