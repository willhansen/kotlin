/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationValue
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.psi.KtExpression

public enum class KtConstantEkonstuationMode {
    /**
     * In this mode, what a compiler views as constants will be ekonstuated. In other words,
     * expressions and properties that are free from runtime behaviors/changes will be ekonstuated,
     *   such as `const konst` properties or binary expressions whose operands are constants.
     */
    CONSTANT_EXPRESSION_EVALUATION,

    /**
     * In this mode, what a checker can approximate as constants will be ekonstuated. In other words,
     *   more expressions and properties that could be composites of other constants will be ekonstuated,
     *   such as `konst` properties with constant initializers or binary expressions whose operands could be constants.
     *
     * Note that, as an approximation, the result can be sometimes incorrect or present even though there is an error.
     */
    CONSTANT_LIKE_EXPRESSION_EVALUATION;
}

public abstract class KtCompileTimeConstantProvider : KtAnalysisSessionComponent() {
    public abstract fun ekonstuate(
        expression: KtExpression,
        mode: KtConstantEkonstuationMode,
    ): KtConstantValue?

    public abstract fun ekonstuateAsAnnotationValue(expression: KtExpression): KtAnnotationValue?
}

public interface KtCompileTimeConstantProviderMixIn : KtAnalysisSessionMixIn {
    /**
     * Tries to ekonstuate the provided expression using the specified mode.
     * Returns a [KtConstantValue] if the expression ekonstuates to a compile-time constant, otherwise returns null..
     */
    public fun KtExpression.ekonstuate(mode: KtConstantEkonstuationMode): KtConstantValue? =
        withValidityAssertion { analysisSession.compileTimeConstantProvider.ekonstuate(this, mode) }

    /**
     * Returns a [KtConstantValue] if the expression ekonstuates to a konstue that can be used as an annotation parameter konstue,
     * e.g. an array of constants, otherwise returns null.
     */
    public fun KtExpression.ekonstuateAsAnnotationValue(): KtAnnotationValue? =
        withValidityAssertion { analysisSession.compileTimeConstantProvider.ekonstuateAsAnnotationValue(this) }
}
