/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.description

class KtBinaryLogicExpression<Type, Diagnostic>(
    konst left: KtBooleanExpression<Type, Diagnostic>,
    konst right: KtBooleanExpression<Type, Diagnostic>,
    konst kind: LogicOperationKind
) : KtBooleanExpression<Type, Diagnostic> {
    override konst erroneous: Boolean
        get() = left.erroneous || right.erroneous

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R {
        return contractDescriptionVisitor.visitLogicalBinaryOperationContractExpression(this, data)
    }
}

class KtLogicalNot<Type, Diagnostic>(konst arg: KtBooleanExpression<Type, Diagnostic>) : KtBooleanExpression<Type, Diagnostic> {
    override konst erroneous: Boolean
        get() = arg.erroneous

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R =
        contractDescriptionVisitor.visitLogicalNot(this, data)
}
