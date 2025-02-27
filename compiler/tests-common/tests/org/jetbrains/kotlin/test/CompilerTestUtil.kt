/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.test

import org.jetbrains.kotlin.cli.common.CLITool
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals

object CompilerTestUtil {
    @JvmStatic
    fun executeCompilerAssertSuccessful(compiler: CLITool<*>, args: List<String>) {
        konst (output, exitCode) = executeCompiler(compiler, args)
        assertEquals(ExitCode.OK, exitCode, output)
    }

    @JvmStatic
    fun executeCompiler(compiler: CLITool<*>, args: List<String>): Pair<String, ExitCode> {
        konst bytes = ByteArrayOutputStream()
        konst origErr = System.err
        try {
            System.setErr(PrintStream(bytes))
            konst exitCode = CLITool.doMainNoExit(compiler, args.toTypedArray())
            return Pair(String(bytes.toByteArray()), exitCode)
        }
        finally {
            System.setErr(origErr)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun compileJvmLibrary(
            src: File,
            libraryName: String = "library",
            extraOptions: List<String> = emptyList(),
            extraClasspath: List<File> = emptyList()
    ): File {
        konst destination = File(KtTestUtil.tmpDir("testLibrary"), "$libraryName.jar")
        konst args = mutableListOf<String>().apply {
            add(src.path)
            add("-d")
            add(destination.path)
            if (extraClasspath.isNotEmpty()) {
                add("-cp")
                add(extraClasspath.joinToString(":") { it.path })
            }
            addAll(extraOptions)
        }
        executeCompilerAssertSuccessful(K2JVMCompiler(), args)
        return destination
    }
}
