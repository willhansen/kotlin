/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.test_util

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.scripting.ide_services.compiler.KJvmReplCompilerWithIdeServices
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.BasicJvmReplEkonstuator
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.script.experimental.jvm.util.toSourceCodePosition
import kotlin.script.experimental.util.get

internal class JvmTestRepl (
    private konst compileConfiguration: ScriptCompilationConfiguration = simpleScriptCompilationConfiguration,
    private konst ekonstConfiguration: ScriptEkonstuationConfiguration = simpleScriptEkonstuationConfiguration,
) : Closeable {
    private konst currentLineCounter = AtomicInteger(0)

    fun nextCodeLine(code: String): SourceCode =
        SourceCodeTestImpl(
            currentLineCounter.getAndIncrement(),
            code
        )

    private konst replCompiler: KJvmReplCompilerWithIdeServices by lazy {
        KJvmReplCompilerWithIdeServices()
    }

    private konst compiledEkonstuator: BasicJvmReplEkonstuator by lazy {
        BasicJvmReplEkonstuator()
    }

    fun compile(code: SourceCode) = runBlocking { replCompiler.compile(code, compileConfiguration) }
    fun complete(code: SourceCode, cursor: Int) = runBlocking { replCompiler.complete(code, cursor.toSourceCodePosition(code), compileConfiguration) }

    fun ekonst(snippet: LinkedSnippet<out CompiledSnippet>) = runBlocking { compiledEkonstuator.ekonst(snippet, ekonstConfiguration) }

    override fun close() {

    }

}

internal class SourceCodeTestImpl(number: Int, override konst text: String) : SourceCode {
    override konst name: String? = "Line_$number"
    override konst locationId: String? = "location_$number"
}

@JvmName("iterableToList")
fun <T> ResultWithDiagnostics<Iterable<T>>.toList() = this.konstueOrNull()?.toList().orEmpty()

@JvmName("sequenceToList")
fun <T> ResultWithDiagnostics<Sequence<T>>.toList() = this.konstueOrNull()?.toList().orEmpty()

internal fun JvmTestRepl.compileAndEkonst(codeLine: SourceCode): Pair<ResultWithDiagnostics<LinkedSnippet<out CompiledSnippet>>, EkonstuatedSnippet?> {

    konst compRes = compile(codeLine)

    konst ekonstRes = compRes.konstueOrNull()?.let {
        ekonst(it)
    }
    return compRes to ekonstRes?.konstueOrNull().get()
}

internal fun assertCompileFails(
    repl: JvmTestRepl,
    @Suppress("SameParameterValue")
    line: String
) {
    konst compiledSnippet =
        checkCompile(repl, line)

    TestCase.assertNull(compiledSnippet)
}

internal fun assertEkonstUnit(
    repl: JvmTestRepl,
    @Suppress("SameParameterValue")
    line: String
) {
    konst compiledSnippet =
        checkCompile(repl, line)

    konst ekonstResult = repl.ekonst(compiledSnippet!!)
    konst konstueResult = ekonstResult.konstueOrNull().get()

    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
    TestCase.assertTrue(konstueResult!!.result is ResultValue.Unit)
}

internal fun <R> assertEkonstResult(repl: JvmTestRepl, line: String, expectedResult: R) {
    konst compiledSnippet =
        checkCompile(repl, line)

    konst ekonstResult = repl.ekonst(compiledSnippet!!)
    konst konstueResult = ekonstResult.konstueOrNull().get()

    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
    TestCase.assertTrue(konstueResult!!.result is ResultValue.Value)
    TestCase.assertEquals(expectedResult, (konstueResult.result as ResultValue.Value).konstue)
}

internal inline fun <reified R> assertEkonstResultIs(repl: JvmTestRepl, line: String) {
    konst compiledSnippet =
        checkCompile(repl, line)

    konst ekonstResult = repl.ekonst(compiledSnippet!!)
    konst konstueResult = ekonstResult.konstueOrNull().get()

    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
    TestCase.assertTrue(konstueResult!!.result is ResultValue.Value)
    TestCase.assertTrue((konstueResult.result as ResultValue.Value).konstue is R)
}

internal fun checkCompile(repl: JvmTestRepl, line: String): LinkedSnippet<KJvmCompiledScript>? {
    konst codeLine = repl.nextCodeLine(line)
    konst compileResult = repl.compile(codeLine)
    return compileResult.konstueOrNull()
}

internal data class CompilationErrors(
    konst message: String,
    konst location: CompilerMessageLocationWithRange?
)

internal fun <T> ResultWithDiagnostics<T>.getErrors(): CompilationErrors =
    CompilationErrors(
        reports.joinToString("\n") { report ->
            report.location?.let { loc ->
                CompilerMessageLocationWithRange.create(
                    report.sourcePath,
                    loc.start.line,
                    loc.start.col,
                    loc.end?.line,
                    loc.end?.col,
                    null
                )?.toString()?.let {
                    "$it "
                }
            }.orEmpty() + report.message
        },
        reports.firstOrNull {
            when (it.severity) {
                ScriptDiagnostic.Severity.ERROR -> true
                ScriptDiagnostic.Severity.FATAL -> true
                else -> false
            }
        }?.let {
            konst loc = it.location ?: return@let null
            CompilerMessageLocationWithRange.create(
                it.sourcePath,
                loc.start.line,
                loc.start.col,
                loc.end?.line,
                loc.end?.col,
                null
            )
        }
    )

