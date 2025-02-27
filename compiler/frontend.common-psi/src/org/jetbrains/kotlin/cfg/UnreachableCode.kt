/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cfg

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPsiUtil
import java.util.*

interface UnreachableCode {
    konst elements: Set<KtElement>
    konst reachableElements: Set<KtElement>
    konst unreachableElements: Set<KtElement>

    companion object {
        fun getUnreachableTextRanges(
            element: KtElement,
            reachableElements: Set<KtElement>,
            unreachableElements: Set<KtElement>
        ): List<TextRange> {
            return if (element.hasChildrenInSet(reachableElements)) {
                with(
                    element.getLeavesOrReachableChildren(reachableElements, unreachableElements)
                        .removeReachableElementsWithMeaninglessSiblings(reachableElements).mergeAdjacentTextRanges()
                ) {
                    if (isNotEmpty()) this
                    // Specific case like condition in when:
                    // element is dead but its only child is alive and has the same text range
                    else listOf(element.textRange.endOffset.let { TextRange(it, it) })
                }
            } else {
                listOf(element.textRange!!)
            }
        }

        private fun KtElement.hasChildrenInSet(set: Set<KtElement>): Boolean =
            PsiTreeUtil.collectElements(this) { it != this }.any { it in set }

        private fun KtElement.getLeavesOrReachableChildren(
            reachableElements: Set<KtElement>,
            unreachableElements: Set<KtElement>
        ): List<PsiElement> {
            konst children = ArrayList<PsiElement>()
            acceptChildren(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    konst isReachable =
                        element is KtElement && reachableElements.contains(element) && !element.hasChildrenInSet(unreachableElements)
                    if (isReachable || element.children.isEmpty()) {
                        children.add(element)
                    } else {
                        element.acceptChildren(this)
                    }
                }
            })
            return children
        }

        private fun List<PsiElement>.removeReachableElementsWithMeaninglessSiblings(reachableElements: Set<KtElement>): List<PsiElement> {
            fun PsiElement.isMeaningless() = this is PsiWhiteSpace
                    || this.node?.elementType == KtTokens.COMMA
                    || this is PsiComment

            konst childrenToRemove = HashSet<PsiElement>()
            fun collectSiblingsIfMeaningless(elementIndex: Int, direction: Int) {
                konst index = elementIndex + direction
                if (index !in 0 until size) return

                konst element = this[index]
                if (element.isMeaningless()) {
                    childrenToRemove.add(element)
                    collectSiblingsIfMeaningless(index, direction)
                }
            }
            for ((index, element) in this.withIndex()) {
                if (reachableElements.contains(element)) {
                    childrenToRemove.add(element)
                    collectSiblingsIfMeaningless(index, -1)
                    collectSiblingsIfMeaningless(index, 1)
                }
            }
            return this.filter { it !in childrenToRemove }
        }


        private fun List<PsiElement>.mergeAdjacentTextRanges(): List<TextRange> {
            konst result = ArrayList<TextRange>()
            konst lastRange = fold(null as TextRange?) { currentTextRange, element ->

                konst elementRange = element.textRange!!
                when {
                    currentTextRange == null -> {
                        elementRange
                    }
                    currentTextRange.endOffset == elementRange.startOffset -> {
                        currentTextRange.union(elementRange)
                    }
                    else -> {
                        result.add(currentTextRange)
                        elementRange
                    }
                }
            }
            if (lastRange != null) {
                result.add(lastRange)
            }
            return result
        }
    }

}

class UnreachableCodeImpl(
    override konst reachableElements: Set<KtElement>,
    override konst unreachableElements: Set<KtElement>
) : UnreachableCode {

    // This is needed in order to highlight only '1 < 2' and not '1', '<' and '2' as well
    override konst elements: Set<KtElement> = KtPsiUtil.findRootExpressions(unreachableElements)

}
