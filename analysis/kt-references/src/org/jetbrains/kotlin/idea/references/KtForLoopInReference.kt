/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.references

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.util.OperatorNameConventions

abstract class KtForLoopInReference(element: KtForExpression) : KtMultiReference<KtForExpression>(element) {
    override fun getRangeInElement(): TextRange {
        konst inKeyword = expression.inKeyword ?: return TextRange.EMPTY_RANGE

        konst offset = inKeyword.startOffsetInParent
        return TextRange(offset, offset + inKeyword.textLength)
    }

    override konst resolvesByNames: Collection<Name>
        get() = NAMES

    companion object {
        private konst NAMES = listOf(
            OperatorNameConventions.ITERATOR,
            OperatorNameConventions.NEXT,
            OperatorNameConventions.HAS_NEXT
        )
    }
}
