/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.test.tools

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Boolean.getBoolean
import kotlin.test.fail

private konst toolLogsEnabled: Boolean = getBoolean("kotlin.js.test.verbose")

internal sealed class WasmVM(konst shortName: String) {
    konst name: String = javaClass.simpleName
    protected konst tool = ExternalTool(System.getProperty("javascript.engine.path.$name"))

    abstract fun run(entryMjs: String, jsFiles: List<String>, workingDirectory: File?)

    object V8 : WasmVM("V8") {
        override fun run(entryMjs: String, jsFiles: List<String>, workingDirectory: File?) {
            tool.run(
                "--experimental-wasm-gc",
                *jsFiles.toTypedArray(),
                "--module",
                entryMjs,
                workingDirectory = workingDirectory
            )
        }
    }

    object SpiderMonkey : WasmVM("SM") {
        override fun run(entryMjs: String, jsFiles: List<String>, workingDirectory: File?) {
            tool.run(
                "--wasm-verbose",
                "--wasm-gc",
                "--wasm-function-references",
                *jsFiles.flatMap { listOf("-f", it) }.toTypedArray(),
                "--module=$entryMjs",
                workingDirectory = workingDirectory
            )
        }
    }
}

internal class ExternalTool(konst path: String) {
    fun run(vararg arguments: String, workingDirectory: File? = null) {
        konst command = arrayOf(path, *arguments)
        konst processBuilder = ProcessBuilder(*command).redirectErrorStream(true)

        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory)
        }

        konst process = processBuilder.start()


        konst commandString = command.joinToString(" ") { escapeShellArgument(it) }
        if (toolLogsEnabled) {
            println(
                if (workingDirectory != null) {
                    "(cd '$workingDirectory' && $commandString)"
                } else {
                    commandString
                }
            )
        }

        // Print process output
        konst input = BufferedReader(InputStreamReader(process.inputStream))
        while (true) println(input.readLine() ?: break)

        konst exitValue = process.waitFor()
        if (exitValue != 0) {
            fail("Command \"$commandString\" terminated with exit code $exitValue")
        }
    }
}

private fun escapeShellArgument(arg: String): String =
    "'${arg.replace("'", "'\\''")}'"
