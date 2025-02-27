/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.testFramework.TestDataFile
import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.*
import org.jetbrains.kotlin.konan.blackboxtest.support.util.*
import org.junit.jupiter.api.Tag
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.*
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEquals
import org.junit.jupiter.api.Assumptions
import kotlin.test.assertIs

@Tag("cinterop")
abstract class AbstractNativeCInteropKT39120Test : AbstractNativeCInteropBaseTest() {

    @Synchronized
    protected fun runTest(@TestDataFile testPath: String) {
        // KT-39120 is about Objective-C, so this test is for Apple hosts/targets only
        Assumptions.assumeTrue(targets.hostTarget.family.isAppleFamily && targets.testTarget.family.isAppleFamily)

        konst testPathFull = getAbsoluteFile(testPath)
        konst testDataDir = testPathFull.parentFile.parentFile
        konst def1File = testPathFull.resolve("pod1.def")
        konst def2File = testPathFull.resolve("pod2.def")
        konst golden1File = testPathFull.resolve("pod1.contents.gold.txt")
        konst golden2File = testPathFull.resolve("pod2.contents.gold.txt")

        konst includeFrameworkArgs = TestCompilerArgs("-compiler-option", "-F${testDataDir.canonicalPath}")
        konst klib1: KLIB = cinteropToLibrary(targets, def1File, buildDir, includeFrameworkArgs).assertSuccess().resultingArtifact
        konst contents1 = klib1.getContents(kotlinNativeClassLoader.classLoader)

        konst expectedFiltered1Output = golden1File.readText()
        konst actualFiltered1Output = filterContentsOutput(contents1, " pod.Version|POD|class Pod")
        assertEquals(StringUtilRt.convertLineSeparators(expectedFiltered1Output), StringUtilRt.convertLineSeparators(actualFiltered1Output))

        konst cinterop2ExtraArgs = TestCompilerArgs("-l", klib1.klibFile.canonicalPath, "-compiler-option", "-fmodules")
        konst klib2: KLIB = cinteropToLibrary(targets, def2File, buildDir, includeFrameworkArgs + cinterop2ExtraArgs).assertSuccess().resultingArtifact
        konst contents2 = klib2.getContents(kotlinNativeClassLoader.classLoader)

        konst expectedFiltered2Output = golden2File.readText()
        konst actualFiltered2Output = filterContentsOutput(contents2, " pod.Version|POD|class Pod")
        assertEquals(StringUtilRt.convertLineSeparators(expectedFiltered2Output), StringUtilRt.convertLineSeparators(actualFiltered2Output))

        konst ktFile = testPathFull.resolve(DEFAULT_FILE_NAME)
        if (ktFile.exists()) {
            // Just compile "main.kt" with klib1 and klib2, without running resulting executable
            konst module = TestModule.Exclusive(DEFAULT_MODULE_NAME, emptySet(), emptySet(), emptySet()).apply {
                files += TestFile.createCommitted(ktFile, this)
            }
            konst compilationResult = compileToExecutable(
                createTestCaseNoTestRun(module, TestCompilerArgs(listOf())),
                klib1.asLibraryDependency(),
                klib2.asLibraryDependency()
            )

            konst expectedFailureTxtFile = testPathFull.resolve("expected.compilation.failure.txt")
            if (expectedFailureTxtFile.exists()) {
                assertIs<TestCompilationResult.CompilationToolFailure>(compilationResult)
                konst expectedFailureSubstring = expectedFailureTxtFile.readText()
                konst actualFailure = compilationResult.loggedData.toString()
                assert(actualFailure.contains(expectedFailureSubstring)) {
                    "Expected failure substring:\n$expectedFailureSubstring\nActual failure logged data:\n$actualFailure"
                }
            } else {
                compilationResult.assertSuccess()
            }
        }
    }

    private fun filterContentsOutput(contents: String, pattern: String) =
        contents.split("\n").filter {
            it.contains(Regex(pattern))
        }.joinToString(separator = "\n")

    private fun createTestCaseNoTestRun(module: TestModule.Exclusive, compilerArgs: TestCompilerArgs) = TestCase(
        id = TestCaseId.Named(module.name),
        kind = TestKind.STANDALONE_NO_TR,
        modules = setOf(module),
        freeCompilerArgs = compilerArgs,
        nominalPackageName = PackageName.EMPTY,
        checks = TestRunChecks.Default(testRunSettings.get<Timeouts>().executionTimeout),
        extras = TestCase.NoTestRunnerExtras(".${module.name}")
    ).apply {
        initialize(null, null)
    }
}
