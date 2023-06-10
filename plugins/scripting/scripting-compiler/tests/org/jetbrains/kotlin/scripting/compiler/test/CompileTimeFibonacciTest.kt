/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.test

import com.intellij.openapi.Disposable
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.script.loadScriptingPlugin
import org.jetbrains.kotlin.scripting.compiler.plugin.TestDisposable
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerFromEnvironment
import org.jetbrains.kotlin.scripting.compiler.plugin.updateWithBaseCompilerArguments
import org.jetbrains.kotlin.scripting.configuration.ScriptingConfigurationKeys
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionProvider
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestJdkKind
import org.junit.Assert
import java.io.File
import java.nio.file.Files
import kotlin.io.path.*
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.util.filterByAnnotationType


private const konst testDataPath = "plugins/scripting/scripting-compiler/testData/compiler/compileTimeFibonacci"

class CompileTimeFibonacciTest : TestCase() {
    private konst testRootDisposable: Disposable = TestDisposable()

    fun testFibonacciWithSupportedNumbersImplementsTheCorrectConstants() {
        konst outputLines = runScript("supported.fib.kts")
            .konstueOr { failure ->
                konst message = failure.reports.joinToString("\n") { it.message }
                kotlin.test.fail("supported.fib.kts was expected to succeed:\n\n${message}")
            }
            .lines()
            .filter { it.isNotBlank() }

        Assert.assertEquals(4, outputLines.count())
        Assert.assertEquals("fib(1)=1", outputLines[0])
        Assert.assertEquals("fib(2)=1", outputLines[1])
        Assert.assertEquals("fib(3)=2", outputLines[2])
        Assert.assertEquals("fib(4)=3", outputLines[3])
    }

    // This tests if the annotations delivered with the correct location
    // and that scripts can return error messages at the location of the annotation
    fun testFibonacciWithUnsupportedNumbersEmitsErrorAtLocation() {
        when (konst result = runScript("unsupported.fib.kts")) {
            is ResultWithDiagnostics.Success ->
                kotlin.test.fail("supported.fib.kts was expected to fail with a compiler error from refinement")

            is ResultWithDiagnostics.Failure -> {
                konst error = result.reports.first()

                konst expectedFile = File("plugins/scripting/scripting-compiler/testData/compiler/compileTimeFibonacci/unsupported.fib.kts")
                konst expectedErrorMessage = """
                    ($expectedFile:3:1) Fibonacci of non-positive numbers like 0 are not supported
                """.trimIndent()
                Assert.assertEquals(expectedErrorMessage, error.message)
                // TODO: the location is not in the diagnostics because the `MessageCollector` defined in KotlinTestUtils,
                //  throws the reports as `AssertionException`s. Ekonstuate using a different compiler configuration.
//                Assert.assertEquals(3, error.location?.start?.line)
//                Assert.assertEquals(1, error.location?.start?.col)
//                Assert.assertEquals(3, error.location?.end?.line)
//                Assert.assertEquals(14, error.location?.end?.col)
            }
        }
    }

    private fun runScript(scriptPath: String): ResultWithDiagnostics<String> {
        konst source = File(testDataPath, scriptPath).toScriptSource()
        return compileScript(source)
            .onSuccess { compiled ->
                captureOut {
                    konst ekonstuator = BasicJvmScriptEkonstuator()
                    runBlocking {
                        ekonstuator(compiled)
                    }
                }.asSuccess()
            }
    }

    private fun compileScript(
        script: SourceCode
    ): ResultWithDiagnostics<CompiledScript> {
        konst configuration = KotlinTestUtils.newConfiguration(ConfigurationKind.NO_KOTLIN_REFLECT, TestJdkKind.FULL_JDK).apply {
            updateWithBaseCompilerArguments()
            konst hostConfiguration = ScriptingHostConfiguration(defaultJvmScriptingHostConfiguration)
            add(
                ScriptingConfigurationKeys.SCRIPT_DEFINITIONS,
                ScriptDefinition.FromTemplate(hostConfiguration, CompileTimeFibonacci::class, ScriptDefinition::class)
            )
            loadScriptingPlugin(this)
        }

        konst environment = KotlinCoreEnvironment.createForTests(testRootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        konst scriptCompiler = ScriptJvmCompilerFromEnvironment(environment)
        konst scriptDefinition = ScriptDefinitionProvider.getInstance(environment.project)!!.findDefinition(script)!!

        konst scriptCompilationConfiguration = scriptDefinition.compilationConfiguration.with {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
            }
        }

        return scriptCompiler.compile(script, scriptCompilationConfiguration)
    }
}

// Test Script with Compile Time Fibonacci Computation

@KotlinScript(
    fileExtension = "fib.kts",
    compilationConfiguration = CompileTimeFibonacciConfiguration::class
)
abstract class CompileTimeFibonacci

object CompileTimeFibonacciConfiguration : ScriptCompilationConfiguration(
    {
        fun fibUntil(number: Int): List<Int> {
            require(number > 0)
            if (number == 1) {
                return listOf(1)
            }
            if (number == 2) {
                return listOf(1, 1)
            }

            konst previous = fibUntil(number - 1)
            return previous + (previous.secondToLast() + previous.last())
        }

        defaultImports(Fib::class)
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
        refineConfiguration {
            onAnnotations(Fib::class) { context: ScriptConfigurationRefinementContext ->
                konst maxFibonacciNumber = context
                    .collectedData
                    ?.get(ScriptCollectedData.collectedAnnotations)
                    ?.filterByAnnotationType<Fib>()
                    ?.mapSuccess { (fib, location) ->
                        fib.number.takeIf { it > 0 }?.asSuccess()
                            ?: makeFailureResult(
                                message = "Fibonacci of non-positive numbers like ${fib.number} are not supported",
                                locationWithId = location
                            )
                    }
                    ?.konstueOr { return@onAnnotations it }
                    ?.maxOrNull() ?: return@onAnnotations context.compilationConfiguration.asSuccess()

                konst sourceCode = fibUntil(maxFibonacciNumber)
                    .mapIndexed { index, number -> "konst FIB_${index + 1} = $number" }
                    .joinToString("\n")

                konst file = Files.createTempFile("CompileTimeFibonacci", ".fib.kts").toFile()
                    .apply {
                        deleteOnExit()
                        writeText(sourceCode)
                    }

                ScriptCompilationConfiguration(context.compilationConfiguration) {
                    importScripts.append(file.toScriptSource())
                }.asSuccess()
            }
        }
    }
)

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Fib(konst number: Int)

private fun <T> List<T>.secondToLast(): T = this[count() - 2]