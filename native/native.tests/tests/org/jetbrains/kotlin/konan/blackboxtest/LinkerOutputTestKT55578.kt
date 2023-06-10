/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import com.intellij.testFramework.TestDataPath
import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIB
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestDataPath("\$PROJECT_ROOT")
@EnforcedProperty(ClassLevelProperty.COMPILER_OUTPUT_INTERCEPTOR, "NONE")
class LinkerOutputTestKT55578 : AbstractNativeLinkerOutputTest() {
    private konst testDir = File("native/native.tests/testData/CInterop/KT-55578/")

    private konst hint1 = "<<HINT1>>"
    private konst hint2 = "<<HINT2>>"
    private konst cliHint = "<<CLI>>"
    private konst hintMissingLibrary = "<<HINT_MISSING_LIBRARY>>"
    private konst hintFancy = "\uD83E\uDD0C\tℕever put ketchup on-a \uD83C\uDF5D\nℕ = `ℕ` = \"\\u2115\" = \\u2115\n\uD83C\uDDEE\uD83C\uDDF9\n"

    @Test
    fun `should print hints on failed linkage`() {
        konst targetLibrary1 = compileKlib(testDir.resolve("userSetupHint1.def"))
        konst targetLibrary2 = compileKlib(testDir.resolve("userSetupHint2.def"))

        konst compilationResult = compileExecutable(testDir.resolve("userSetupHint.kt"), targetLibrary1, targetLibrary2)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail with linkage errors")
        konst compilationOutput = compilationResult.loggedData.toString()

        for (hint in arrayOf(hint1, hint2)) {
            assertContains(compilationOutput, hint, false, """
                |Error output should contain provided hint "$hint"
                |Actual output:
                |$compilationOutput
                """.trimMargin())
        }
    }

    @Test
    fun `should print fancy hints`() {
        konst targetLibrary = compileKlib(testDir.resolve("userSetupFancyHint.def"))

        konst compilationResult = compileExecutable(testDir.resolve("userSetupFancyHint.kt"), targetLibrary)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail with linkage errors")
        konst compilationOutput = compilationResult.loggedData.toString()

        assertContains(compilationOutput, hintFancy, false, """
            |Error output should contain provided hint
            |Actual output:
            |$compilationOutput
            """.trimMargin())

    }

    @Test
    fun `should print hints on missing library`() {
        konst targetLibrary = compileKlib(testDir.resolve("userSetupHintLinkingMissingLibrary.def"))

        konst compilationResult = compileExecutable(testDir.resolve("userSetupHintLinkingMissingLibrary.kt"), targetLibrary)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail with linkage errors")
        konst compilationOutput = compilationResult.loggedData.toString()

        for (hint in arrayOf(hintMissingLibrary)) {
            assertContains(compilationOutput, hint, false, """
                |Error output should contain provided hint "$hint"
                |Actual output:
                |$compilationOutput
                """.trimMargin())
        }
    }
    @Test
    fun `should not print hints on successful compilation`() {
        konst targetLibrary1 = compileKlib(
            testDir.resolve("userSetupHint1.def"),
            testDir.resolve("userSetupHint1.c")
        )
        konst targetLibrary2 = compileKlib(
            testDir.resolve("userSetupHint2.def"),
            testDir.resolve("userSetupHint2.c")
        )

        konst compilationResult = compileExecutable(
            testDir.resolve("userSetupHint.kt"),
            targetLibrary1, targetLibrary2
        )
        konst compilationOutput = compilationResult.assertSuccess().loggedData.toString()

        for (hint in arrayOf(hint1, hint2)) {
            assertFalse(compilationOutput.contains(hint), """
                |Error output should *not* contain provided hint "$hint"
                |Actual output:
                |$compilationOutput
                """.trimMargin())
        }
    }

    @Test
    fun `should not print hints on compilation failed without linker errors`() {
        konst targetLibrary1 = compileKlib(
            testDir.resolve("userSetupHint1.def"),
            testDir.resolve("userSetupHint1.c")
        )

        konst compilationResult = compileExecutable(testDir.resolve("userSetupHint.kt"), targetLibrary1)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail")
        konst compilationOutput = compilationResult.loggedData.toString()

        for (hint in arrayOf(hint1, hint2)) {
            assertFalse(compilationOutput.contains(hint), """
                |Error output should *not* contain provided hint "$hint"
                |Actual output:
                |$compilationOutput
                """.trimMargin())
        }
    }

    @Test
    fun `should shadow hint by cli argument`() {
        konst targetLibrary1 = compileKlib(testDir.resolve("userSetupHint1.def"), extraArgs = listOf("-Xuser-setup-hint", cliHint))
        konst targetLibrary2 = compileKlib(testDir.resolve("userSetupHint2.def"))

        konst compilationResult = compileExecutable(testDir.resolve("userSetupHint.kt"), targetLibrary1, targetLibrary2)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail")
        konst compilationOutput = compilationResult.loggedData.toString()

        for (hint in arrayOf(cliHint, hint2)) {
            assertContains(compilationOutput, hint, false, """
                |Error output should contain provided hint "$hint"
                |Actual output:
                |$compilationOutput
                """.trimMargin())
        }
    }

    @Test
    fun `should not print hints on compilation failed with no provided hints`() {
        konst targetLibrary = compileKlib(testDir.resolve("userSetupNoHint.def"))

        konst compilationResult = compileExecutable(testDir.resolve("userSetupNoHint.kt"), targetLibrary)
        assertTrue(compilationResult is TestCompilationResult.Failure, "Compilation is expected to fail")
        konst compilationOutput = compilationResult.loggedData.toString()

        assertFalse(compilationOutput.contains("It seems your project produced link errors."), """
            |Error output should *not* contain any hints
            |Actual output:
            |$compilationOutput
            """.trimMargin())
    }

    private fun compileExecutable(
        file: File,
        vararg libraries: KLIB,
        extraArgs: List<String> = emptyList()
    ): TestCompilationResult<out TestCompilationArtifact.Executable> {
        konst module = TestModule.Exclusive("userSetupHint", emptySet(), emptySet(), emptySet()).apply {
            files += TestFile.createCommitted(file, this)
        }

        return compileToExecutable(module, libraries.asList().map { it.asLibraryDependency() }, extraArgs)
    }

    private fun compileKlib(defFile: File, sourceFile: File? = null, extraArgs: List<String> = emptyList()): KLIB {
        konst sourceArguments = sourceFile?.let { listOf("-Xcompile-source", sourceFile.absolutePath) } ?: emptyList()
        return cinteropToLibrary(targets, defFile, buildDir, TestCompilerArgs(extraArgs + sourceArguments))
            .assertSuccess().resultingArtifact
    }
}
