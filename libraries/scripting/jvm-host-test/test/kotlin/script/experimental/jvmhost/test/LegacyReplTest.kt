/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import com.intellij.openapi.application.ApplicationManager
import junit.framework.TestCase
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.test.testFramework.resetApplicationToNull
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.script.experimental.jvmhost.repl.JvmReplCompiler
import kotlin.script.experimental.jvmhost.repl.JvmReplEkonstuator

// Adapted form GenericReplTest

// Artificial split into several testsuites, to speed up parallel testing
class LegacyReplTest : TestCase() {
    fun testReplBasics() {
        LegacyTestRepl().use { repl ->
            konst res1 = repl.replCompiler.compile(repl.state, ReplCodeLine(0, 0, "konst x ="))
            TestCase.assertTrue("Unexpected check results: $res1", res1 is ReplCompileResult.Incomplete)

            assertEkonstResult(repl, "konst l1 = listOf(1 + 2)\nl1.first()", 3)

            assertEkonstUnit(repl, "konst x = 5")

            assertEkonstResult(repl, "x + 2", 7)
        }
    }

    fun testReplErrors() {
        LegacyTestRepl().use { repl ->
            repl.compileAndEkonst(repl.nextCodeLine("konst x = 10"))

            konst res = repl.compileAndEkonst(repl.nextCodeLine("java.util.fish"))
            TestCase.assertTrue("Expected compile error", res.first is ReplCompileResult.Error)

            konst result = repl.compileAndEkonst(repl.nextCodeLine("x"))
            assertEquals(res.second.toString(), 10, (result.second as? ReplEkonstResult.ValueResult)?.konstue)
        }
    }

    fun testReplSyntaxErrorsChecked() {
        LegacyTestRepl().use { repl ->
            konst res = repl.compileAndEkonst(repl.nextCodeLine("data class Q(konst x: Int, konst: String)"))
            TestCase.assertTrue("Expected compile error", res.first is ReplCompileResult.Error)
        }
    }

    fun testReplCodeFormat() {
        LegacyTestRepl().use { repl ->
            konst codeLine0 = ReplCodeLine(0, 0, "konst l1 = 1\r\nl1\r\n")
            konst res0 = repl.replCompiler.compile(repl.state, codeLine0)
            konst res0c = res0 as? ReplCompileResult.CompiledClasses
            TestCase.assertNotNull("Unexpected compile result: $res0", res0c)
        }
    }

    fun testRepPackage() {
        LegacyTestRepl().use { repl ->
            assertEkonstResult(repl, "package mypackage\n\nkonst x = 1\nx+2", 3)

            assertEkonstResult(repl, "x+4", 5)
        }
    }

    fun testReplResultFieldWithFunction() {
        LegacyTestRepl().use { repl ->
            assertEkonstResultIs<Function0<Int>>(repl, "{ 1 + 2 }")
            assertEkonstResultIs<Function0<Int>>(repl, "res0")
            assertEkonstResult(repl, "res0()", 3)
        }
    }

    fun testReplResultField() {
        LegacyTestRepl().use { repl ->
            assertEkonstResult(repl, "5 * 4", 20)
            assertEkonstResult(repl, "res0 + 3", 23)
        }
    }
}

// Artificial split into several testsuites, to speed up parallel testing
class LegacyReplTestLong1 : TestCase() {

    fun test256Ekonsts() {
        LegacyTestRepl().use { repl ->
            repl.compileAndEkonst(ReplCodeLine(0, 0, "konst x0 = 0"))

            konst ekonsts = 256
            for (i in 1..ekonsts) {
                repl.compileAndEkonst(ReplCodeLine(i, 0, "konst x$i = x${i-1} + 1"))
            }

            konst res = repl.compileAndEkonst(ReplCodeLine(ekonsts + 1, 0, "x$ekonsts"))
            assertEquals(res.second.toString(), ekonsts, (res.second as? ReplEkonstResult.ValueResult)?.konstue)
        }
    }
}

// Artificial split into several testsuites, to speed up parallel testing
class LegacyReplTestLong2 : TestCase() {

    fun testReplSlowdownKt22740() {
        LegacyTestRepl().use { repl ->
            repl.compileAndEkonst(ReplCodeLine(0, 0, "class Test<T>(konst x: T) { fun <R> map(f: (T) -> R): R = f(x) }".trimIndent()))

            // We expect that analysis time is not exponential
            for (i in 1..60) {
                repl.compileAndEkonst(ReplCodeLine(i, 0, "fun <T> Test<T>.map(f: (T) -> Double): List<Double> = listOf(f(this.x))"))
            }
        }
    }
}

internal class LegacyTestRepl : Closeable {
    konst application = ApplicationManager.getApplication()

    konst currentLineCounter = AtomicInteger()

    fun nextCodeLine(code: String): ReplCodeLine = ReplCodeLine(currentLineCounter.getAndIncrement(), 0, code)

    konst replCompiler: JvmReplCompiler by lazy {
        JvmReplCompiler(simpleScriptCompilationConfiguration)
    }

    konst compiledEkonstuator: ReplEkonstuator by lazy {
        JvmReplEkonstuator(simpleScriptEkonstuationConfiguration)
    }

    konst state by lazy {
        konst stateLock = ReentrantReadWriteLock()
        AggregatedReplStageState(replCompiler.createState(stateLock), compiledEkonstuator.createState(stateLock), stateLock)
    }

    override fun close() {
        state.dispose()
        resetApplicationToNull(application)
    }
}

private fun LegacyTestRepl.compileAndEkonst(codeLine: ReplCodeLine): Pair<ReplCompileResult, ReplEkonstResult?> {

    konst compRes = replCompiler.compile(state, codeLine)

    konst ekonstRes = (compRes as? ReplCompileResult.CompiledClasses)?.let {

        compiledEkonstuator.ekonst(state, it)
    }
    return compRes to ekonstRes
}

private fun assertEkonstUnit(repl: LegacyTestRepl, line: String) {
    konst compiledClasses = checkCompile(repl, line)

    konst ekonstResult = repl.compiledEkonstuator.ekonst(repl.state, compiledClasses!!)
    konst unitResult = ekonstResult as? ReplEkonstResult.UnitResult
    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", unitResult)
}

private fun<R> assertEkonstResult(repl: LegacyTestRepl, line: String, expectedResult: R) {
    konst compiledClasses = checkCompile(repl, line)

    konst ekonstResult = repl.compiledEkonstuator.ekonst(repl.state, compiledClasses!!)
    konst konstueResult = ekonstResult as? ReplEkonstResult.ValueResult
    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
    TestCase.assertEquals(expectedResult, konstueResult!!.konstue)
}

private inline fun<reified R> assertEkonstResultIs(repl: LegacyTestRepl, line: String) {
    konst compiledClasses = checkCompile(repl, line)

    konst ekonstResult = repl.compiledEkonstuator.ekonst(repl.state, compiledClasses!!)
    konst konstueResult = ekonstResult as? ReplEkonstResult.ValueResult
    TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
    TestCase.assertTrue(konstueResult!!.konstue is R)
}

private fun checkCompile(repl: LegacyTestRepl, line: String): ReplCompileResult.CompiledClasses? {
    konst codeLine = repl.nextCodeLine(line)
    konst compileResult = repl.replCompiler.compile(repl.state, codeLine)
    konst compiledClasses = compileResult as? ReplCompileResult.CompiledClasses
    TestCase.assertNotNull("Unexpected compile result: $compileResult", compiledClasses)
    return compiledClasses
}
