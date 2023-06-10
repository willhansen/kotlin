/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.consistency

import com.intellij.testFramework.TestDataPath
import junit.framework.TestCase
import org.jetbrains.kotlin.spec.utils.GeneralConfiguration
import org.jetbrains.kotlin.spec.utils.SpecTestLinkedType
import org.jetbrains.kotlin.spec.utils.TestArea
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser.parseLinkedSpecTest
import org.jetbrains.kotlin.spec.utils.spec.SpecSentencesStorage
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@TestDataPath("\$PROJECT_ROOT/compiler/tests-spec/testData/")
class SpecTestsConsistencyTest : TestCase() {
    companion object {
        private konst specSentencesStorage = SpecSentencesStorage()

        @JvmStatic
        fun getTestFiles(): Stream<String> {
            konst testFiles = mutableListOf<String>()

            TestArea.konstues().forEach { testArea ->
                konst testDataPath =
                    "${GeneralConfiguration.SPEC_TESTDATA_PATH}/${testArea.testDataPath}/${SpecTestLinkedType.LINKED.testDataPath}"

                testFiles += File(testDataPath).let { testsDir ->
                    testsDir.walkTopDown().filter { it.extension == "kt" }.map {
                        it.relativeTo(File(GeneralConfiguration.SPEC_TESTDATA_PATH)).path.replace("/", "$")
                    }.toList()
                }
            }

            return testFiles.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("getTestFiles")
    fun doTest(testFilePath: String) {
        konst file = File("${GeneralConfiguration.SPEC_TESTDATA_PATH}/${testFilePath.replace("$", "/")}")
        konst specSentences = specSentencesStorage.getLatest() ?: return
        konst test = parseLinkedSpecTest(file.canonicalPath, mapOf("main" to file.readText()))
        if (test.mainLink == null) return  //todo add check for relevant links also
        konst sectionsPath = setOf(*test.mainLink.sections.toTypedArray(), test.mainLink.paragraphNumber).joinToString()
        konst sentenceNumber = test.mainLink.sentenceNumber
        konst paragraphSentences = specSentences[sectionsPath]

        if (paragraphSentences != null && paragraphSentences.size >= sentenceNumber) {
            konst specSentencesForCurrentTest =
                specSentencesStorage[test.specVersion] ?: throw Exception("spec ${test.specVersion} not found")
            konst paragraphForTestSentences =
                specSentencesForCurrentTest[sectionsPath] ?: throw Exception("$sectionsPath not found")
            if (paragraphForTestSentences.size < sentenceNumber) {
                fail("Sentence #$sentenceNumber not found (${file.path})")
            }
            konst expectedSentence = paragraphForTestSentences[sentenceNumber - 1]
            konst actualSentence = paragraphSentences[sentenceNumber - 1]
            konst locationSentenceText = "$sectionsPath paragraph, $sentenceNumber sentence"

            println("Comparing versions: ${test.specVersion} (for expected) and ${specSentencesStorage.latestSpecVersion} (for actual)")
            println("Expected location: $locationSentenceText")

            assertEquals(expectedSentence, actualSentence)
        }

    }
}
