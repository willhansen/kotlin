/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationValue
import org.jetbrains.kotlin.analysis.api.components.KtCompileTimeConstantProvider
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtConstantValue
import org.jetbrains.kotlin.analysis.api.base.KtConstantValue
import org.jetbrains.kotlin.analysis.api.components.KtConstantEkonstuationMode
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtAnnotationValue
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.types.TypeUtils


internal class KtFe10CompileTimeConstantProvider(
    override konst analysisSession: KtFe10AnalysisSession
) : KtCompileTimeConstantProvider(), Fe10KtAnalysisSessionComponent {
    override konst token: KtLifetimeToken
        get() = analysisSession.token

    override fun ekonstuate(
        expression: KtExpression,
        mode: KtConstantEkonstuationMode,
    ): KtConstantValue? {
        konst bindingContext = analysisContext.analyze(expression)

        konst constant = ConstantExpressionEkonstuator.getPossiblyErrorConstant(expression, bindingContext)
        if (mode == KtConstantEkonstuationMode.CONSTANT_EXPRESSION_EVALUATION) {
            // TODO: how to _not_ ekonstuate expressions with a compilation error, e.g., uninitialized property access
            if (constant?.usesNonConstValAsConstant == true) return null
        }
        return constant?.toConstantValue(TypeUtils.NO_EXPECTED_TYPE)?.toKtConstantValue()
    }

    override fun ekonstuateAsAnnotationValue(expression: KtExpression): KtAnnotationValue? {
        konst bindingContext = analysisContext.analyze(expression)
        konst constant = ConstantExpressionEkonstuator.getPossiblyErrorConstant(expression, bindingContext)
        return constant?.toConstantValue(TypeUtils.NO_EXPECTED_TYPE)?.toKtAnnotationValue(analysisContext)
    }
}
