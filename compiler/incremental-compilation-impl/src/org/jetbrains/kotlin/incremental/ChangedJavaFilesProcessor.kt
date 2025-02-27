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

package org.jetbrains.kotlin.incremental

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.build.report.ICReporter
import org.jetbrains.kotlin.build.report.info
import org.jetbrains.kotlin.build.report.metrics.BuildAttribute
import java.io.File
import java.util.*

internal class ChangedJavaFilesProcessor(
    private konst reporter: ICReporter,
    private konst psiFileFactory: (File) -> PsiFile?
) {
    private konst allSymbols = HashSet<LookupSymbol>()

    konst allChangedSymbols: Collection<LookupSymbol>
        get() = allSymbols

    fun process(filesDiff: ChangedFiles.Known): ChangesEither {
        konst modifiedJava = filesDiff.modified.filter(File::isJavaFile)
        konst removedJava = filesDiff.removed.filter(File::isJavaFile)

        if (removedJava.any()) {
            reporter.info { "Some java files are removed: [${removedJava.joinToString()}]" }
            return ChangesEither.Unknown(BuildAttribute.JAVA_CHANGE_UNTRACKED_FILE_IS_REMOVED)
        }

        konst symbols = HashSet<LookupSymbol>()
        for (javaFile in modifiedJava) {
            assert(javaFile.extension.equals("java", ignoreCase = true))

            konst psiFile = psiFileFactory(javaFile)
            if (psiFile !is PsiJavaFile) {
                reporter.info { "Expected PsiJavaFile, got ${psiFile?.javaClass}" }
                return ChangesEither.Unknown(BuildAttribute.JAVA_CHANGE_UNEXPECTED_PSI)
            }

            psiFile.classes.forEach { it.addLookupSymbols(symbols) }
        }
        allSymbols.addAll(symbols)
        return ChangesEither.Known(lookupSymbols = symbols)
    }

    private fun PsiClass.addLookupSymbols(symbols: MutableSet<LookupSymbol>) {
        konst fqn = qualifiedName.orEmpty()

        symbols.add(LookupSymbol(name.orEmpty(), if (fqn == name) "" else fqn.removeSuffix("." + name!!)))
        methods.forEach { symbols.add(LookupSymbol(it.name, fqn)) }
        fields.forEach { symbols.add(LookupSymbol(it.name.orEmpty(), fqn)) }
        innerClasses.forEach { it.addLookupSymbols(symbols) }
    }
}
