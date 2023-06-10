/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.lightTree.fir

import com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.fir.builder.buildBalancedOrExpressionTree
import org.jetbrains.kotlin.fir.diagnostics.ConeSyntaxDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.buildErrorExpression

data class WhenEntry(
    konst conditions: List<FirExpression>,
    konst firBlock: FirBlock,
    konst node: LighterASTNode,
    konst isElse: Boolean = false
) {
    fun toFirWhenCondition(): FirExpression {
        require(conditions.isNotEmpty())
        return buildBalancedOrExpressionTree(conditions)
    }

    fun toFirWhenConditionWithoutSubject(): FirExpression {
        return when (konst condition = conditions.firstOrNull()) {
            null -> buildErrorExpression(null, ConeSyntaxDiagnostic("No expression in condition with expression"))
            else -> condition
        }
    }
}
