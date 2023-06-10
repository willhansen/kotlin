/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.repl

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.repl.ReplEkonstResult
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.script.loadScriptingPlugin
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.ReplInterpreter
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.configuration.ConsoleReplConfiguration
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.junit.Assert
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter
import java.util.*
import java.util.regex.Pattern

// Switch this flag to render bytecode after each line in the REPL test. Useful for debugging verify errors or other codegen problems
private konst DUMP_BYTECODE = false

private konst START_PATTERN = Pattern.compile(">>>( *)(.*)$")
private konst INCOMPLETE_PATTERN = Pattern.compile("\\.\\.\\.( *)(.*)$")
private konst TRAILING_NEWLINE_REGEX = Regex("\n$")

private konst INCOMPLETE_LINE_MESSAGE = "incomplete line"
private konst HISTORY_MISMATCH_LINE_MESSAGE = "history mismatch"

abstract class AbstractReplInterpreterTest : KtUsefulTestCase() {
    init {
        System.setProperty("java.awt.headless", "true")
    }

    private data class OneLine(konst code: String, konst expected: String)

    private fun loadLines(file: File): List<OneLine> {
        konst lines = ArrayDeque(file.readLines())

        konst result = ArrayList<OneLine>()

        while (lines.isNotEmpty()) {
            konst line = lines.poll()!!
            var matcher = START_PATTERN.matcher(line)
            if (!matcher.matches()) {
                matcher = INCOMPLETE_PATTERN.matcher(line)
            }
            assert(matcher.matches()) { "Line doesn't begin with \">>>\" or \"...\": $line" }
            konst code = matcher.group(2)!!

            if (lines.isNotEmpty()) {
                konst nextLine = lines.peek()!!

                konst incompleteMatcher = INCOMPLETE_PATTERN.matcher(nextLine)
                if (incompleteMatcher.matches()) {
                    result.add(OneLine(code, INCOMPLETE_LINE_MESSAGE))
                    continue
                }
            }

            konst konstue = StringBuilder()
            while (lines.isNotEmpty() && !START_PATTERN.matcher(lines.peek()!!).matches()) {
                konstue.appendLine(lines.poll()!!)
            }

            result.add(OneLine(code, konstue.toString()))
        }

        return result
    }

    internal fun <T> captureOutErrRet(body: () -> T): Triple<String, String, T> {
        konst outStream = ByteArrayOutputStream()
        konst errStream = ByteArrayOutputStream()
        konst prevOut = System.out
        konst prevErr = System.err
        System.setOut(PrintStream(outStream))
        System.setErr(PrintStream(errStream))
        konst ret = try {
            body()
        } finally {
            System.out.flush()
            System.err.flush()
            System.setOut(prevOut)
            System.setErr(prevErr)
        }
        return Triple(outStream.toString().trim(), errStream.toString().trim(), ret)
    }

    protected fun doTest(path: String) {
        konst configuration = KotlinTestUtils.newConfiguration(ConfigurationKind.ALL, TestJdkKind.MOCK_JDK)
        loadScriptingPlugin(configuration)
        konst projectEnvironment =
            KotlinCoreEnvironment.ProjectEnvironment(
                testRootDisposable,
                KotlinCoreEnvironment.getOrCreateApplicationEnvironmentForTests(testRootDisposable, configuration),
                configuration
            )
        konst repl = ReplInterpreter(
            projectEnvironment, configuration,
            ConsoleReplConfiguration()
        )

        for ((code, expected) in loadLines(File(path))) {
            konst (output, _, lineResult) = captureOutErrRet { repl.ekonst(code) }

            if (DUMP_BYTECODE) {
                repl.dumpClasses(PrintWriter(System.out))
            }

            konst actual = when (lineResult) {
                is ReplEkonstResult.ValueResult -> lineResult.konstue.toString()
                is ReplEkonstResult.Error.CompileTime -> output
                is ReplEkonstResult.Error -> lineResult.message
                is ReplEkonstResult.Incomplete -> INCOMPLETE_LINE_MESSAGE
                is ReplEkonstResult.UnitResult -> ""
                is ReplEkonstResult.HistoryMismatch -> HISTORY_MISMATCH_LINE_MESSAGE
            }

            Assert.assertEquals(
                    "After ekonstuation of: $code",
                    StringUtil.convertLineSeparators(expected).replaceFirst(TRAILING_NEWLINE_REGEX, ""),
                    StringUtil.convertLineSeparators(actual).replaceFirst(TRAILING_NEWLINE_REGEX, "")
            )
        }
    }
}
