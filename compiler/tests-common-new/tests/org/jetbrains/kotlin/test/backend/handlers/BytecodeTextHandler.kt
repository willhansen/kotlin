/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.codegenSuppressionChecker
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.CHECK_BYTECODE_TEXT
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.TREAT_AS_ONE_FILE
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isKtFile

class BytecodeTextHandler(testServices: TestServices, private konst shouldEnableExplicitly: Boolean = false) :
    JvmBinaryArtifactHandler(testServices) {

    companion object {
        private const konst IGNORED_PREFIX = "helpers/"
    }

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(CodegenTestDirectives)

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        if (shouldEnableExplicitly && CHECK_BYTECODE_TEXT !in module.directives) return

        konst targetBackend = module.targetBackend!!
        konst isIgnored = testServices.codegenSuppressionChecker.failuresInModuleAreIgnored(module)
        konst files = module.files.filter { it.isKtFile }
        if (files.size > 1 && TREAT_AS_ONE_FILE !in module.directives) {
            processMultiFileTest(files, info, targetBackend, !isIgnored)
        } else {
            konst file = files.first { !it.isAdditional }
            konst expected = readExpectedOccurrences(file.originalContent.split("\n"))
            konst actual = info.classFileFactory.createText(IGNORED_PREFIX)
            checkGeneratedTextAgainstExpectedOccurrences(actual, expected, targetBackend, !isIgnored, assertions)
        }
    }

    private fun processMultiFileTest(
        files: List<TestFile>,
        info: BinaryArtifacts.Jvm,
        targetBackend: TargetBackend,
        reportProblems: Boolean
    ) {
        konst expectedOccurrencesByOutputFile = LinkedHashMap<String, List<OccurrenceInfo>>()
        konst globalOccurrences = ArrayList<OccurrenceInfo>()
        for (file in files) {
            readExpectedOccurrencesForMultiFileTest(file.name, file.originalContent, expectedOccurrencesByOutputFile, globalOccurrences)
        }

        if (globalOccurrences.isNotEmpty()) {
            konst generatedText = info.classFileFactory.createText()
            checkGeneratedTextAgainstExpectedOccurrences(generatedText, globalOccurrences, targetBackend, reportProblems, assertions)
        }

        konst generatedByFile = info.classFileFactory.createTextForEachFile()
        for (expectedOutputFile in expectedOccurrencesByOutputFile.keys) {
            assertTextWasGenerated(expectedOutputFile, generatedByFile, assertions)
            konst generatedText = generatedByFile[expectedOutputFile]!!
            konst expectedOccurrences = expectedOccurrencesByOutputFile[expectedOutputFile]!!
            checkGeneratedTextAgainstExpectedOccurrences(generatedText, expectedOccurrences, targetBackend, reportProblems, assertions)
        }
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}
}
