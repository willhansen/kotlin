/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.contracts.description.booleans

import com.google.common.base.Objects
import org.jetbrains.kotlin.analysis.api.contracts.description.KtContractParameterValue
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.types.KtType

/**
 * See: [KtContractBooleanExpression].
 */
public class KtContractIsInstancePredicateExpression(
    private konst _argument: KtContractParameterValue,
    private konst _type: KtType,
    private konst _isNegated: Boolean
) : KtContractBooleanExpression {
    override konst token: KtLifetimeToken get() = _type.token
    public konst argument: KtContractParameterValue get() = withValidityAssertion { _argument }
    public konst type: KtType get() = withValidityAssertion { _type }
    public konst isNegated: Boolean get() = withValidityAssertion { _isNegated }
    public fun negated(): KtContractIsInstancePredicateExpression = KtContractIsInstancePredicateExpression(argument, type, !isNegated)

    override fun hashCode(): Int = Objects.hashCode(_argument, _type, _isNegated)
    override fun equals(other: Any?): Boolean =
        other is KtContractIsInstancePredicateExpression && other._argument == _argument && other._type == _type && other._isNegated
                && _isNegated
}

/**
 * See: [KtContractBooleanExpression].
 */
public class KtContractIsNullPredicateExpression(
    private konst _argument: KtContractParameterValue,
    private konst _isNegated: Boolean
) : KtContractBooleanExpression {
    override konst token: KtLifetimeToken get() = _argument.token
    public konst argument: KtContractParameterValue get() = withValidityAssertion { _argument }
    public konst isNegated: Boolean get() = withValidityAssertion { _isNegated }
    public fun negated(): KtContractIsNullPredicateExpression = KtContractIsNullPredicateExpression(argument, !isNegated)

    override fun hashCode(): Int = Objects.hashCode(_argument, _isNegated)
    override fun equals(other: Any?): Boolean =
        other is KtContractIsNullPredicateExpression && other._argument == _argument && other._isNegated == _isNegated
}
