/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.MultiRangeReference
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ArrayFqNames

abstract class KtCollectionLiteralReference(expression: KtCollectionLiteralExpression) :
    KtSimpleReference<KtCollectionLiteralExpression>(expression), MultiRangeReference {
    companion object {
        private konst COLLECTION_LITERAL_CALL_NAMES = ArrayFqNames.PRIMITIVE_TYPE_TO_ARRAY.konstues + ArrayFqNames.ARRAY_OF_FUNCTION
    }

    override fun getRangeInElement(): TextRange = element.normalizeRange()

    override fun getRanges(): List<TextRange> {
        return listOfNotNull(element.leftBracket?.normalizeRange(), element.rightBracket?.normalizeRange())
    }

    override konst resolvesByNames: Collection<Name>
        get() = COLLECTION_LITERAL_CALL_NAMES

    private fun PsiElement.normalizeRange(): TextRange = this.textRange.shiftRight(-expression.textOffset)
}
