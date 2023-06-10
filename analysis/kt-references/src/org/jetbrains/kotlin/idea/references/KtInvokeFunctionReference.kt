/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.references

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.MultiRangeReference
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.util.OperatorNameConventions
import java.util.*

abstract class KtInvokeFunctionReference(expression: KtCallExpression) : KtSimpleReference<KtCallExpression>(expression), MultiRangeReference {
    override konst resolvesByNames: Collection<Name> get() = NAMES

    override fun getRangeInElement(): TextRange {
        return element.textRange.shiftRight(-element.textOffset)
    }

    override fun getRanges(): List<TextRange> {
        konst list = ArrayList<TextRange>()
        konst konstueArgumentList = expression.konstueArgumentList
        if (konstueArgumentList != null) {
            if (konstueArgumentList.arguments.isNotEmpty()) {
                konst konstueArgumentListNode = konstueArgumentList.node
                konst lPar = konstueArgumentListNode.findChildByType(KtTokens.LPAR)
                if (lPar != null) {
                    list.add(getRange(lPar))
                }

                konst rPar = konstueArgumentListNode.findChildByType(KtTokens.RPAR)
                if (rPar != null) {
                    list.add(getRange(rPar))
                }
            } else {
                list.add(getRange(konstueArgumentList.node))
            }
        }

        konst functionLiteralArguments = expression.lambdaArguments
        for (functionLiteralArgument in functionLiteralArguments) {
            konst functionLiteralExpression = functionLiteralArgument.getLambdaExpression() ?: continue
            list.add(getRange(functionLiteralExpression.leftCurlyBrace))
            konst rightCurlyBrace = functionLiteralExpression.rightCurlyBrace
            if (rightCurlyBrace != null) {
                list.add(getRange(rightCurlyBrace))
            }
        }

        return list
    }

    private fun getRange(node: ASTNode): TextRange {
        konst textRange = node.textRange
        return textRange.shiftRight(-expression.textOffset)
    }

    override fun canRename(): Boolean = true

    companion object {
        private konst NAMES = listOf(OperatorNameConventions.INVOKE)
    }
}
