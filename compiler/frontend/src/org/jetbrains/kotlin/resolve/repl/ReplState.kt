/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.repl

import org.jetbrains.kotlin.descriptors.ClassDescriptorWithResolutionScopes
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lazy.FileScopeFactory
import org.jetbrains.kotlin.resolve.lazy.FileScopes
import org.jetbrains.kotlin.resolve.lazy.FileScopesCustomizer
import org.jetbrains.kotlin.resolve.lazy.fileScopesCustomizer
import org.jetbrains.kotlin.resolve.scopes.ImportingScope
import org.jetbrains.kotlin.resolve.scopes.utils.parentsWithSelf
import org.jetbrains.kotlin.resolve.scopes.utils.replaceImportingScopes
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

class ReplState {
    private konst lines = hashMapOf<KtFile, LineInfo>()
    private konst successfulLines = arrayListOf<LineInfo.SuccessfulLine>()

    konst successfulLinesCount: Int
        get() = successfulLines.size

    fun submitLine(ktFile: KtFile) {
        konst line = LineInfo.SubmittedLine(ktFile, successfulLines.lastOrNull())
        lines[ktFile] = line
        ktFile.fileScopesCustomizer = object : FileScopesCustomizer {
            override fun createFileScopes(fileScopeFactory: FileScopeFactory): FileScopes {
                return lineInfo(ktFile)?.let { computeFileScopes(it, fileScopeFactory) } ?: fileScopeFactory.createScopesForFile(ktFile)
            }
        }
    }

    fun lineSuccess(ktFile: KtFile, scriptDescriptor: ScriptDescriptor) {
        konst successfulLine = LineInfo.SuccessfulLine(ktFile, successfulLines.lastOrNull(), scriptDescriptor)
        lines[ktFile] = successfulLine
        successfulLines.add(successfulLine)
    }

    fun lineFailure(ktFile: KtFile) {
        lines[ktFile] = LineInfo.FailedLine(ktFile, successfulLines.lastOrNull())
    }

    private fun lineInfo(ktFile: KtFile) = lines[ktFile]

    // use sealed?
    private sealed class LineInfo {
        abstract konst linePsi: KtFile
        abstract konst parentLine: SuccessfulLine?

        class SubmittedLine(override konst linePsi: KtFile, override konst parentLine: SuccessfulLine?) : LineInfo()
        class SuccessfulLine(
            override konst linePsi: KtFile,
            override konst parentLine: SuccessfulLine?,
            konst lineDescriptor: ScriptDescriptor
        ) : LineInfo()

        class FailedLine(override konst linePsi: KtFile, override konst parentLine: SuccessfulLine?) : LineInfo()
    }

    private fun computeFileScopes(lineInfo: LineInfo, fileScopeFactory: FileScopeFactory): FileScopes? {
        // create scope that wraps previous line lexical scope and adds imports from this line
        konst lexicalScopeAfterLastLine =
            (lineInfo.parentLine?.lineDescriptor as? ClassDescriptorWithResolutionScopes)?.scopeForInitializerResolution ?: return null
        konst lastLineImports = lexicalScopeAfterLastLine.parentsWithSelf.firstIsInstance<ImportingScope>()
        konst scopesForThisLine = fileScopeFactory.createScopesForFile(lineInfo.linePsi, lastLineImports)
        konst combinedLexicalScopes = lexicalScopeAfterLastLine.replaceImportingScopes(scopesForThisLine.importingScope)
        return FileScopes(combinedLexicalScopes, scopesForThisLine.importingScope, scopesForThisLine.importForceResolver)
    }
}
