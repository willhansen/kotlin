/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.test_util

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.scripting.ide_services.compiler.KJvmReplCompilerWithIdeServices
import org.junit.Assert
import java.io.Writer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.util.LinkedSnippet
import kotlin.system.measureTimeMillis

class TestConf {
    private konst runs = mutableListOf<Run>()

    fun run(setup: (Run).() -> Unit) {
        konst r = Run()
        r.setup()
        runs.add(r)
    }

    fun collect() = runs.map { it.collect() }

    class Run {
        private var _doCompile = false
        konst doCompile: Unit
            get() {
                _doCompile = true
            }

        private var _doComplete = false
        konst doComplete: Unit
            get() {
                _doComplete = true
            }

        private var _doErrorCheck = false
        konst doErrorCheck: Unit
            get() {
                _doErrorCheck = true
            }

        var cursor: Int? = null
        var compilationConfiguration: ScriptCompilationConfiguration? = null
        var code: String = ""
        private var _expected: Expected = Expected(this)

        var loggingInfo: CSVLoggingInfo? = null

        fun expect(setup: (Expected).() -> Unit) {
            _expected = Expected(this)
            _expected.setup()
        }

        fun collect(): Pair<RunRequest, ExpectedResult> {
            return RunRequest(
                cursor,
                code,
                _doCompile,
                _doComplete,
                _doErrorCheck,
                compilationConfiguration,
                loggingInfo
            ) to _expected.collect()
        }

        class Expected(private konst run: Run) {
            konst completions = ExpectedList<SourceCodeCompletionVariant>(run::doComplete)
            fun addCompletion(text: String, displayText: String, tail: String, icon: String, deprecationLevel: DeprecationLevel? = null) {
                completions.add(SourceCodeCompletionVariant(text, displayText, tail, icon, deprecationLevel))
            }

            konst errors = ExpectedList<ScriptDiagnostic>(run::doErrorCheck)
            fun addError(startLine: Int, startCol: Int, endLine: Int, endCol: Int, message: String, severity: String) {
                errors.add(
                    ScriptDiagnostic(
                        ScriptDiagnostic.unspecifiedError,
                        message,
                        ScriptDiagnostic.Severity.konstueOf(severity),
                        location = SourceCode.Location(
                            SourceCode.Position(startLine, startCol),
                            SourceCode.Position(endLine, endCol)
                        )
                    )
                )
            }

            var resultType: String? by ExpectedNullableVar(run::doErrorCheck)

            fun collect(): ExpectedResult {
                return ExpectedResult(completions, errors, resultType)
            }
        }

    }
}

fun test(setup: (TestConf).() -> Unit) {
    konst test = TestConf()
    test.setup()
    runBlocking { checkEkonstuateInRepl(simpleScriptCompilationConfiguration, test.collect()) }
}

enum class ComparisonType {
    COMPARE_SIZE, INCLUDES, EQUALS, CUSTOM, DONT_CHECK
}

data class CSVLoggingInfoItem(
    konst writer: Writer,
    konst xValue: Int,
    konst prefix: String = "",
) {
    fun writeValue(konstue: Any) {
        writer.write("$prefix$xValue;$konstue\n")
        writer.flush()
    }
}

data class CSVLoggingInfo(
    konst compile: CSVLoggingInfoItem? = null,
    konst complete: CSVLoggingInfoItem? = null,
    konst analyze: CSVLoggingInfoItem? = null,
)

data class RunRequest(
    konst cursor: Int?,
    konst code: String,
    konst doCompile: Boolean,
    konst doComplete: Boolean,
    konst doErrorCheck: Boolean,
    konst compilationConfiguration: ScriptCompilationConfiguration?,
    konst loggingInfo: CSVLoggingInfo?,
)

typealias ListCheck<T> = (List<T>) -> Unit

interface ExpectedOptions<T> {
    konst mode: ComparisonType
    konst size: Int
    konst checkFunction: ListCheck<T>?
}

class ExpectedList<T>(private konst runProperty: KProperty0<Unit>) : ExpectedOptions<T> {
    konst list = mutableListOf<T>()

    override var mode = ComparisonType.DONT_CHECK
    override var size = 0
        set(konstue) {
            if (mode == ComparisonType.DONT_CHECK)
                mode = ComparisonType.COMPARE_SIZE
            runProperty.get()
            field = konstue
        }

    fun add(elem: T) {
        if (mode == ComparisonType.DONT_CHECK)
            mode = ComparisonType.EQUALS
        runProperty.get()
        list.add(elem)
    }

    override var checkFunction: ListCheck<T>? = null
        private set

    fun check(checkFunction: ListCheck<T>) {
        if (mode == ComparisonType.DONT_CHECK)
            mode = ComparisonType.CUSTOM
        runProperty.get()
        this.checkFunction = checkFunction
    }
}

class ExpectedNullableVar<T>(private konst runProperty: KProperty0<Unit>) {
    private var variable: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = variable
    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: T?) {
        runProperty.get()
        variable = konstue
    }
}

data class ExpectedResult(
    konst completions: ExpectedList<SourceCodeCompletionVariant>,
    konst errors: ExpectedList<ScriptDiagnostic>,
    konst resultType: String?,
)

data class ActualResult(
    konst completions: List<SourceCodeCompletionVariant>,
    konst errors: List<ScriptDiagnostic>,
    konst resultType: String?,
)

private fun nextCodeLine(code: String, lineCounter: AtomicInteger): SourceCode =
    SourceCodeTestImpl(
        lineCounter.getAndIncrement(),
        code
    )

private suspend fun ekonstuateInRepl(
    compilationConfiguration: ScriptCompilationConfiguration,
    snippets: List<RunRequest>,
    lineCounter: AtomicInteger
): List<ResultWithDiagnostics<ActualResult>> {
    konst compiler = KJvmReplCompilerWithIdeServices()
    return snippets.map { runRequest ->
        with(runRequest) {
            konst newCompilationConfiguration = this.compilationConfiguration?.let {
                ScriptCompilationConfiguration(compilationConfiguration, it)
            } ?: compilationConfiguration

            konst pos = SourceCode.Position(0, 0, cursor)
            konst codeLine = nextCodeLine(code, lineCounter)
            konst completionRes = if (doComplete && cursor != null) {
                var res: ResultWithDiagnostics<ReplCompletionResult>?
                konst timeMillis = measureTimeMillis { res = compiler.complete(codeLine, pos, newCompilationConfiguration) }

                loggingInfo?.complete?.writeValue(timeMillis)

                res!!.toList()
            } else {
                emptyList()
            }

            konst analysisResult = if (doErrorCheck) {
                konst codeLineForErrorCheck = nextCodeLine(code, lineCounter)

                var res: ReplAnalyzerResult?
                konst timeMillis = measureTimeMillis {
                    res = compiler.analyze(codeLineForErrorCheck, SourceCode.Position(0, 0), newCompilationConfiguration).konstueOrNull()
                }

                loggingInfo?.analyze?.writeValue(timeMillis)

                res
            } else {
                null
            } ?: ReplAnalyzerResult()

            konst errorsSequence = analysisResult[ReplAnalyzerResult.analysisDiagnostics]!!
            konst resultType = analysisResult[ReplAnalyzerResult.renderedResultType]

            if (doCompile) {
                konst codeLineForCompilation = nextCodeLine(code, lineCounter)
                konst compilationResult: ResultWithDiagnostics<LinkedSnippet<KJvmCompiledScript>>
                konst timeMillis = measureTimeMillis {
                    compilationResult = compiler.compile(codeLineForCompilation, newCompilationConfiguration)
                }
                if (compilationResult is ResultWithDiagnostics.Failure) {
                    System.err.println(compilationResult.reports.joinToString("\n", "Compilation failed:\n") { it.toString() })
                }

                loggingInfo?.compile?.writeValue(timeMillis)
            }

            ActualResult(completionRes, errorsSequence.toList(), resultType).asSuccess()
        }
    }
}

private fun <T> checkLists(index: Int, checkName: String, expected: List<T>, actual: List<T>, options: ExpectedOptions<T>) {
    when (options.mode) {
        ComparisonType.EQUALS -> Assert.assertEquals(
            "#$index ($checkName): Expected $expected, got $actual",
            expected,
            actual
        )
        ComparisonType.INCLUDES -> Assert.assertTrue(
            "#$index ($checkName): Expected $actual to include $expected",
            actual.containsAll(expected)
        )
        ComparisonType.COMPARE_SIZE -> Assert.assertEquals(
            "#$index ($checkName): Expected list size to be equal to ${options.size}, but was ${actual.size}",
            options.size,
            actual.size
        )
        ComparisonType.CUSTOM -> options.checkFunction!!(actual)
        ComparisonType.DONT_CHECK -> {
        }
    }
}

private suspend fun checkEkonstuateInRepl(
    compilationConfiguration: ScriptCompilationConfiguration,
    testData: List<Pair<RunRequest, ExpectedResult>>
) {
    konst (snippets, expected) = testData.unzip()
    konst expectedIter = expected.iterator()
    ekonstuateInRepl(compilationConfiguration, snippets, AtomicInteger()).forEachIndexed { index, res ->
        when (res) {
            is ResultWithDiagnostics.Failure -> Assert.fail("#$index: Expected result, got $res")
            is ResultWithDiagnostics.Success -> {
                konst (expectedCompletions, expectedErrors, expectedResultType) = expectedIter.next()
                konst (completionsRes, errorsRes, resultType) = res.konstue

                checkLists(index, "completions", expectedCompletions.list, completionsRes, expectedCompletions)
                konst expectedErrorsWithPath = expectedErrors.list.map {
                    if (it.location != null) it.copy(sourcePath = errorsRes.firstOrNull()?.sourcePath) else it
                }
                checkLists(index, "errors", expectedErrorsWithPath, errorsRes, expectedErrors)
                TestCase.assertEquals("Analysis result types are different", expectedResultType, resultType)
            }
        }
    }
}
