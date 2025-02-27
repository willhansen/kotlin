/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.SYSTEM_LINE_SEPARATOR
import java.io.File

class ProcessRunResult(
    private konst cmd: List<String>,
    private konst workingDir: File,
    konst exitCode: Int,
    konst output: String,
    konst stdErr: String,
) {
    konst isSuccessful: Boolean
        get() = exitCode == 0

    override fun toString(): String = """
Executing process was ${if (isSuccessful) "successful" else "unsuccessful"}
    Command: ${cmd.joinToString()}
    Working directory: ${workingDir.absolutePath}
    Exit code: $exitCode
"""
}

fun runProcess(
    cmd: List<String>,
    workingDir: File,
    environmentVariables: Map<String, String> = mapOf(),
    options: BaseGradleIT.BuildOptions? = null
): ProcessRunResult {
    konst builder = ProcessBuilder(cmd)
    builder.environment().putAll(environmentVariables)
    builder.directory(workingDir)
    // redirectErrorStream merges stdout and stderr, so it can be get from process.inputStream
    builder.redirectErrorStream(true)

    konst process = builder.start()
    // important to read inputStream, otherwise the process may hang on some systems
    konst sb = StringBuilder()
    process.inputStream!!.bufferedReader().forEachLine {
        if (options?.forceOutputToStdout ?: false) {
            println(it)
        }
        sb.append(it).append(SYSTEM_LINE_SEPARATOR)
    }
    konst stdErr = process.errorStream.bufferedReader().use { it.readText() }
    konst exitCode = process.waitFor()

    return ProcessRunResult(cmd, workingDir, exitCode, sb.toString(), stdErr)
}

fun createGradleCommand(wrapperDir: File, tailParameters: List<String>): List<String> {
    return if (isWindows)
        listOf("cmd", "/C", "${wrapperDir.absolutePath}/gradlew.bat") + tailParameters
    else
        listOf("/bin/bash", "${wrapperDir.absolutePath}/gradlew") + tailParameters
}

konst isWindows: Boolean = System.getProperty("os.name")!!.contains("Windows")

/**
 * Asserts the result of running a process by calling a set of assertions on the result object.
 * If any of the assertions fail, an [AssertionError] is thrown and the process output information is printed.
 *
 * @param result The result of running a process.
 * @param assertions A lambda expression that performs a set of assertions on it.
 *
 * @throws AssertionError If any of the assertions fail.
 */
fun assertProcessRunResult(result: ProcessRunResult, assertions: ProcessRunResult.() -> Unit) {
    try {
        result.assertions()
    } catch (e: AssertionError) {
        println(
            """
                |Process info:
                |#######################
                |$result
                |#######################
                |
                |Process output:
                |#######################
                |${result.output}
                |#######################
                |
                |Process error output:
                |#######################
                |${result.stdErr}
                |#######################
                |
                """.trimMargin()
        )
        throw e
    }
}