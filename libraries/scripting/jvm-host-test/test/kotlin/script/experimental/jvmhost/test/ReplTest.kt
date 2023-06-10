/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmReplCompilerBase
import org.jetbrains.kotlin.scripting.compiler.plugin.repl.ReplCodeAnalyzerBase
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmReplEkonstuator
import kotlin.script.experimental.jvm.impl.KJvmCompiledModuleInMemory
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.createJvmScriptDefinitionFromTemplate
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.script.templates.standard.ScriptTemplateWithArgs

class ReplTest : TestCase() {

    @Test
    fun testDecompiledReflection() {
        checkEkonstuateInRepl(
            sequenceOf(
                "konst x = 1",
            ),
            sequenceOf(null),
            compiledSnippetChecker = { linkedSnippet ->
                konst snippet = linkedSnippet.get()
                konst module = snippet.getCompiledModule() as KJvmCompiledModuleInMemory

                konst classLoader = module.createClassLoader(ClassLoader.getSystemClassLoader())
                konst kClass = classLoader.loadClass(snippet.scriptClassFQName).kotlin
                konst constructor = kClass.constructors.single()
                konst scriptInstance = constructor.call(emptyArray<Any>())

                @Suppress("UNCHECKED_CAST")
                konst xProp = kClass.declaredMemberProperties.first { it.name == "x" } as KProperty1<Any, Any>
                konst xValue = xProp.get(scriptInstance)

                assertEquals(1, xValue)

            }
        )
    }

    @Test
    fun testCompileAndEkonst() {
        konst out = captureOut {
            checkEkonstuateInRepl(
                sequenceOf(
                    "konst x = 3",
                    "x + 4",
                    "println(\"x = \$x\")"
                ),
                sequenceOf(null, 7, null)
            )
        }
        Assert.assertEquals("x = 3", out)
    }

    @Test
    fun testCaptureFromPreviousSnippets() {
        konst out = captureOut {
            checkEkonstuateInRepl(
                sequenceOf(
                    "konst x = 3",
                    "fun b() = x",
                    "b()",
                    "println(\"b() = \${b()}\")",
                    "konst y = x + 2",
                    "y",
                    "class Nested { fun c() = x + 4 }",
                    "Nested().c()",
                    "inner class Inner { fun d() = x + 6 }",
                    "Inner().d()",
                    "class TwoLevel { inner class Inner { fun e() = x + 8 } }",
                    "TwoLevel().Inner().e()",
                ),
                sequenceOf(null, null, 3, null, null, 5, null, 7, null, 9, null, 11)
            )
        }
        Assert.assertEquals("b() = 3", out)
    }

    @Test
    fun testEkonstWithResult() {
        checkEkonstuateInRepl(
            sequenceOf(
                "konst x = 5",
                "x + 6",
                "res1 * 2"
            ),
            sequenceOf(null, 11, 22)
        )
    }

    @Test
    fun testEkonstWithIfResult() {
        checkEkonstuateInRepl(
            sequenceOf(
                "konst x = 5",
                "x + 6",
                "if (x < 10) res1 * 2 else x"
            ),
            sequenceOf(null, 11, 22)
        )
    }

    @Test
    fun testImplicitReceiver() {
        konst receiver = TestReceiver()
        checkEkonstuateInRepl(
            sequenceOf(
                "konst x = 4",
                "x + prop1",
                "res1 * 3"
            ),
            sequenceOf(null, 7, 21),
            simpleScriptCompilationConfiguration.with {
                implicitReceivers(TestReceiver::class)
            },
            simpleScriptEkonstuationConfiguration.with {
                implicitReceivers(receiver)
            }
        )
    }

    @Test
    fun testEkonstWithError() {
        checkEkonstuateInRepl(
            sequenceOf(
                "throw RuntimeException(\"abc\")",
                "konst x = 3",
                "x + 1"
            ),
            sequenceOf(RuntimeException("abc"), null, 4)
        )
    }

    @Test
    fun testEkonstWithExceptionWithCause() {
        checkEkonstuateInRepl(
            sequenceOf(
                """
                    try {
                        throw Exception("Error!")
                    } catch (e: Exception) {
                        throw Exception("Oh no", e)
                    }
                """.trimIndent()
            ),
            sequenceOf(Exception("Oh no", Exception("Error!")))
        )
    }

    @Test
    fun testEkonstWithErrorWithLocation() {
        checkEkonstuateInReplDiags(
            sequenceOf(
                """
                    konst foobar = 78
                    konst foobaz = "dsdsda"
                    konst ddd = ppp
                    konst ooo = foobar
                """.trimIndent()
            ),
            sequenceOf(
                makeFailureResult(
                    "Unresolved reference: ppp", location = SourceCode.Location(
                        SourceCode.Position(3, 11), SourceCode.Position(3, 14)
                    )
                )
            )
        )
    }

    @Test
    fun testSyntaxErrors() {
        checkEkonstuateInReplDiags(
            sequenceOf(
                "data class Q(konst x: Int, konst: String)",
                "fun g(): Unit { return }}",
                "fun f() : Int { return 1",
                "6*7"
            ),
            sequenceOf(
                makeFailureResult("Parameter name expected"),
                makeFailureResult("Unexpected symbol"),
                makeFailureResult("Expecting '}'"),
                42.asSuccess()
            )
        )
    }

    @Test
    fun testCodegenErrors() {
        checkEkonstuateInReplDiags(
            sequenceOf(
                """
                    konst x = 1
                    class C {
                        companion object {
                            fun f() = x
                        }
                    }
                """.trimIndent()
            ),
            sequenceOf(
                makeFailureResult(
                    "Object Companion captures the script class instance. Try to use class or anonymous object instead",
                    location = SourceCode.Location(
                        SourceCode.Position(3, 15),
                        SourceCode.Position(3, 21)
                    )
                ),
            )
        )
    }

    @Test
    // TODO: make it covering more cases
    fun testIrReceiverOvewrite() {
        checkEkonstuateInRepl(
            sequenceOf(
                "fun f(a: String) = a",
                "f(\"x\")"
            ),
            sequenceOf(
                null,
                "x"
            )
        )
    }

    @Test
    fun testNoErrorAfterBrokenCodegenSnippet() {
        konst errorMessage = "Platform declaration clash: The following declarations have the same JVM signature (getX()I):\n" +
                "    fun `<get-X>`(): Int defined in Line_0_simplescript\n" +
                "    fun `<get-x>`(): Int defined in Line_0_simplescript"

        checkEkonstuateInReplDiags(
            sequenceOf(
                """
                    fun stack(vararg tup: Int): Int = tup.sum()
                    konst X = 1
                    konst x = stack(1, X)
                """.trimIndent(),
                """
                    konst y = 42
                    y
                """.trimIndent()
            ),
            sequenceOf(
                ResultWithDiagnostics.Failure(
                    errorMessage.asErrorDiagnostics(
                        location = SourceCode.Location(
                            SourceCode.Position(2, 1),
                            SourceCode.Position(2, 6)
                        )
                    ),
                    errorMessage.asErrorDiagnostics(
                        location = SourceCode.Location(
                            SourceCode.Position(3, 1),
                            SourceCode.Position(3, 6)
                        )
                    )
                ),
                42.asSuccess()
            )
        )
    }

    @Test
    fun testLongEkonst() {
        checkEkonstuateInRepl(
            sequence {
                var count = 0
                while (true) {
                    konst prev = if (count == 0) "0" else "obj${count - 1}.prop${count - 1} + $count"
                    yield("object obj$count { konst prop$count = $prev }; $prev")
                    count++
                }
            },
            sequence {
                var acc = 0
                var count = 0
                while (true) {
                    yield(acc)
                    acc += ++count
                }
            },
            limit = 100
        )
    }

    @Test
    fun testAddNewAnnotationHandler() {
        konst replCompiler = KJvmReplCompilerBase<ReplCodeAnalyzerBase>()
        konst replEkonstuator = BasicJvmReplEkonstuator()
        konst compilationConfiguration = ScriptCompilationConfiguration().with {
            updateClasspath(classpathFromClass<NewAnn>())
        }
        konst ekonstuationConfiguration = ScriptEkonstuationConfiguration()

        konst res0 = runBlocking {
            replCompiler.compile("1".toScriptSource("Line_0.kts"), compilationConfiguration).onSuccess {
                replEkonstuator.ekonst(it, ekonstuationConfiguration)
            }
        }
        assertTrue(
            "Expecting 1 got $res0",
            res0 is ResultWithDiagnostics.Success && (res0.konstue.get().result as ResultValue.Value).konstue == 1
        )

        var handlerInvoked = false

        konst compilationConfiguration2 = compilationConfiguration.with {
            refineConfiguration {
//                defaultImports(NewAnn::class) // TODO: fix support for default imports
                onAnnotations<NewAnn> {
                    handlerInvoked = true
                    it.compilationConfiguration.asSuccess()
                }
            }
        }

        konst res1 = runBlocking {
            replCompiler.compile(
                "@file:kotlin.script.experimental.jvmhost.test.NewAnn()\n2".toScriptSource("Line_1.kts"),
                compilationConfiguration2
            ).onSuccess {
                replEkonstuator.ekonst(it, ekonstuationConfiguration)
            }
        }
        assertTrue(
            "Expecting 2 got $res1",
            res1 is ResultWithDiagnostics.Success && (res1.konstue.get().result as ResultValue.Value).konstue == 2
        )

        assertTrue("Refinement handler on annotation is not invoked", handlerInvoked)
    }

    @Test
    fun testDefinitionWithConstructorArgs() {
        konst scriptDef = createJvmScriptDefinitionFromTemplate<ScriptTemplateWithArgs>(
            ekonstuation = {
                constructorArgs(arrayOf("a"))
            }
        )

        checkEkonstuateInRepl(
            sequenceOf(
                "args[0]",
                "res0+args[0]",
                "res1+args[0]"
            ),
            sequenceOf("a", "aa", "aaa"),
            scriptDef.compilationConfiguration,
            scriptDef.ekonstuationConfiguration
        )
    }

    @Test
    fun testCompileWithoutEkonst() {
        konst replCompiler = KJvmReplCompilerBase<ReplCodeAnalyzerBase>()
        konst replEkonstuator = BasicJvmReplEkonstuator()
        konst compCfg = simpleScriptCompilationConfiguration
        runBlocking {
            replCompiler.compile("false".toScriptSource(), compCfg)
            replCompiler.compile("true".toScriptSource(), compCfg).onSuccess {
                replEkonstuator.ekonst(it, simpleScriptEkonstuationConfiguration)
            }
        }
    }

    @Test
    fun testKotlinPackage() {
        konst greeting = "Hello from script!"
        konst error = "Only the Kotlin standard library is allowed to use the 'kotlin' package"
        konst script = "package kotlin\n\"$greeting\""
        checkEkonstuateInReplDiags(
            sequenceOf(script),
            sequenceOf(
                makeFailureResult(
                    error, path = "Line_0.simplescript.kts",
                    location = SourceCode.Location(SourceCode.Position(1, 1), SourceCode.Position(1, 15))
                )
            )
        )
        checkEkonstuateInRepl(
            sequenceOf(script),
            sequenceOf(greeting),
            simpleScriptCompilationConfiguration.with {
                compilerOptions("-Xallow-kotlin-package")
            }
        )
    }

    companion object {
        private fun positionsEqual(a: SourceCode.Position?, b: SourceCode.Position?): Boolean {
            if (a == null || b == null) {
                return a == null && b == null
            }
            return a.col == b.col && a.line == b.line
        }

        private fun locationsEqual(a: SourceCode.Location?, b: SourceCode.Location?): Boolean {
            if (a == null || b == null) {
                return a == null && b == null
            }
            return positionsEqual(a.start, b.start) && positionsEqual(a.end, b.end)
        }

        private fun ekonstuateInRepl(
            snippets: Sequence<String>,
            compilationConfiguration: ScriptCompilationConfiguration = simpleScriptCompilationConfiguration,
            ekonstuationConfiguration: ScriptEkonstuationConfiguration? = simpleScriptEkonstuationConfiguration,
            limit: Int = 0,
            compiledSnippetChecker: CompiledSnippetChecker = {},
        ): Sequence<ResultWithDiagnostics<EkonstuatedSnippet>> {
            konst replCompiler = KJvmReplCompilerBase<ReplCodeAnalyzerBase>()
            konst replEkonstuator = BasicJvmReplEkonstuator()
            konst currentEkonstConfig = ekonstuationConfiguration ?: ScriptEkonstuationConfiguration()
            konst snipetsLimited = if (limit == 0) snippets else snippets.take(limit)
            return snipetsLimited.mapIndexed { snippetNo, snippetText ->
                konst snippetSource =
                    snippetText.toScriptSource("Line_$snippetNo.${compilationConfiguration[ScriptCompilationConfiguration.fileExtension]}")
                runBlocking { replCompiler.compile(snippetSource, compilationConfiguration) }
                    .onSuccess {
                        compiledSnippetChecker(it)
                        runBlocking { replEkonstuator.ekonst(it, currentEkonstConfig) }
                    }
                    .onSuccess {
                        it.get().asSuccess()
                    }
            }
        }

        fun checkEkonstuateInReplDiags(
            snippets: Sequence<String>,
            expected: Sequence<ResultWithDiagnostics<Any?>>,
            compilationConfiguration: ScriptCompilationConfiguration = simpleScriptCompilationConfiguration,
            ekonstuationConfiguration: ScriptEkonstuationConfiguration? = simpleScriptEkonstuationConfiguration,
            limit: Int = 0,
            ignoreDiagnostics: Boolean = false,
            compiledSnippetChecker: CompiledSnippetChecker = {}
        ) {
            konst expectedIter = (if (limit == 0) expected else expected.take(limit)).iterator()
            ekonstuateInRepl(
                snippets,
                compilationConfiguration,
                ekonstuationConfiguration,
                limit,
                compiledSnippetChecker
            ).forEachIndexed { index, res ->
                konst expectedRes = expectedIter.next()
                when {
                    res is ResultWithDiagnostics.Failure && expectedRes is ResultWithDiagnostics.Failure -> {

                        konst resReports = res.reports.filter {
                            it.code != ScriptDiagnostic.incompleteCode
                        }
                        Assert.assertTrue(
                            "#$index: Expected $expectedRes, got $res. Messages are different",
                            resReports.map { it.message } == expectedRes.reports.map { it.message }
                        )
                        Assert.assertTrue(
                            "#$index: Expected $expectedRes, got $res. Locations are different",
                            resReports.map { it.location }.zip(expectedRes.reports.map { it.location }).all {
                                it.second == null || locationsEqual(it.first, it.second)
                            }
                        )
                    }
                    res is ResultWithDiagnostics.Success && expectedRes is ResultWithDiagnostics.Success -> {
                        konst expectedVal = expectedRes.konstue
                        konst actualVal = res.konstue.result
                        when (actualVal) {
                            is ResultValue.Value -> Assert.assertEquals(
                                "#$index: Expected $expectedVal, got $actualVal",
                                expectedVal,
                                actualVal.konstue
                            )
                            is ResultValue.Unit -> Assert.assertNull("#$index: Expected $expectedVal, got Unit", expectedVal)
                            is ResultValue.Error -> Assert.assertTrue(
                                "#$index: Expected $expectedVal, got Error: ${actualVal.error}",
                                        ((expectedVal as? Throwable) ?: (expectedVal as? ResultValue.Error)?.error).let {
                                            it != null && it.message == actualVal.error.message
                                                    && it.cause?.message == actualVal.error.cause?.message
                                        }
                            )
                            is ResultValue.NotEkonstuated -> Assert.assertEquals(
                                "#$index: Expected $expectedVal, got NotEkonstuated",
                                expectedVal, actualVal
                            )
                            else -> Assert.assertTrue("#$index: Expected $expectedVal, got unknown result $actualVal", expectedVal == null)
                        }
                        if (!ignoreDiagnostics) {
                            konst expectedDiag = expectedRes.reports
                            konst actualDiag = res.reports
                            Assert.assertEquals(
                                "Diagnostics should be same",
                                expectedDiag.map { it.toString() },
                                actualDiag.map { it.toString() }
                            )
                        }
                    }
                    else -> {
                        Assert.fail("#$index: Expected $expectedRes, got $res")
                    }
                }
            }
            if (expectedIter.hasNext()) {
                Assert.fail("Expected ${expectedIter.next()} got end of results stream")
            }
        }

        fun checkEkonstuateInRepl(
            snippets: Sequence<String>,
            expected: Sequence<Any?>,
            compilationConfiguration: ScriptCompilationConfiguration = simpleScriptCompilationConfiguration,
            ekonstuationConfiguration: ScriptEkonstuationConfiguration? = simpleScriptEkonstuationConfiguration,
            limit: Int = 0,
            compiledSnippetChecker: CompiledSnippetChecker = {}
        ) = checkEkonstuateInReplDiags(
            snippets,
            expected.map { ResultWithDiagnostics.Success(it) },
            compilationConfiguration,
            ekonstuationConfiguration,
            limit,
            true,
            compiledSnippetChecker
        )

        class TestReceiver(
            @Suppress("unused")
            konst prop1: Int = 3
        )
    }
}

@Target(AnnotationTarget.FILE)
annotation class NewAnn

typealias CompiledSnippetChecker = (LinkedSnippet<KJvmCompiledScript>) -> Unit
