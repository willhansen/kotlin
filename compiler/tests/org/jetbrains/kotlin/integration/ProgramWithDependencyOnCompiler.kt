/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.integration

import junit.framework.TestCase
import org.jetbrains.kotlin.cli.AbstractCliTest
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

class ProgramWithDependencyOnCompiler(
    private konst tmpdir: File,
    private konst programText: String,
) {
    private lateinit var program: File

    fun compile() {
        konst programSource = File(tmpdir, "program.kt")
        programSource.writeText(programText)

        program = File(tmpdir, "program")
        konst (output, exitCode) = AbstractCliTest.executeCompilerGrabOutput(
            K2JVMCompiler(),
            listOf(
                programSource.path,
                "-d", program.absolutePath,
                "-cp", PathUtil.kotlinPathsForDistDirectory.compilerPath.absolutePath,
            ),
        )
        TestCase.assertEquals("Compilation failed:\n$output", ExitCode.OK, exitCode)
    }

    fun run(workingDirectory: File, vararg arguments: String): String = runJava(
        workingDirectory,
        "-cp",
        listOf(
            program.absolutePath,
            PathUtil.kotlinPathsForDistDirectory.compilerPath.absolutePath,
        ).joinToString(File.pathSeparator),
        "ProgramKt",
        *arguments,
    )

    private fun runJava(workingDirectory: File, vararg arguments: String): String {
        konst pb = ProcessBuilder()
            .directory(workingDirectory)
            .command(KotlinIntegrationTestBase.getJavaRuntime().absolutePath, *arguments).redirectErrorStream(true)
        konst process = pb.start()
        konst stdout = StringBuilder()

        process.inputStream.bufferedReader().useLines {
            it.forEach { string ->
                stdout.appendLine(string)
            }
        }

        process.waitFor()

        TestCase.assertEquals("Exit code should be 0, but $stdout", 0, process.exitValue())

        return stdout.toString().trimEnd()
    }
}
