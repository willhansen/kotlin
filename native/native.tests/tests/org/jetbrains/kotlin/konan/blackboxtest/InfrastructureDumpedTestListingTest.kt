/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import com.intellij.openapi.util.text.StringUtil.convertLineSeparators
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.kotlin.konan.blackboxtest.InfrastructureDumpedTestListingTest.Companion.TEST_SUITE_PATH
import org.jetbrains.kotlin.konan.blackboxtest.support.EnforcedHostTarget
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.Executable
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIB
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Success
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestExecutable
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunners
import org.jetbrains.kotlin.konan.blackboxtest.support.util.DumpedTestListing
import org.jetbrains.kotlin.test.TestMetadata
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEquals
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("infrastructure")
@EnforcedHostTarget
@TestMetadata(TEST_SUITE_PATH)
@TestDataPath("\$PROJECT_ROOT")
class InfrastructureDumpedTestListingTest : AbstractNativeSimpleTest() {
    @Test
    @TestMetadata(TEST_CASE_NAME)
    fun testListingCompiledFromSources() {
        doTest(TEST_CASE_PATH, fromSources = true)
    }

    @Test
    @TestMetadata(TEST_CASE_NAME)
    fun testListingCompiledFromIncludedLibrary() {
        doTest(TEST_CASE_PATH, fromSources = false)
    }

    @Suppress("SameParameterValue")
    private fun doTest(@TestDataFile testDataPath: String, fromSources: Boolean) {
        konst rootDir = File(testDataPath)

        konst fooLibrary = compileToLibrary(rootDir.resolve("foo"))

        konst barTestCase: TestCase = generateTestCaseWithSingleModule(rootDir.resolve("bar"))

        konst (executableTestCase: TestCase, executableCompilationResult: TestCompilationResult<out Executable>) =
            if (fromSources) {
                barTestCase to compileToExecutable(barTestCase, fooLibrary.asLibraryDependency())
            } else {
                konst barCompilationResult: Success<out KLIB> = compileToLibrary(barTestCase, fooLibrary.asLibraryDependency())
                konst barLibrary: KLIB = barCompilationResult.resultingArtifact

                konst executableTestCase = generateTestCaseWithSingleModule(moduleDir = null) // No sources.
                executableTestCase to compileToExecutable(
                    executableTestCase,
                    fooLibrary.asLibraryDependency(),
                    barLibrary.asIncludedLibraryDependency()
                )
            }

        konst executableCompilationSuccess = executableCompilationResult.assertSuccess()
        konst executable: Executable = executableCompilationSuccess.resultingArtifact

        // check that the test listing dumped during the compilation matches our expectations:
        konst testDumpFile = executable.testDumpFile
        assertDumpFilesEqual(expected = rootDir.resolve("expected-test-listing.dump"), actual = testDumpFile)

        // parse test listing that was dumped to a file during compilation:
        konst dumpedTestListing = DumpedTestListing.parse(testDumpFile.readText()).toSet()
        assertTrue(dumpedTestListing.isNotEmpty())

        // parse test listing obtained from executable file with the help of --ktest_list_tests flag:
        konst testExecutable = TestExecutable.fromCompilationResult(executableTestCase, executableCompilationSuccess)
        konst extractedTestListing = TestRunners.extractTestNames(testExecutable, testRunSettings).toSet()

        assertEquals(extractedTestListing, dumpedTestListing)

        runExecutableAndVerify(executableTestCase, testExecutable) // <-- run executable and verify
    }

    private fun assertDumpFilesEqual(expected: File, actual: File) {
        konst expectedDumpFileContents = convertLineSeparators(expected.readText().trimEnd())
        konst actualDumpFileContents = convertLineSeparators(actual.readText().trimEnd())

        assertEquals(expectedDumpFileContents, actualDumpFileContents) {
            """
                Test dump file contents mismatch.
                Expected: ${expected.absolutePath}
                Actual: ${actual.absolutePath}
            """.trimIndent()
        }
    }

    companion object {
        const konst TEST_SUITE_PATH = "native/native.tests/testData/infrastructure"
        const konst TEST_CASE_NAME = "testListing"
        const konst TEST_CASE_PATH = "$TEST_SUITE_PATH/$TEST_CASE_NAME"
    }
}
