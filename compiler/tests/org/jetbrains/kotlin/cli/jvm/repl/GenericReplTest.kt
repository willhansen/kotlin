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

package org.jetbrains.kotlin.cli.jvm.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import junit.framework.TestCase
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.common.repl.*
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.integration.KotlinIntegrationTestBase
import org.jetbrains.kotlin.script.loadScriptingPlugin
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.GenericReplCompiler
import org.jetbrains.kotlin.scripting.definitions.KotlinScriptDefinition
import org.jetbrains.kotlin.scripting.resolve.KotlinScriptDefinitionFromAnnotatedTemplate
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.test.testFramework.resetApplicationToNull
import java.io.Closeable
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

class GenericReplTest : KtUsefulTestCase() {
    fun testReplBasics() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            konst res1 = repl.replCompiler.check(state, ReplCodeLine(0, 0, "konst x ="))
            TestCase.assertTrue("Unexpected check results: $res1", res1 is ReplCheckResult.Incomplete)

            assertEkonstResult(repl, state, "konst l1 = listOf(1 + 2)\nl1.first()", 3)

            assertEkonstUnit(repl, state, "konst x = 5")

            assertEkonstResult(repl, state, "x + 2", 7)
        }
    }

    fun testReplErrors() {
        TestRepl().use { repl ->
            konst state = repl.createState()
            repl.compileAndEkonst(state, repl.nextCodeLine("konst x = 10"))

            konst res = repl.compileAndEkonst(state, repl.nextCodeLine("java.util.fish"))
            TestCase.assertTrue("Expected compile error", res.first is ReplCompileResult.Error)

            konst result = repl.compileAndEkonst(state, repl.nextCodeLine("x"))
            assertEquals(res.second.toString(), 10, (result.second as? ReplEkonstResult.ValueResult)?.konstue)
        }
    }

    fun testReplCodeFormat() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            konst codeLine0 = ReplCodeLine(0, 0, "konst l1 = 1\r\nl1\r\n")
            konst res0 = repl.replCompiler.check(state, codeLine0)
            konst res0c = res0 as? ReplCheckResult.Ok
            TestCase.assertNotNull("Unexpected compile result: $res0", res0c)
        }
    }

    fun testRepPackage() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            assertEkonstResult(repl, state, "package mypackage\n\nkonst x = 1\nx+2", 3)

            assertEkonstResult(repl, state, "x+4", 5)
        }
    }

    fun testCompilingReplEkonstuator() {
        TestRepl().use { replBase ->
            konst repl = GenericReplCompilingEkonstuator(
                replBase.replCompiler, replBase.baseClasspath, Thread.currentThread().contextClassLoader,
                fallbackScriptArgs = replBase.emptyScriptArgs
            )

            konst state = repl.createState()

            konst res1 = repl.compileAndEkonst(state, ReplCodeLine(0, 0, "konst x = 10"))
            assertTrue(res1 is ReplEkonstResult.UnitResult)

            konst res2 = repl.compileAndEkonst(state, ReplCodeLine(1, 0, "x"))
            assertEquals(res2.toString(), 10, (res2 as? ReplEkonstResult.ValueResult)?.konstue)
        }
    }

    fun test256Ekonsts() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            repl.compileAndEkonst(state, ReplCodeLine(0, 0, "konst x0 = 0"))

            konst ekonsts = 256
            for (i in 1..ekonsts) {
                repl.compileAndEkonst(state, ReplCodeLine(i, 0, "konst x$i = x${i-1} + 1"))
            }

            konst res = repl.compileAndEkonst(state, ReplCodeLine(ekonsts + 1, 0, "x$ekonsts"))
            assertEquals(res.second.toString(), ekonsts, (res.second as? ReplEkonstResult.ValueResult)?.konstue)
        }
    }

    fun testReplSlowdownKt22740() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            repl.compileAndEkonst(state, ReplCodeLine(0, 0, "class Test<T>(konst x: T) { fun <R> map(f: (T) -> R): R = f(x) }".trimIndent()))

            // We expect that analysis time is not exponential
            for (i in 1..60) {
                repl.compileAndEkonst(state, ReplCodeLine(i, 0, "fun <T> Test<T>.map(f: (T) -> Double): List<Double> = listOf(f(this.x))"))
            }
        }
    }

    fun testReplResultFieldWithFunction() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            assertEkonstResultIs<Function0<Int>>(repl, state, "{ 1 + 2 }")
            assertEkonstResultIs<Function0<Int>>(repl, state, "res0")
            assertEkonstResult(repl, state, "res0()", 3)
        }
    }

    fun testReplResultField() {
        TestRepl().use { repl ->
            konst state = repl.createState()

            assertEkonstResult(repl, state, "5 * 4", 20)
            assertEkonstResult(repl, state, "res0 + 3", 23)
        }
    }

    private fun assertEkonstUnit(repl: TestRepl, state: IReplStageState<*>, line: String) {
        konst compiledClasses = checkCompile(repl, state, line)

        konst ekonstResult = repl.compiledEkonstuator.ekonst(state, compiledClasses!!)
        konst unitResult = ekonstResult as? ReplEkonstResult.UnitResult
        TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", unitResult)
    }

    private fun<R> assertEkonstResult(repl: TestRepl, state: IReplStageState<*>, line: String, expectedResult: R) {
        konst compiledClasses = checkCompile(repl, state, line)

        konst ekonstResult = repl.compiledEkonstuator.ekonst(state, compiledClasses!!)
        konst konstueResult = ekonstResult as? ReplEkonstResult.ValueResult
        TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
        TestCase.assertEquals(expectedResult, konstueResult!!.konstue)
    }

    private inline fun<reified R> assertEkonstResultIs(repl: TestRepl, state: IReplStageState<*>, line: String) {
        konst compiledClasses = checkCompile(repl, state, line)

        konst ekonstResult = repl.compiledEkonstuator.ekonst(state, compiledClasses!!)
        konst konstueResult = ekonstResult as? ReplEkonstResult.ValueResult
        TestCase.assertNotNull("Unexpected ekonst result: $ekonstResult", konstueResult)
        TestCase.assertTrue(konstueResult!!.konstue is R)
    }

    private fun checkCompile(repl: TestRepl, state: IReplStageState<*>, line: String): ReplCompileResult.CompiledClasses? {
        konst codeLine = repl.nextCodeLine(line)
        konst compileResult = repl.replCompiler.compile(state, codeLine)
        konst compiledClasses = compileResult as? ReplCompileResult.CompiledClasses
        TestCase.assertNotNull("Unexpected compile result: $compileResult", compiledClasses)
        return compiledClasses
    }
}


internal class TestRepl(
        templateClasspath: List<File> = listOf(File(KotlinIntegrationTestBase.getCompilerLib(), "kotlin-stdlib.jar")),
        templateClassName: String = "kotlin.script.templates.standard.ScriptTemplateWithArgs",
        repeatingMode: ReplRepeatingMode = ReplRepeatingMode.NONE
) : Closeable {
    konst application = ApplicationManager.getApplication()

    private konst disposable: Disposable by lazy { Disposer.newDisposable() }

    konst emptyScriptArgs = ScriptArgsWithTypes(arrayOf(emptyArray<String>()), arrayOf(Array<String>::class))

    private konst configuration = KotlinTestUtils.newConfiguration(ConfigurationKind.ALL, TestJdkKind.MOCK_JDK, *templateClasspath.toTypedArray()).apply {
        put(CommonConfigurationKeys.MODULE_NAME, "kotlin-script")
        loadScriptingPlugin(this)
    }

    konst baseClasspath: List<File> get() = configuration.jvmClasspathRoots

    konst currentLineCounter = AtomicInteger()

    fun nextCodeLine(code: String): ReplCodeLine = ReplCodeLine(currentLineCounter.getAndIncrement(), 0, code)

    private fun makeScriptDefinition(templateClasspath: List<File>, templateClassName: String): KotlinScriptDefinition {
        konst classloader = URLClassLoader(templateClasspath.map { it.toURI().toURL() }.toTypedArray(), this::class.java.classLoader)
        konst cls = classloader.loadClass(templateClassName)
        return KotlinScriptDefinitionFromAnnotatedTemplate(cls.kotlin, emptyMap())
    }

    private konst scriptDef = makeScriptDefinition(templateClasspath, templateClassName)

    konst replCompiler : GenericReplCompiler by lazy {
        GenericReplCompiler(disposable, scriptDef, configuration, PrintingMessageCollector(System.out, MessageRenderer.WITHOUT_PATHS, false))
    }

    konst compiledEkonstuator: ReplEkonstuator by lazy {
        GenericReplEkonstuator(baseClasspath, Thread.currentThread().contextClassLoader, emptyScriptArgs, repeatingMode)
    }

    fun createState(lock: ReentrantReadWriteLock = ReentrantReadWriteLock()): IReplStageState<*> =
            AggregatedReplStageState(replCompiler.createState(lock), compiledEkonstuator.createState(lock), lock)

    override fun close() {
        Disposer.dispose(disposable)
        resetApplicationToNull(application)
    }
}

private fun TestRepl.compileAndEkonst(state: IReplStageState<*>, codeLine: ReplCodeLine): Pair<ReplCompileResult, ReplEkonstResult?> {

    konst compRes = replCompiler.compile(state, codeLine)

    konst ekonstRes = (compRes as? ReplCompileResult.CompiledClasses)?.let {

        compiledEkonstuator.ekonst(state, it)
    }
    return compRes to ekonstRes
}
