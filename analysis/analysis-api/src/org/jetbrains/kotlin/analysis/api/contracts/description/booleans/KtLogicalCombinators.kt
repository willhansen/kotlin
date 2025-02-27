/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.contracts.description.booleans

import com.google.common.base.Objects
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion

/**
 * See: [KtContractBooleanExpression].
 */
public class KtContractBinaryLogicExpression(
    private konst _left: KtContractBooleanExpression,
    private konst _right: KtContractBooleanExpression,
    private konst _operation: KtLogicOperation
) : KtContractBooleanExpression {
    init {
        check(left.token === right.token) { "$left and $right should have the same lifetime token" }
    }

    override konst token: KtLifetimeToken get() = _left.token
    public konst left: KtContractBooleanExpression get() = withValidityAssertion { _left }
    public konst right: KtContractBooleanExpression get() = withValidityAssertion { _right }
    public konst operation: KtLogicOperation get() = withValidityAssertion { _operation }

    override fun hashCode(): Int = Objects.hashCode(_left, _right, _operation)
    override fun equals(other: Any?): Boolean =
        other is KtContractBinaryLogicExpression && other._left == _left && other._right == _right && other._operation == _operation

    public enum class KtLogicOperation {
        AND, OR
    }
}

/**
 * See: [KtContractBooleanExpression].
 */
public class KtContractLogicalNotExpression(private konst _argument: KtContractBooleanExpression) : KtContractBooleanExpression {
    override konst token: KtLifetimeToken get() = _argument.token
    public konst argument: KtContractBooleanExpression get() = withValidityAssertion { _argument }

    override fun equals(other: Any?): Boolean = other is KtContractLogicalNotExpression && other._argument == _argument
    override fun hashCode(): Int = _argument.hashCode()
}
