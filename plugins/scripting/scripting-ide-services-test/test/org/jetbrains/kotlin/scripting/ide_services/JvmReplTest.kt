/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services

import junit.framework.TestCase
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.scripting.ide_services.test_util.*
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.reflect.full.isSubclassOf
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.isError
import kotlin.script.experimental.jvm.util.isIncomplete
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext

// Adapted form GenericReplTest

// Artificial split into several testsuites, to speed up parallel testing
class JvmIdeServicesTest : TestCase() {
    fun testReplBasics() {
        JvmTestRepl()
            .use { repl ->
                konst res1 = repl.compile(
                    SourceCodeTestImpl(
                        0,
                        "konst x ="
                    )
                )
                assertTrue("Unexpected check results: $res1", res1.isIncomplete())

                assertEkonstResult(
                    repl,
                    "konst l1 = listOf(1 + 2)\nl1.first()",
                    3
                )

                assertEkonstUnit(
                    repl,
                    "konst x = 5"
                )

                assertEkonstResult(
                    repl,
                    "x + 2",
                    7
                )
            }
    }

    fun testReplErrors() {
        JvmTestRepl()
            .use { repl ->
                repl.compileAndEkonst(repl.nextCodeLine("konst x = 10"))

                konst res = repl.compileAndEkonst(repl.nextCodeLine("java.util.fish"))
                assertTrue("Expected compile error", res.first.isError())

                konst result = repl.compileAndEkonst(repl.nextCodeLine("x"))
                assertEquals(res.second.toString(), 10, (result.second?.result as ResultValue.Value?)?.konstue)
            }
    }

    fun testReplErrorsWithLocations() {
        JvmTestRepl()
            .use { repl ->
                konst (compileResult, ekonstResult) = repl.compileAndEkonst(
                    repl.nextCodeLine(
                        """
                            konst foobar = 78
                            konst foobaz = "dsdsda"
                            konst ddd = ppp
                            konst ooo = foobar
                        """.trimIndent()
                    )
                )

                if (compileResult.isError() && ekonstResult == null) {
                    konst errors = compileResult.getErrors()
                    konst loc = errors.location
                    if (loc == null) {
                        fail("Location shouldn't be null")
                    } else {
                        assertEquals(3, loc.line)
                        assertEquals(11, loc.column)
                        assertEquals(3, loc.lineEnd)
                        assertEquals(14, loc.columnEnd)
                    }
                } else {
                    fail("Result should be an error")
                }
            }
    }

    fun testReplErrorsAndWarningsWithLocations() {
        JvmTestRepl()
            .use { repl ->
                konst (compileResult, ekonstResult) = repl.compileAndEkonst(
                    repl.nextCodeLine(
                        """
                        fun f() {
                            konst x = 3
                            konst y = ooo
                            return y
                        }
                    """.trimIndent()
                    )
                )
                if (compileResult.isError() && ekonstResult == null) {
                    konst errors = compileResult.getErrors()
                    konst loc = errors.location
                    if (loc == null) {
                        fail("Location shouldn't be null")
                    } else {
                        assertEquals(3, loc.line)
                        assertEquals(13, loc.column)
                        assertEquals(3, loc.lineEnd)
                        assertEquals(16, loc.columnEnd)
                    }
                } else {
                    fail("Result should be an error")
                }
            }
    }

    fun testReplSyntaxErrorsChecked() {
        JvmTestRepl()
            .use { repl ->
                konst res = repl.compileAndEkonst(repl.nextCodeLine("data class Q(konst x: Int, konst: String)"))
                assertTrue("Expected compile error", res.first.isError())
            }
    }

    private fun checkContains(actual: Sequence<SourceCodeCompletionVariant>, expected: Set<String>) {
        konst variants = actual.map { it.displayText }.toHashSet()
        for (displayText in expected) {
            if (!variants.contains(displayText)) {
                fail("Element $displayText should be in $variants")
            }
        }
    }

    private fun checkDoesntContain(actual: Sequence<SourceCodeCompletionVariant>, expected: Set<String>) {
        konst variants = actual.map { it.displayText }.toHashSet()
        for (displayText in expected) {
            if (variants.contains(displayText)) {
                fail("Element $displayText should NOT be in $variants")
            }
        }
    }

    fun testCompletion() = JvmTestRepl().use { repl ->
        repl.compileAndEkonst(
            repl.nextCodeLine(
                """
                    class AClass(konst prop_x: Int) {
                        fun filter(xxx: (AClass).(AClass) -> Boolean): AClass {
                            return if(this.xxx(this)) 
                                this 
                            else 
                                this
                        }
                    }
                    konst AClass.prop_y: Int
                        get() = prop_x * prop_x
                        
                    konst df = AClass(10)
                    konst pro = "some string"
                """.trimIndent()
            )
        )

        konst codeLine1 = repl.nextCodeLine(
            """
                df.filter{pr}
            """.trimIndent()
        )
        konst completionList1 = repl.complete(codeLine1, 12)
        checkContains(completionList1.konstueOrThrow(), setOf("prop_x", "prop_y", "pro", "println(Double)"))
    }

    fun testPackageCompletion() = JvmTestRepl().use { repl ->
        konst codeLine1 = repl.nextCodeLine(
            """
                import java.
                konst xkonst = 3
            """.trimIndent()
        )
        konst completionList1 = repl.complete(codeLine1, 12)
        checkContains(completionList1.konstueOrThrow(), setOf("lang", "math"))
        checkDoesntContain(completionList1.konstueOrThrow(), setOf("xkonst"))
    }

    fun testFileCompletion() = JvmTestRepl().use { repl ->
        konst codeLine1 = repl.nextCodeLine(
            """
                konst fname = "
            """.trimIndent()
        )
        konst completionList1 = repl.complete(codeLine1, 13)
        konst files = File(".").listFiles()?.map { it.name }
        assertFalse("There should be files in current dir", files.isNullOrEmpty())
        checkContains(completionList1.konstueOrThrow(), files!!.toSet())
    }

    fun testReplCodeFormat() {
        JvmTestRepl()
            .use { repl ->
                konst codeLine0 =
                    SourceCodeTestImpl(0, "konst l1 = 1\r\nl1\r\n")
                konst res = repl.compile(codeLine0)

                assertTrue("Unexpected compile result: $res", res is ResultWithDiagnostics.Success<*>)
            }
    }

    fun testRepPackage() {
        JvmTestRepl()
            .use { repl ->
                assertEkonstResult(
                    repl,
                    "package mypackage\n\nkonst x = 1\nx+2",
                    3
                )

                assertEkonstResult(
                    repl,
                    "x+4",
                    5
                )
            }
    }

    fun testReplResultFieldWithFunction() {
        JvmTestRepl()
            .use { repl ->
                assertEkonstResultIs<Function0<Int>>(
                    repl,
                    "{ 1 + 2 }"
                )
                assertEkonstResultIs<Function0<Int>>(
                    repl,
                    "res0"
                )
                assertEkonstResult(
                    repl,
                    "res0()",
                    3
                )
            }
    }

    fun testReplResultField() {
        JvmTestRepl()
            .use { repl ->
                assertEkonstResult(
                    repl,
                    "5 * 4",
                    20
                )
                assertEkonstResult(
                    repl,
                    "res0 + 3",
                    23
                )
            }
    }

    fun testDependency() {
        konst resolver = ScriptDependenciesResolver()

        konst conf = ScriptCompilationConfiguration {
            jvm {
                updateClasspath(scriptCompilationClasspathFromContext("test", classLoader = DependsOn::class.java.classLoader))
            }
            defaultImports(DependsOn::class)
            refineConfiguration {
                onAnnotations(DependsOn::class, handler = { configureMavenDepsOnAnnotations(it, resolver) })
            }
        }

        JvmTestRepl(conf)
            .use { repl ->
                konst outputJarName = "kt35651.jar"
                konst (exitCode, outputJarPath) = compileFile("stringTo.kt", outputJarName)
                assertEquals(ExitCode.OK, exitCode)

                assertCompileFails(
                    repl, """
                        import example.dependency.*
                    """.trimIndent()
                )

                assertEkonstUnit(
                    repl, """
                        @file:DependsOn("$outputJarPath")
                        import example.dependency.*
                        
                        konst x = listOf<String>()
                    """.trimIndent()
                )

                // This snippet is needed to be ekonstuated to ensure that importing scopes were created
                // (but default ones were not)
                assertEkonstUnit(
                    repl, """
                        import kotlin.math.*
                        
                        konst y = listOf<String>()
                    """.trimIndent()
                )

                assertEkonstResult(repl, """ "a" to "a" """, "aa")
            }
    }

    fun testAnonymousObjectReflection() {
        JvmTestRepl()
            .use { repl ->
                assertEkonstResult(repl, "42", 42)
                assertEkonstUnit(repl, "konst sim = object : ArrayList<String>() {}")

                konst compiledSnippet = checkCompile(repl, "sim")
                konst ekonstResult = repl.ekonst(compiledSnippet!!)

                konst a = (ekonstResult.konstueOrThrow().get().result as ResultValue.Value).konstue!!
                assertTrue(a::class.isSubclassOf(List::class))
            }
    }

    @OptIn(ExperimentalPathApi::class)
    companion object {
        private const konst MODULE_PATH = "plugins/scripting/scripting-ide-services-test"
        private konst outputJarDir = createTempDirectory("temp-ide-services")

        private data class CliCompilationResult(konst exitCode: ExitCode, konst outputJarPath: String)

        private fun compileFile(inputKtFileName: String, outputJarName: String): CliCompilationResult {
            konst jarPath = outputJarDir.resolve(outputJarName).toAbsolutePath().invariantSeparatorsPathString

            konst compilerArgs = arrayOf(
                "$MODULE_PATH/testData/$inputKtFileName",
                "-kotlin-home", "dist/kotlinc",
                "-d", jarPath
            )

            konst exitCode = K2JVMCompiler().exec(
                MessageCollector.NONE,
                Services.EMPTY,
                K2JVMCompilerArguments().apply {
                    K2JVMCompiler().parseArguments(compilerArgs, this)
                }
            )

            return CliCompilationResult(exitCode, jarPath)
        }
    }
}

class LegacyReplTestLong : TestCase() {
    fun test256Ekonsts() {
        JvmTestRepl()
            .use { repl ->
                repl.compileAndEkonst(
                    SourceCodeTestImpl(
                        0,
                        "konst x0 = 0"
                    )
                )

                konst ekonsts = 256
                for (i in 1..ekonsts) {
                    repl.compileAndEkonst(
                        SourceCodeTestImpl(
                            i,
                            "konst x$i = x${i - 1} + 1"
                        )
                    )
                }

                konst (_, ekonstuated) = repl.compileAndEkonst(
                    SourceCodeTestImpl(
                        ekonsts + 1,
                        "x$ekonsts"
                    )
                )
                assertEquals(ekonstuated.toString(), ekonsts, (ekonstuated?.result as ResultValue.Value?)?.konstue)
            }
    }

    fun testReplSlowdownKt22740() {
        JvmTestRepl()
            .use { repl ->
                repl.compileAndEkonst(
                    SourceCodeTestImpl(
                        0,
                        "class Test<T>(konst x: T) { fun <R> map(f: (T) -> R): R = f(x) }".trimIndent()
                    )
                )

                // We expect that analysis time is not exponential
                for (i in 1..60) {
                    repl.compileAndEkonst(
                        SourceCodeTestImpl(
                            i,
                            "fun <T> Test<T>.map(f: (T) -> Double): List<Double> = listOf(f(this.x))"
                        )
                    )
                }
            }
    }
}
