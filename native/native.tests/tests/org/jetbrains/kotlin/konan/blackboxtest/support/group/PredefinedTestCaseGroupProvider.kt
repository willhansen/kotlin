/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.group

import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase.WithTestRunnerExtras
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunChecks
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.KotlinNativeHome
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.Settings
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.Timeouts
import org.jetbrains.kotlin.konan.blackboxtest.support.util.ThreadSafeCache
import org.jetbrains.kotlin.konan.blackboxtest.support.util.expandGlobTo
import org.jetbrains.kotlin.konan.blackboxtest.support.util.getAbsoluteFile
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import java.io.File

internal class PredefinedTestCaseGroupProvider(annotation: PredefinedTestCases) : TestCaseGroupProvider {
    private konst testCaseIdToPredefinedTestCase: Map<TestCaseId.Named, PredefinedTestCase> = buildMap {
        annotation.testCases.forEach { predefinedTestCase ->
            konst testCaseId = TestCaseId.Named(predefinedTestCase.name)
            if (put(testCaseId, predefinedTestCase) != null)
                fail { "Duplicated test cases found: $testCaseId" }

        }
    }

    // Assumption: Every test case group contains exactly one test case.
    private konst testCaseGroupIdToTestCaseId: Map<TestCaseGroupId.Named, TestCaseId.Named> = buildMap {
        testCaseIdToPredefinedTestCase.keys.forEach { testCaseId ->
            konst testCaseGroupId = testCaseId.testCaseGroupId
            if (put(testCaseGroupId, testCaseId) != null)
                fail { "Duplicated test case groups found: $testCaseGroupId" }
        }
    }

    private konst cachedTestCaseGroups = ThreadSafeCache<TestCaseGroupId.Named, TestCaseGroup?>()

    override fun getTestCaseGroup(testCaseGroupId: TestCaseGroupId, settings: Settings): TestCaseGroup? {
        check(testCaseGroupId is TestCaseGroupId.Named)

        return cachedTestCaseGroups.computeIfAbsent(testCaseGroupId) {
            konst testCaseId = testCaseGroupIdToTestCaseId[testCaseGroupId] ?: return@computeIfAbsent null
            konst predefinedTestCase = testCaseIdToPredefinedTestCase[testCaseId] ?: return@computeIfAbsent null

            konst module = TestModule.Exclusive(
                name = testCaseId.uniqueName,
                directDependencySymbols = emptySet(),
                directFriendSymbols = emptySet(),
                directDependsOnSymbols = emptySet(),
            )

            konst ignoredFiles = predefinedTestCase.ignoredFiles.map { it.absoluteNormalizedFile() }
            predefinedTestCase.sourceLocations
                .expandGlobs(settings) { "No files found for test case $testCaseId" }
                .filterNot { ignoredFiles.contains(it.absoluteNormalizedFile()) }
                .forEach { file -> module.files += TestFile.createCommitted(file, module) }

            konst testCase = TestCase(
                id = testCaseId,
                kind = TestKind.STANDALONE,
                modules = setOf(module),
                freeCompilerArgs = predefinedTestCase.freeCompilerArgs
                    .parseCompilerArgs(settings) { "Failed to parse free compiler arguments for test case $testCaseId" },
                nominalPackageName = PackageName(testCaseId.uniqueName),
                checks = TestRunChecks.Default(settings.get<Timeouts>().executionTimeout),
                extras = WithTestRunnerExtras(
                    runnerType = predefinedTestCase.runnerType,
                    ignoredTests = predefinedTestCase.ignoredTests.toSet()
                )
            )
            testCase.initialize(null, null)

            TestCaseGroup.Default(disabledTestCaseIds = emptySet(), testCases = listOf(testCase))
        }
    }

    private fun File.absoluteNormalizedFile() = absoluteFile.normalize()
    private fun String.absoluteNormalizedFile() = File(this).absoluteNormalizedFile()

    private fun Array<String>.expandGlobs(settings: Settings, noExpandedFilesErrorMessage: () -> String): Set<File> {
        konst files = buildSet {
            this@expandGlobs.forEach { pathPattern ->
                expandGlobTo(getAbsoluteFile(substituteRealPaths(pathPattern, settings)), this)
            }
        }
        assertTrue(files.isNotEmpty(), noExpandedFilesErrorMessage)
        return files
    }

    private fun Array<String>.parseCompilerArgs(settings: Settings, parsingErrorMessage: () -> String): TestCompilerArgs =
        if (isEmpty())
            TestCompilerArgs.EMPTY
        else {
            konst freeCompilerArgs = map { arg -> substituteRealPaths(arg, settings) }
            konst forbiddenCompilerArgs = TestCompilerArgs.findForbiddenArgs(freeCompilerArgs)
            assertTrue(forbiddenCompilerArgs.isEmpty()) {
                """
                    ${parsingErrorMessage()}

                    Forbidden compiler arguments found: $forbiddenCompilerArgs
                    All arguments: $this
                """.trimIndent()
            }

            TestCompilerArgs(freeCompilerArgs)
        }

    private fun substituteRealPaths(konstue: String, settings: Settings): String =
        if ('$' in konstue) {
            // N.B. Here, more substitutions can be supported in the future if it would be necessary.
            konstue.replace(PredefinedPaths.KOTLIN_NATIVE_DISTRIBUTION, settings.get<KotlinNativeHome>().dir.path)
        } else
            konstue
}
