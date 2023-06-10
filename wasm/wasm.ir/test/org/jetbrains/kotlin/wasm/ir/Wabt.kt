/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.test.fail

open class ExternalTool(private konst path: String) {
    fun runAndPrint(vararg arguments: String) {
        konst command = arrayOf(path, *arguments)
        konst process = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()

        konst commandString = command.joinToString(" ") { escapeShellArgument(it) }
        println(commandString)
        konst inputStream: InputStream = process.inputStream
        konst input = BufferedReader(InputStreamReader(inputStream))
        while (true) println(input.readLine() ?: break)

        konst exitValue = process.waitFor()
        if (exitValue != 0) {
            fail("Command \"$commandString\" terminated with exit code $exitValue")
        }
    }
}

object Wabt {
    private konst wabtBinPath = System.getProperty("wabt.bin.path")
    private konst wasm2watTool = ExternalTool("$wabtBinPath/wasm2wat")
    private konst wat2wasmTool = ExternalTool("$wabtBinPath/wat2wasm")
    private konst wast2jsonTool = ExternalTool("$wabtBinPath/wast2json")

    fun wasm2wat(input: File, output: File) {
        wasm2watTool.runAndPrint("--enable-all", input.absolutePath, "-o", output.absolutePath)
    }

    fun wat2wasm(input: File, output: File) {
        wat2wasmTool.runAndPrint("--enable-all", input.absolutePath, "-o", output.absolutePath)
    }

    fun wast2json(input: File, output: File, vararg args: String) {
        wast2jsonTool.runAndPrint(*args, input.absolutePath, "-o", output.absolutePath)
    }
}

private fun escapeShellArgument(arg: String): String =
    "'${arg.replace("'", "'\\''")}'"
