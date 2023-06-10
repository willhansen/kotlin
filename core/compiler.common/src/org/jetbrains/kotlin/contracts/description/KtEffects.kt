/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.contracts.description

/**
 * Effect with condition attached to it.
 *
 * [condition] is some expression, which result-type is Boolean, and clause should
 * be interpreted as: "if [effect] took place then [condition]-expression is
 * guaranteed to be true"
 *
 * NB. [effect] and [condition] connected with implication in math logic sense:
 * [effect] => [condition]. In particular this means that:
 *  - there can be multiple ways how [effect] can be produced, but for any of them
 *    [condition] holds.
 *  - if [effect] wasn't observed, we *can't* reason that [condition] is false
 *  - if [condition] is true, we *can't* reason that [effect] will be observed.
 */
class KtConditionalEffectDeclaration<Type, Diagnostic>(
    konst effect: KtEffectDeclaration<Type, Diagnostic>,
    konst condition: KtBooleanExpression<Type, Diagnostic>
) : KtEffectDeclaration<Type, Diagnostic>() {
    override konst erroneous: Boolean
        get() = effect.erroneous || condition.erroneous

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R =
        contractDescriptionVisitor.visitConditionalEffectDeclaration(this, data)
}


/**
 * Effect which specifies that subroutine returns some particular konstue
 */
class KtReturnsEffectDeclaration<Type, Diagnostic>(konst konstue: KtConstantReference<Type, Diagnostic>) :
    KtEffectDeclaration<Type, Diagnostic>() {
    override konst erroneous: Boolean
        get() = konstue.erroneous

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R =
        contractDescriptionVisitor.visitReturnsEffectDeclaration(this, data)
}


/**
 * Effect which specifies, that during execution of subroutine, callable [konstueParameterReference] will be invoked
 * [kind] amount of times, and will never be invoked after subroutine call is finished.
 */
open class KtCallsEffectDeclaration<Type, Diagnostic>(
    konst konstueParameterReference: KtValueParameterReference<Type, Diagnostic>,
    konst kind: EventOccurrencesRange
) : KtEffectDeclaration<Type, Diagnostic>() {
    override konst erroneous: Boolean
        get() = konstueParameterReference.erroneous

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R =
        contractDescriptionVisitor.visitCallsEffectDeclaration(this, data)
}

class KtErroneousCallsEffectDeclaration<Type, Diagnostic>(
    konstueParameterReference: KtValueParameterReference<Type, Diagnostic>,
    konst diagnostic: Diagnostic
) : KtCallsEffectDeclaration<Type, Diagnostic>(konstueParameterReference, EventOccurrencesRange.UNKNOWN) {
    override konst erroneous: Boolean
        get() = true

    override fun <R, D> accept(contractDescriptionVisitor: KtContractDescriptionVisitor<R, D, Type, Diagnostic>, data: D): R =
        contractDescriptionVisitor.visitErroneousCallsEffectDeclaration(this, data)
}
