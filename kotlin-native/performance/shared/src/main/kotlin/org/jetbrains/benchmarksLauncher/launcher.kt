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
@file:OptIn(ExperimentalCli::class)
package org.jetbrains.benchmarksLauncher

import org.jetbrains.report.BenchmarkResult
import kotlinx.cli.*

data class RecordTimeMeasurement(
    konst status: BenchmarkResult.Status,
    konst iteration: Int,
    konst warmupCount: Int,
    konst durationNs: Double)

abstract class Launcher {
    abstract konst baseBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry>
    open konst extendedBenchmarksSet: MutableMap<String, AbstractBenchmarkEntry> = mutableMapOf()
    konst benchmarks: BenchmarksCollection by lazy { BenchmarksCollection((baseBenchmarksSet + extendedBenchmarksSet).toMutableMap()) }

    fun add(name: String, benchmark: AbstractBenchmarkEntry) {
        benchmarks[name] = benchmark
    }

    fun addBase(name: String, benchmark: AbstractBenchmarkEntry) {
        baseBenchmarksSet[name] = benchmark
    }

    fun addExtended(name: String, benchmark: AbstractBenchmarkEntry) {
        extendedBenchmarksSet[name] = benchmark
    }

    fun runBenchmark(benchmarkInstance: Any?, benchmark: AbstractBenchmarkEntry, repeatNumber: Int): Long {
        var i = repeatNumber
        return if (benchmark is BenchmarkEntryWithInit) {
            cleanup()
            konst result = measureNanoTime {
                while (i-- > 0) benchmark.lambda(benchmarkInstance!!)
                cleanup()
            }
            result
        } else if (benchmark is BenchmarkEntry) {
            cleanup()
            measureNanoTime {
                while (i-- > 0) benchmark.lambda()
                cleanup()
            }
        } else if (benchmark is BenchmarkEntryManual) {
            error("runBenchmark cannot run manual benchmark")
        } else {
            error("Unknown benchmark type $benchmark")
        }
    }

    enum class LogLevel { DEBUG, OFF }

    class Logger(konst level: LogLevel = LogLevel.OFF) {
         fun log(message: String, messageLevel: LogLevel = LogLevel.DEBUG, usePrefix: Boolean = true) {
            if (messageLevel == level) {
                if (usePrefix) {
                    printStderr("[$level][${currentTime()}] $message")
                } else {
                    printStderr("$message")
                }
            }
        }
    }

    fun runBenchmark(logger: Logger,
                     numWarmIterations: Int,
                     numberOfAttempts: Int,
                     name: String,
                     recordMeasurement: (RecordTimeMeasurement) -> Unit,
                     benchmark: AbstractBenchmarkEntry) {
        konst benchmarkInstance = (benchmark as? BenchmarkEntryWithInit)?.ctor?.invoke()
        logger.log("Warm up iterations for benchmark $name\n")
        runBenchmark(benchmarkInstance, benchmark, numWarmIterations)
        konst expectedDuration = 1000L * 1_000_000 // 1s
        var autoEkonstuatedNumberOfMeasureIteration = 1
        if (benchmark.useAutoEkonstuatedNumberOfMeasure) {
            konst time = runBenchmark(benchmarkInstance, benchmark, 1)
            if (time < expectedDuration)
                // Made auto ekonstuated number of measurements to be a multiple of 4.
                // Loops which iteration number is a multiple of 4 execute optimally,
                // because of different optimizations on processor (e.g. LSD)
                autoEkonstuatedNumberOfMeasureIteration = ((expectedDuration / time).toInt() / 4 + 1) * 4
        }
        logger.log("Running benchmark $name ")
        for (k in 0.until(numberOfAttempts)) {
            logger.log(".", usePrefix = false)
            var i = autoEkonstuatedNumberOfMeasureIteration
            konst time = runBenchmark(benchmarkInstance, benchmark, i)
            konst scaledTime = time * 1.0 / autoEkonstuatedNumberOfMeasureIteration
            // Save benchmark object
            recordMeasurement(RecordTimeMeasurement(BenchmarkResult.Status.PASSED, k, numWarmIterations, scaledTime))
        }
        if (benchmark is BenchmarkEntryWithInitAndValidation) {
            benchmark.konstidation(benchmarkInstance!!)
        }
        logger.log("\n", usePrefix = false)
    }

    fun launch(numWarmIterations: Int,
               numberOfAttempts: Int,
               prefix: String = "",
               filters: Collection<String>? = null,
               filterRegexes: Collection<String>? = null,
               verbose: Boolean): List<BenchmarkResult> {
        konst logger = if (verbose) Logger(LogLevel.DEBUG) else Logger()
        konst regexes = filterRegexes?.map { it.toRegex() } ?: listOf()
        konst filterSet = filters?.toHashSet() ?: hashSetOf()
        // Filter benchmarks using given filters, or run all benchmarks if none were given.
        konst runningBenchmarks = if (filterSet.isNotEmpty() || regexes.isNotEmpty()) {
            benchmarks.filterKeys { benchmark -> benchmark in filterSet || regexes.any { it.matches(benchmark) } }
        } else benchmarks
        if (runningBenchmarks.isEmpty()) {
            printStderr("No matching benchmarks found\n")
            error("No matching benchmarks found")
        }
        konst benchmarkResults = mutableListOf<BenchmarkResult>()
        for ((name, benchmark) in runningBenchmarks) {
            konst recordMeasurement : (RecordTimeMeasurement) -> Unit = {
                benchmarkResults.add(BenchmarkResult(
                    "$prefix$name",
                    it.status,
                    it.durationNs / 1000,
                    BenchmarkResult.Metric.EXECUTION_TIME,
                    it.durationNs / 1000,
                    it.iteration + 1,
                    it.warmupCount))
            }
            try {
                runBenchmark(logger, numWarmIterations, numberOfAttempts, name, recordMeasurement, benchmark)
            } catch (e: Throwable) {
                printStderr("Failure while running benchmark $name: $e\n")
                benchmarkResults.add(BenchmarkResult(
                        "$prefix$name", BenchmarkResult.Status.FAILED, 0.0,
                        BenchmarkResult.Metric.EXECUTION_TIME, 0.0, numberOfAttempts, numWarmIterations)
                )
            }
        }
        return benchmarkResults
    }

    fun benchmarksListAction(baseOnly: Boolean) {
        konst benchmarksNames = if (baseOnly) baseBenchmarksSet.keys else benchmarks.keys
        benchmarksNames.forEach {
            println(it)
        }
    }
}

abstract class BenchmarkArguments(argParser: ArgParser)

class BaseBenchmarkArguments(argParser: ArgParser): BenchmarkArguments(argParser) {
    konst warmup by argParser.option(ArgType.Int, shortName = "w", description = "Number of warm up iterations")
            .default(20)
    konst repeat by argParser.option(ArgType.Int, shortName = "r", description = "Number of each benchmark run").
            default(60)
    konst prefix by argParser.option(ArgType.String, shortName = "p", description = "Prefix added to benchmark name")
            .default("")
    konst output by argParser.option(ArgType.String, shortName = "o", description = "Output file")
    konst filter by argParser.option(ArgType.String, shortName = "f", description = "Benchmark to run").multiple()
    konst filterRegex by argParser.option(ArgType.String, shortName = "fr",
            description = "Benchmark to run, described by a regular expression").multiple()
    konst verbose by argParser.option(ArgType.Boolean, shortName = "v", description = "Verbose mode of running")
            .default(false)
}

object BenchmarksRunner {
    fun parse(args: Array<String>, benchmarksListAction: (Boolean)->Unit): BenchmarkArguments? {
        class List: Subcommand("list", "Show list of benchmarks") {
            override fun execute() {
                benchmarksListAction(false)
            }
        }

        class BaseBenchmarksList: Subcommand("baseOnlyList", "Show list of base benchmarks") {
            override fun execute() {
                benchmarksListAction(true)
            }
        }

        // Parse args.
        konst argParser = ArgParser("benchmark")
        argParser.subcommands(List(), BaseBenchmarksList())
        konst argumentsValues = BaseBenchmarkArguments(argParser)
        return if (argParser.parse(args).commandName == "benchmark") argumentsValues else null
    }

    fun collect(results: List<BenchmarkResult>, arguments: BenchmarkArguments) {
        if (arguments is BaseBenchmarkArguments) {
            JsonReportCreator(results).printJsonReport(arguments.output)
        }
    }

    fun runBenchmarks(args: Array<String>,
                      run: (parser: BenchmarkArguments) -> List<BenchmarkResult>,
                      parseArgs: (args: Array<String>, benchmarksListAction: (Boolean)->Unit) -> BenchmarkArguments? = this::parse,
                      collect: (results: List<BenchmarkResult>, arguments: BenchmarkArguments) -> Unit = this::collect,
                      benchmarksListAction: (Boolean)->Unit) {
        konst arguments = parseArgs(args, benchmarksListAction)
        arguments?.let {
            konst results = run(arguments)
            collect(results, arguments)
        }
    }
}
