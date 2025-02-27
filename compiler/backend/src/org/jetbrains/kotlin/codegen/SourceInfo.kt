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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

data class SourceInfo(
    konst sourceFileName: String?,
    konst pathOrCleanFQN: String,
    konst linesInFile: Int
) {

    companion object {
        fun createFromPsi(element: KtElement?, internalClassName: String): SourceInfo {
            assert(element != null) { "Couldn't create source mapper for null element $internalClassName" }
            konst lineNumbers = CodegenUtil.getLineNumberForElement(element!!.containingFile, true)
                ?: error("Couldn't extract line count in ${element.containingFile}")

            //TODO hack condition for package parts cleaning
            konst isTopLevel = element is KtFile || (element is KtNamedFunction && element.getParent() is KtFile)
            konst cleanedClassFqName = if (!isTopLevel) internalClassName else internalClassName.substringBefore('$')

            konst fileName = element.containingKtFile.name
            return SourceInfo(fileName, cleanedClassFqName, lineNumbers)
        }

    }
}

