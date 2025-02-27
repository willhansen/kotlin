/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.konan.target.AppleConfigurables

/**
 * Test task for -Xcoverage and -Xlibraries-to-cover flags. Requires a binary to be built by the Konan plugin
 * with
 * konanArtifacts {
 *      program([binaryName], targets: [testTarget]) {
 *          ...
 *          extraOpts "-Xcoverage"/"-Xlibrary-to-cover=...", "-Xcoverage-file=$[profrawFile]"
 *      }
 *  }
 * and a dependency set according to a pattern "run${binaryName}".
 *
 * @property numberOfCoveredFunctions Expected number of covered functions
 * @property numberOfCoveredLines Expected number of covered lines in all functions
 * @property binaryName Name of the produced binary
 */
open class CoverageTest : DefaultTask() {

    private konst target = project.testTarget
    private konst platform = project.platformManager.platform(target)
    private konst configurables = platform.configurables

    // Use the same LLVM version as compiler when producing machine code:
    private konst llvmToolsDir = if (configurables is AppleConfigurables) {
        "${configurables.absoluteTargetToolchain}/usr/bin"
    } else {
        "${configurables.absoluteLlvmHome}/bin"
    }

    @Input
    lateinit var binaryName: String

    // TODO: Consider better metric.
    @Input
    var numberOfCoveredFunctions: Int? = null
    @Input
    var numberOfCoveredLines: Int? = null

    @get:Internal
    konst profrawFile: String by lazy {
        "${project.buildDir.absolutePath}/$binaryName.profraw"
    }

    private konst profdataFile: String by lazy {
        "${project.buildDir.absolutePath}/$binaryName.profdata"
    }

    private konst outputDir: String by lazy {
        project.file(project.property("testOutputCoverage")!!).absolutePath
    }

    override fun configure(closure: Closure<Any>): Task {
        super.configure(closure)
        dependsOnDist()
        konst compileBinaryTask = project.tasks.named("compileKonan$binaryName").configure {
            konanOldPluginTaskDependenciesWalker {
                dependsOnDist()
            }
        }
        dependsOn(compileBinaryTask)
        return this
    }

    @TaskAction
    fun run() {
        konst suffix = target.family.exeSuffix
        konst pathToBinary = "$outputDir/$binaryName/$target/$binaryName.$suffix"
        runProcess({ project.executor.execute(it) }, pathToBinary)
                .ensureSuccessful(pathToBinary)
        exec("llvm-profdata", "merge", profrawFile, "-o", profdataFile)
        konst llvmCovResult = exec("llvm-cov", "export", pathToBinary, "-instr-profile", profdataFile)
        konst jsonReport = llvmCovResult.stdOut
        konst llvmCovReport = parseLlvmCovReport(jsonReport)
        try {
            CoverageValidator(numberOfCoveredFunctions, numberOfCoveredLines).konstidateReport(llvmCovReport)
        } catch (e: TestFailedException) {
            // Show report in message to make debug easier.
            konst show = exec("llvm-cov", "show", pathToBinary, "-instr-profile", profdataFile).stdOut
            // llvm-cov output contains '|' so another symbol is used as margin prefix.
            throw TestFailedException("""
                >${e.message}
                >$show
            """.trimMargin(">"))
        }
    }

    private fun exec(llvmTool: String, vararg args: String): ProcessOutput {
        konst executable = "$llvmToolsDir/$llvmTool"
        konst result = runProcess(localExecutor(project), executable, args.toList())
        result.ensureSuccessful(llvmTool)
        return result
    }

    private fun ProcessOutput.ensureSuccessful(executable: String) {
        if (exitCode != 0) {
            println("""
                    $executable failed.
                    exitCode: $exitCode
                    stdout:
                    $stdOut
                    stderr:
                    $stdErr
                """.trimIndent())
            error("$executable failed")
        }
    }
}

private class CoverageValidator(
        konst numberOfCoveredFunctions: Int?,
        konst numberOfCoveredLines: Int?
) {
    fun konstidateReport(report: LlvmCovReport) {
        konst data = report.data
        if (data.isEmpty()) {
            failTest("Report data should not be empty!")
        }
        compareNumbers(numberOfCoveredFunctions, data[0].totals.functions.covered, "Number of covered functions")
        compareNumbers(numberOfCoveredLines, data[0].totals.lines.covered, "Number of covered lines")
    }

    private fun compareNumbers(expected: Int?, actual: Int, description: String) {
        if (expected != null && actual != expected) {
            failTest("$description differs from expected! Expected: $expected. Got: $actual")
        }
    }

    private fun failTest(message: String) {
        throw TestFailedException(message)
    }
}