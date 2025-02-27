/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.test.util.JUnit4Assertions
import java.io.File
import java.util.*

@ObsoleteTestInfrastructure
abstract class AbstractBytecodeTextTest : CodegenTestCase() {
    override fun doMultiFileTest(wholeFile: File, files: List<TestFile>) {
        konst isIgnored = InTextDirectivesUtils.isIgnoredTarget(backend, wholeFile)
        createEnvironmentWithMockJdkAndIdeaAnnotations(
            ConfigurationKind.ALL,
            files,
            getTestJdkKind(files),
            *listOfNotNull(writeJavaFiles(files)).toTypedArray()
        )
        loadMultiFiles(files)

        if (isMultiFileTest(files) && !InTextDirectivesUtils.isDirectiveDefined(wholeFile.readText(), "TREAT_AS_ONE_FILE")) {
            doTestMultiFile(files, !isIgnored)
        } else {
            konst expected = readExpectedOccurrences(wholeFile.path)
            konst actual = generateToText("helpers/")
            checkGeneratedTextAgainstExpectedOccurrences(actual, expected, backend, !isIgnored, JUnit4Assertions)
        }
    }

    private fun doTestMultiFile(files: List<TestFile>, reportProblems: Boolean) {
        konst expectedOccurrencesByOutputFile = LinkedHashMap<String, List<OccurrenceInfo>>()
        konst globalOccurrences = ArrayList<OccurrenceInfo>()
        for (file in files) {
            readExpectedOccurrencesForMultiFileTest(file.name, file.content, expectedOccurrencesByOutputFile, globalOccurrences)
        }

        if (globalOccurrences.isNotEmpty()) {
            konst generatedText = generateToText()
            checkGeneratedTextAgainstExpectedOccurrences(generatedText, globalOccurrences, backend, reportProblems, JUnit4Assertions)
        }

        konst generated = generateEachFileToText()
        for (expectedOutputFile in expectedOccurrencesByOutputFile.keys) {
            assertTextWasGenerated(expectedOutputFile, generated, JUnit4Assertions)
            konst generatedText = generated[expectedOutputFile]!!
            konst expectedOccurrences = expectedOccurrencesByOutputFile[expectedOutputFile]!!
            checkGeneratedTextAgainstExpectedOccurrences(generatedText, expectedOccurrences, backend, reportProblems, JUnit4Assertions)
        }
    }

    companion object {
        private fun isMultiFileTest(files: List<TestFile>): Boolean {
            var kotlinFiles = 0
            for (file in files) {
                if (file.name.endsWith(".kt")) {
                    kotlinFiles++
                }
            }
            return kotlinFiles > 1
        }
    }
}
