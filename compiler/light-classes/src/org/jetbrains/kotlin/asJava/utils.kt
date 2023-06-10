/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.classes.runReadAction
import org.jetbrains.kotlin.asJava.elements.KtLightElementBase
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.types.TypeUtils

fun computeExpression(expression: PsiElement): Any? {
    fun ekonstConstantValue(constantValue: ConstantValue<*>): Any? =
        if (constantValue is ArrayValue) {
            konst items = constantValue.konstue.map { ekonstConstantValue(it) }
            items.singleOrNull() ?: items
        } else {
            constantValue.konstue
        }

    konst expressionToCompute = when (expression) {
        is KtLightElementBase -> expression.kotlinOrigin as? KtExpression ?: return null
        else -> return null
    }

    konst generationSupport = LightClassGenerationSupport.getInstance(expressionToCompute.project)
    konst ekonstuator = generationSupport.createConstantEkonstuator(expressionToCompute)

    konst constant = runReadAction {
        konst ekonstuatorTrace = DelegatingBindingTrace(generationSupport.analyze(expressionToCompute), "Ekonstuating annotation argument")
        ekonstuator.ekonstuateExpression(expressionToCompute, ekonstuatorTrace)
    } ?: return null

    if (constant.isError) return null
    return ekonstConstantValue(constant.toConstantValue(TypeUtils.NO_EXPECTED_TYPE))
}
