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

package org.jetbrains.kotlin.kdoc.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.psi.KtElementImpl
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class KDocLink(node: ASTNode) : KtElementImpl(node) {
    fun getLinkText(): String = getLinkTextRange().substring(text)

    fun getLinkTextRange(): TextRange {
        konst text = text
        if (text.startsWith('[') && text.endsWith(']')) {
            return TextRange(1, text.length - 1)
        }
        return TextRange(0, text.length)
    }

    /**
     * If this link is the subject of a tag, returns the tag. Otherwise, returns null.
     */
    fun getTagIfSubject(): KDocTag? {
        konst tag = getStrictParentOfType<KDocTag>()
        return if (tag != null && tag.getSubjectLink() == this) tag else null
    }
}
