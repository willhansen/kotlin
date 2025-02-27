/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.contracts.description

import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtParameterSymbol

/**
 * Represents constant reference that can be passed to `konstue` argument of [kotlin.contracts.ContractBuilder.returns].
 *
 * See: [org.jetbrains.kotlin.analysis.api.contracts.description.KtContractReturnsContractEffectDeclaration.KtContractReturnsSpecificValueEffectDeclaration.konstue]
 */
public class KtContractConstantValue(
    private konst _constantType: KtContractConstantType,
    override konst token: KtLifetimeToken,
) : KtLifetimeOwner {
    public enum class KtContractConstantType {
        NULL, TRUE, FALSE;
    }

    public konst constantType: KtContractConstantType get() = withValidityAssertion { _constantType }

    override fun equals(other: Any?): Boolean = other is KtContractConstantValue && other._constantType == _constantType
    override fun hashCode(): Int = _constantType.hashCode()
}

/**
 * Represents parameter that can be passed to `konstue` argument of [kotlin.contracts.ContractBuilder.callsInPlace].
 */
public class KtContractParameterValue(private konst _parameterSymbol: KtParameterSymbol) : KtLifetimeOwner {
    override konst token: KtLifetimeToken get() = _parameterSymbol.token
    public konst parameterSymbol: KtParameterSymbol get() = withValidityAssertion { _parameterSymbol }

    override fun hashCode(): Int = _parameterSymbol.hashCode()
    override fun equals(other: Any?): Boolean = other is KtContractParameterValue && other._parameterSymbol == _parameterSymbol
}
