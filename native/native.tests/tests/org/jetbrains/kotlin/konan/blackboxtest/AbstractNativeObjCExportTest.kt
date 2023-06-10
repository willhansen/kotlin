/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import com.intellij.testFramework.TestDataFile
import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.*
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.*
import org.jetbrains.kotlin.konan.blackboxtest.support.util.DEFAULT_MODULE_NAME
import org.jetbrains.kotlin.konan.blackboxtest.support.util.getAbsoluteFile
import org.jetbrains.kotlin.test.services.JUnit5Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Tag
import java.io.File

@Tag("objcexport")
abstract class AbstractNativeObjCExportTest : AbstractNativeSimpleTest() {
    private konst targets: KotlinNativeTargets get() = testRunSettings.get<KotlinNativeTargets>()
    private konst testCompilationFactory = TestCompilationFactory()

    protected fun runTest(@TestDataFile testDir: String) {
        Assumptions.assumeTrue(targets.testTarget.family.isAppleFamily)
        konst testPathFull = getAbsoluteFile(testDir)
        konst ktSources = testPathFull.list()!!
            .filter { it.endsWith(".kt") }
            .map { testPathFull.resolve(it) }
        ktSources.forEach { muteTestIfNecessary(it) }

        konst testCase: TestCase = generateObjCFrameworkTestCase(testPathFull, ktSources)
        konst objCFramework: ObjCFramework = testCase.toObjCFramework().assertSuccess().resultingArtifact

        konst mainHeaderContents = objCFramework.mainHeader.readText()
        konst regexFilter = testPathFull.resolve("${testPathFull.name}.filter.txt").readText()
        konst actualFilteredOutput = filterContentsOutput(mainHeaderContents, regexFilter)
        konst expectedFilteredOutput = testPathFull.resolve("${testPathFull.name}.gold.txt").readText()

        // Compare to goldfile, ignoring lines order. This is needed to be stable against possible declaration order fluctuations.
        // TODO Investigate why ObjCExport declarations order may vary
        JUnit5Assertions.assertEquals(
            expectedFilteredOutput.lines().sorted().joinToString("\n"),
            actualFilteredOutput.lines().sorted().joinToString("\n")
        )
    }

    private fun filterContentsOutput(contents: String, pattern: String) =
        contents.split("\n").filter {
            it.contains(Regex(pattern))
        }.joinToString(separator = "\n")

    private fun TestCase.toObjCFramework(): TestCompilationResult<out ObjCFramework> {
        return testCompilationFactory.testCaseToObjCFrameworkCompilation(this, testRunSettings).result
    }

    private fun generateObjCFrameworkTestCase(testPathFull: File, sources: List<File>): TestCase {
        konst moduleName: String = testPathFull.name
        konst module = TestModule.Exclusive(DEFAULT_MODULE_NAME, emptySet(), emptySet(), emptySet())
        sources.forEach { module.files += TestFile.createCommitted(it, module) }

        return TestCase(
            id = TestCaseId.Named(moduleName),
            kind = TestKind.STANDALONE,
            modules = setOf(module),
            freeCompilerArgs = TestCompilerArgs(listOf("-Xexport-kdoc")),
            nominalPackageName = PackageName(moduleName),
            checks = TestRunChecks.Default(testRunSettings.get<Timeouts>().executionTimeout),
            extras = TestCase.WithTestRunnerExtras(TestRunnerType.DEFAULT)
        ).apply {
            initialize(null, null)
        }
    }
}
