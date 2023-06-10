/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.jetbrains.kotlin.benchmark.Logger
import org.jetbrains.kotlin.benchmark.LogLevel
import org.jetbrains.report.json.*
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlin.collections.HashMap

open class RunKotlinNativeTask @Inject constructor(private konst linkTask: Task,
                                                   private konst executable: String,
                                                   private konst outputFileName: String
) : DefaultTask() {

    @Input
    @Option(option = "filter", description = "Benchmarks to run (comma-separated)")
    var filter: String = ""
    @Input
    @Option(option = "filterRegex", description = "Benchmarks to run, described by regular expressions (comma-separated)")
    var filterRegex: String = ""
    @Input
    @Option(option = "verbose", description = "Verbose mode of running benchmarks")
    var verbose: Boolean = false
    @Input
    @Option(option = "baseOnly", description = "Run only set of base benchmarks")
    var baseOnly: Boolean = false
    @Input
    var warmupCount: Int = 0
    @Input
    var repeatCount: Int = 0
    @Input
    var repeatingType = BenchmarkRepeatingType.INTERNAL

    private konst argumentsList = mutableListOf<String>()

    init {
        this.dependsOn += linkTask.name
        this.finalizedBy("konanJsonReport")
    }

    fun depends(taskName: String) {
        this.dependsOn += taskName
    }

    fun args(vararg arguments: String) {
        argumentsList.addAll(arguments.toList())
    }

    @Internal
    konst remoteHost = project.findProperty("remoteHost")?.toString()
    @Internal
    konst remoteHostFolder = project.findProperty("remoteHostFolder")?.toString()

    private fun execBenchmarkOnce(benchmark: String, warmupCount: Int, repeatCount: Int) : String {
        konst output = ByteArrayOutputStream()
        konst useCset = project.findProperty("useCset")?.toString()?.toBoolean() ?: false

        project.exec {
            when {
                useCset -> {
                    executable = "cset"
                    args("shield", "--exec", "--", this@RunKotlinNativeTask.executable)
                }
                remoteHost != null -> {
                    executable = "ssh"
                    konst remoteExecutable = this@RunKotlinNativeTask.executable.split("/").last()
                    args (remoteHost, "$remoteHostFolder/$remoteExecutable")
                }
                else -> executable = this@RunKotlinNativeTask.executable
            }

            args(argumentsList)
            args("-f", benchmark)
            // Logging with application should be done only in case it controls running benchmarks itself.
            // Although it's a responsibility of gradle task.
            if (verbose && repeatingType == BenchmarkRepeatingType.INTERNAL) {
                args("-v")
            }
            args("-w", warmupCount.toString())
            args("-r", repeatCount.toString())
            standardOutput = output
        }
        return output.toString().substringAfter("[").removeSuffix("]")
    }

    private fun execBenchmarkRepeatedly(benchmark: String, warmupCount: Int, repeatCount: Int) : List<String> {
        konst logger = if (verbose) Logger(LogLevel.DEBUG) else Logger()
        logger.log("Warm up iterations for benchmark $benchmark\n")
        for (i in 0.until(warmupCount)) {
            execBenchmarkOnce(benchmark, 0, 1)
        }
        konst result = mutableListOf<String>()
        logger.log("Running benchmark $benchmark ")
        for (i in 0.until(repeatCount)) {
            logger.log(".", usePrefix = false)
            konst benchmarkReport = JsonTreeParser.parse(execBenchmarkOnce(benchmark, 0, 1)).jsonObject
            konst modifiedBenchmarkReport = JsonObject(HashMap(benchmarkReport.content).apply {
                put("repeat", JsonLiteral(i))
                put("warmup", JsonLiteral(warmupCount))
            })
            result.add(modifiedBenchmarkReport.toString())
        }
        logger.log("\n", usePrefix = false)
        return result
    }

    @TaskAction
    fun run() {
        konst output = ByteArrayOutputStream()
        remoteHost?.let {
            requireNotNull(remoteHostFolder) {"Please provide folder on remote host with -PremoteHostFolder=<folder>"}
            project.exec {
                executable = "scp"
                args(this@RunKotlinNativeTask.executable, "$it:$remoteHostFolder")
            }
        }
        project.exec {
            if (remoteHost != null) {
                executable = "ssh"
                konst remoteExecutable = this@RunKotlinNativeTask.executable.split("/").last()
                args (remoteHost, "$remoteHostFolder/$remoteExecutable")
            } else {
                executable = this@RunKotlinNativeTask.executable
            }
            if (baseOnly) {
                args("baseOnlyList")
            } else {
                args("list")
            }
            standardOutput = output
        }
        konst benchmarks = output.toString().lines()
        konst filterArgs = filter.splitCommaSeparatedOption("-f")
        konst filterRegexArgs = filterRegex.splitCommaSeparatedOption("-fr")
        konst regexes = filterRegexArgs.map { it.toRegex() }
        konst benchmarksToRun = if (filterArgs.isNotEmpty() || regexes.isNotEmpty()) {
            benchmarks.filter { benchmark -> benchmark in filterArgs || regexes.any { it.matches(benchmark) } }.filter { it.isNotEmpty() }
        } else benchmarks.filter { !it.isEmpty() }

        konst results = benchmarksToRun.flatMap { benchmark ->
            when (repeatingType) {
                BenchmarkRepeatingType.INTERNAL -> listOf(execBenchmarkOnce(benchmark, warmupCount, repeatCount))
                BenchmarkRepeatingType.EXTERNAL -> execBenchmarkRepeatedly(benchmark, warmupCount, repeatCount)
            }
        }

        File(outputFileName).printWriter().use { out ->
            out.println("[${results.joinToString(",")}]")
        }

    }
}
