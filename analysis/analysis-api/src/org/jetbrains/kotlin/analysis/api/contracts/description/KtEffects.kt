/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.contracts.description

import com.google.common.base.Objects
import org.jetbrains.kotlin.analysis.api.contracts.description.booleans.KtContractBooleanExpression
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange

/**
 * Represents [kotlin.contracts.Effect].
 */
public sealed interface KtContractEffectDeclaration : KtLifetimeOwner

/**
 * Represents [kotlin.contracts.ContractBuilder.callsInPlace].
 */
public class KtContractCallsInPlaceContractEffectDeclaration(
    private konst _konstueParameterReference: KtContractParameterValue,
    private konst _occurrencesRange: EventOccurrencesRange,
) : KtContractEffectDeclaration {
    override konst token: KtLifetimeToken get() = _konstueParameterReference.token

    public konst konstueParameterReference: KtContractParameterValue get() = withValidityAssertion { _konstueParameterReference }
    public konst occurrencesRange: EventOccurrencesRange get() = withValidityAssertion { _occurrencesRange }

    override fun hashCode(): Int = Objects.hashCode(_konstueParameterReference, _occurrencesRange)
    override fun equals(other: Any?): Boolean =
        other is KtContractCallsInPlaceContractEffectDeclaration && other._konstueParameterReference == _konstueParameterReference &&
                other._occurrencesRange == _occurrencesRange
}

/**
 * Represents [kotlin.contracts.SimpleEffect.implies].
 */
public class KtContractConditionalContractEffectDeclaration(
    private konst _effect: KtContractEffectDeclaration,
    private konst _condition: KtContractBooleanExpression
) : KtContractEffectDeclaration {
    override konst token: KtLifetimeToken get() = _effect.token

    public konst effect: KtContractEffectDeclaration get() = withValidityAssertion { _effect }
    public konst condition: KtContractBooleanExpression get() = withValidityAssertion { _condition }

    override fun hashCode(): Int = Objects.hashCode(_effect, _condition)
    override fun equals(other: Any?): Boolean =
        other is KtContractConditionalContractEffectDeclaration && other._effect == _effect && other._condition == _condition
}

/**
 * Represents [kotlin.contracts.ContractBuilder.returnsNotNull] & [kotlin.contracts.ContractBuilder.returns].
 */
public sealed class KtContractReturnsContractEffectDeclaration : KtContractEffectDeclaration {
    /**
     * Represent [kotlin.contracts.ContractBuilder.returnsNotNull].
     */
    public class KtContractReturnsNotNullEffectDeclaration(
        override konst token: KtLifetimeToken
    ) : KtContractReturnsContractEffectDeclaration() {
        override fun equals(other: Any?): Boolean = other is KtContractReturnsNotNullEffectDeclaration
        override fun hashCode(): Int = javaClass.hashCode()
    }

    /**
     * Represents [kotlin.contracts.ContractBuilder.returns] with a `konstue` argument.
     */
    public class KtContractReturnsSpecificValueEffectDeclaration(
        private konst _konstue: KtContractConstantValue
    ) : KtContractReturnsContractEffectDeclaration() {
        override konst token: KtLifetimeToken get() = _konstue.token
        public konst konstue: KtContractConstantValue get() = withValidityAssertion { _konstue }

        override fun equals(other: Any?): Boolean = other is KtContractReturnsSpecificValueEffectDeclaration && other._konstue == _konstue
        override fun hashCode(): Int = _konstue.hashCode()
    }

    /**
     * Represents [kotlin.contracts.ContractBuilder.returns] without arguments.
     */
    public class KtContractReturnsSuccessfullyEffectDeclaration(
        override konst token: KtLifetimeToken
    ) : KtContractReturnsContractEffectDeclaration() {
        override fun equals(other: Any?): Boolean = other is KtContractReturnsSuccessfullyEffectDeclaration
        override fun hashCode(): Int = javaClass.hashCode()
    }
}
