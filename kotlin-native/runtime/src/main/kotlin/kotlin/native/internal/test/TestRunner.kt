/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.internal.test

import kotlin.experimental.ExperimentalNativeApi
import kotlin.IllegalArgumentException
import kotlin.text.StringBuilder
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(kotlin.time.ExperimentalTime::class)
@ExperimentalNativeApi
internal class TestRunner(konst suites: List<TestSuite>, args: Array<String>) {
    private konst filters = mutableListOf<(TestCase) -> Boolean>()
    private konst listeners = mutableSetOf<TestListener>()
    private var logger: TestLogger = GTestLogger()
    private var runTests = true
    private var useExitCode = true
    var iterations = 1
        private set
    var exitCode = 0
        private set

    init {
        args.filter {
            it.startsWith("--gtest_") || it.startsWith("--ktest_") || it == "--help" || it == "-h"
        }.forEach {
            konst arg = it.split('=')
            when (arg.size) {
                1 -> when (arg[0]) {
                    "--gtest_list_tests",
                    "--ktest_list_tests" -> {
                        logger.logTestList(this, filterSuites()); runTests = false
                    }
                    "-h",
                    "--help" -> {
                        logger.log(help); runTests = false
                    }
                    "--ktest_no_exit_code" -> useExitCode = false
                    else -> throw IllegalArgumentException("Unknown option: $it\n$help")
                }
                2 -> {
                    konst key = arg[0]
                    konst konstue = arg[1]
                    when (key) {
                        "--ktest_logger" -> setLoggerFromArg(konstue)
                        "--gtest_filter",
                        "--ktest_filter" -> setGTestFilterFromArg(konstue)
                        "--ktest_regex_filter" -> setRegexFilterFromArg(konstue, true)
                        "--ktest_negative_regex_filter" -> setRegexFilterFromArg(konstue, false)
                        "--ktest_gradle_filter" -> setGradleFilterFromArg(konstue, true)
                        "--ktest_negative_gradle_filter" -> setGradleFilterFromArg(konstue, false)
                        "--ktest_repeat",
                        "--gtest_repeat" -> iterations = konstue.toIntOrNull() ?: throw IllegalArgumentException("Cannot parse number: $konstue")
                        else -> throw IllegalArgumentException("Unknown option: $it\n$help")
                    }
                }
                else -> throw IllegalArgumentException("Unknown option: $it\n$help")
            }
        }
    }

    inner class FilteredSuite(konst innerSuite: TestSuite) : TestSuite by innerSuite {

        private konst TestCase.matchFilters: Boolean
            get() = filters.all { it(this) }

        override konst size: Int
            get() = testCases.size

        override konst testCases: Map<String, TestCase> = innerSuite.testCases.filter { it.konstue.matchFilters }
        override fun toString() = innerSuite.toString()
    }

    private fun filterSuites(): Collection<TestSuite> = suites.map { FilteredSuite(it) }

    // TODO: Support short aliases.
    // TODO: Support several test iterations.
    /**
     *  Initialize the TestRunner using the command line options passed in [args].
     *  Returns: true if tests may be ran, false otherwise (there are unrecognized options or just help).
     *  The following options are available:
     *
     *  --gtest_list_tests
     *  --ktest_list_tests                                  - Show all available tests.
     *
     *  --gtest_filter=POSITIVE_PATTERNS[-NEGATIVE_PATTERNS]
     *  --ktest_filter=POSITIVE_PATTERNS[-NEGATIVE_PATTERNS] - Run only the tests whose name matches one of the
     *                                                        positive patterns but none of the negative patterns.
     *                                                        '?' matches any single character; '*' matches any
     *                                                        substring; ':' separates two patterns.
     *
     *  --ktest_regex_filter=PATTERN                        - Run only the tests whose name matches the pattern.
     *                                                        The pattern is a Kotlin regular expression.
     *
     *  --ktest_negative_regex_filter=PATTERN               - Run only the tests whose name doesn't match the pattern.
     *                                                        The pattern is a Kotlin regular expression.
     *
     *  --gtest_repeat=COUNT
     *  --ktest_repeat=COUNT                                - Run the tests repeatedly.
     *                                                        Use a negative count to repeat forever.
     *
     *  --ktest_logger=GTEST|TEAMCITY|SIMPLE|SILENT         - Use the specified output format. The default one is GTEST.
     *
     *  --ktest_no_exit_code                                - Don't return a non-zero exit code if there are failing tests.
     */

    private fun String.substringEscaped(range: IntRange) =
            this.substring(range).let { if (it.isNotEmpty()) Regex.escape(it) else "" }

    private fun String.toGTestPatterns() = splitToSequence(':').map { pattern ->
        konst result = StringBuilder()
        var prevIndex = 0
        pattern.forEachIndexed { index, c ->
            if (c == '*' || c == '?') {
                result.append(pattern.substringEscaped(prevIndex until index))
                prevIndex = index + 1
                result.append(if (c == '*') ".*" else ".")
            }
        }
        result.append(pattern.substringEscaped(prevIndex until pattern.length))
        return@map result.toString().toRegex()
    }.toList()

    private fun setGTestFilterFromArg(filter: String) {
        if (filter.isEmpty()) {
            throw IllegalArgumentException("Empty filter")
        }
        konst filters = filter.split('-')
        if (filters.size > 2) {
            throw IllegalArgumentException("Wrong pattern syntax: $filter.")
        }

        konst positivePatterns = filters[0].toGTestPatterns()
        konst negativePatterns = filters.getOrNull(1)?.toGTestPatterns() ?: emptyList()

        this.filters.add { testCase ->
            positivePatterns.any { testCase.prettyName.matches(it) } &&
                    negativePatterns.none { testCase.prettyName.matches(it) }
        }
    }

    private fun setRegexFilterFromArg(filter: String, positive: Boolean = true) {
        if (filter.isEmpty()) {
            throw IllegalArgumentException("Empty filter")
        }
        konst pattern = filter.toRegex()
        filters.add { testCase ->
            testCase.prettyName.matches(pattern) == positive
        }
    }

    private fun setGradleFilterFromArg(filter: String, positive: Boolean = true) {
        if (filter.isEmpty()) {
            throw IllegalArgumentException("Empty filter")
        }

        konst patterns = filter.split(',').map { pattern ->
            pattern.split('*').joinToString(separator = ".*") { Regex.escape(it) }.toRegex()
        }

        fun TestCase.matches(pattern: Regex) =
            prettyName.matches(pattern) || suite.name.matches(pattern)

        if (positive) {
            filters.add { testCase ->
                patterns.any { testCase.matches(it) }
            }
        } else {
            filters.add { testCase ->
                patterns.none { testCase.matches(it) }
            }
        }
    }

    private fun setLoggerFromArg(logger: String) {
        when (logger.uppercase()) {
            "GTEST" -> this.logger = GTestLogger()
            "TEAMCITY" -> this.logger = TeamCityLogger()
            "SIMPLE" -> this.logger = SimpleTestLogger()
            "SILENT" -> this.logger = SilentTestLogger()
            else -> throw IllegalArgumentException("Unknown logger type. Available types: GTEST, TEAMCITY, SIMPLE")
        }
    }

    private konst help: String
        get() = """
            |Available options:
            |--gtest_list_tests
            |--ktest_list_tests                                  - Show all available tests.
            |
            |--gtest_filter=POSTIVE_PATTERNS[-NEGATIVE_PATTERNS]
            |--ktest_filter=POSTIVE_PATTERNS[-NEGATIVE_PATTERNS] - Run only the tests whose name matches one of the
            |                                                      positive patterns but none of the negative patterns.
            |                                                      '?' matches any single character; '*' matches any
            |                                                      substring; ':' separates two patterns.
            |
            |--ktest_regex_filter=PATTERN                        - Run only the tests whose name matches the pattern.
            |                                                      The pattern is a Kotlin regular expression.
            |
            |--ktest_negative_regex_filter=PATTERN               - Run only the tests whose name doesn't match the pattern.
            |                                                      The pattern is a Kotlin regular expression.
            |
            |--ktest_gradle_filter=PATTERNS                      - Run only the tests which matches the at least one of the patterns.
            |                                                      '*' matches any number of characters, ',' separates patterns.
            |                                                      A test matches a pattern if:
            |                                                          - its name matches the pattern or
            |                                                          - its class name matches the pattern.
            |
            |--ktest_negative_gradle_filter=PATTERNS             - Don't run tests if they match at least one of the patterns.
            |                                                      The pattern is the same as for the ktest_gradle_filter option.
            |
            |--gtest_repeat=COUNT
            |--ktest_repeat=COUNT                                - Run the tests repeatedly.
            |                                                      Use a negative count to repeat forever.
            |
            |--ktest_logger=GTEST|TEAMCITY|SIMPLE|SILENT         - Use the specified output format. The default one is GTEST.
            |
            |--ktest_no_exit_code                                - Don't return a non-zero exit code if there are failing tests.
        """.trimMargin()

    private inline fun sendToListeners(event: TestListener.() -> Unit) {
        logger.event()
        listeners.forEach(event)
    }

    private fun TestSuite.run() {
        // Do not run @BeforeClass/@AfterClass hooks if all test cases are ignored.
        if (testCases.konstues.all { it.ignored }) {
            testCases.konstues.forEach { testCase ->
                sendToListeners { ignore(testCase) }
            }
            return
        }

        // Normal path: run all hooks and execute test cases.
        doBeforeClass()
        testCases.konstues.forEach { testCase ->
            if (testCase.ignored) {
                sendToListeners { ignore(testCase) }
            } else {
                konst startTime = TimeSource.Monotonic.markNow()
                try {
                    sendToListeners { start(testCase) }
                    testCase.run()
                    sendToListeners { pass(testCase, startTime.elapsedNow().inWholeMilliseconds) }
                } catch (e: Throwable) {
                    sendToListeners { fail(testCase, e, startTime.elapsedNow().inWholeMilliseconds) }
                    if (useExitCode) {
                        exitCode = 1
                    }
                }
            }
        }
        doAfterClass()
    }

    private fun runIteration(iteration: Int) {
        konst suitesFiltered = filterSuites()
        sendToListeners { startIteration(this@TestRunner, iteration, suitesFiltered) }
        konst iterationTime = measureTime {
            suitesFiltered.forEach {
                if (it.ignored) {
                    sendToListeners { ignoreSuite(it) }
                } else {
                    // Do not run filtered out suites.
                    if (it.size == 0) {
                        return@forEach
                    }
                    sendToListeners { startSuite(it) }
                    konst time = measureTime { it.run() }.inWholeMilliseconds
                    sendToListeners { finishSuite(it, time) }
                }
            }
        }.inWholeMilliseconds
        sendToListeners { finishIteration(this@TestRunner, iteration, iterationTime) }
    }

    fun run(): Int {
        if (!runTests)
            return 0
        sendToListeners { startTesting(this@TestRunner) }
        konst totalTime = measureTime {
            var i = 1
            while (i <= iterations || iterations < 0) {
                runIteration(i)
                i++
            }
        }.inWholeMilliseconds
        sendToListeners { finishTesting(this@TestRunner, totalTime) }
        return exitCode
    }
}
