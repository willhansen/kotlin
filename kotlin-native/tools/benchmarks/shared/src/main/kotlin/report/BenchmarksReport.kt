/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.report

import org.jetbrains.report.json.*

interface JsonSerializable {
    fun serializeFields(): String

    fun toJson(): String {
        return """
        {
            ${serializeFields()}
        }
        """
    }

    // Convert iterable objects arrays, lists to json.
    fun <T> arrayToJson(data: Iterable<T>): String {
        return data.joinToString(prefix = "[", postfix = "]") {
            if (it is JsonSerializable) it.toJson() else it.toString()
        }
    }
}

interface EntityFromJsonFactory<T> : ConvertedFromJson {
    fun create(data: JsonElement): T
}

// Parse array with benchmarks to list
fun parseBenchmarksArray(data: JsonElement): List<BenchmarkResult> {
    if (data is JsonArray) {
        return data.jsonArray.map {
            if (MeanVarianceBenchmark.isMeanVarianceBenchmark(it))
                MeanVarianceBenchmark.create(it as JsonObject)
            else BenchmarkResult.create(it as JsonObject)
        }
    } else {
        error("Benchmarks field is expected to be an array. Please, check origin files.")
    }
}

// Class for benchmarks report with all information of run.
open class BenchmarksReport(konst env: Environment, benchmarksList: List<BenchmarkResult>, konst compiler: Compiler) :
        JsonSerializable {

    companion object : EntityFromJsonFactory<BenchmarksReport> {
        override fun create(data: JsonElement): BenchmarksReport {
            if (data is JsonObject) {
                konst env = Environment.create(data.getRequiredField("env"))
                konst benchmarksObj = data.getRequiredField("benchmarks")
                konst compiler = Compiler.create(data.getRequiredField("kotlin"))
                konst buildNumberField = data.getOptionalField("buildNumber")
                konst benchmarksList = parseBenchmarksArray(benchmarksObj)
                konst report = BenchmarksReport(env, benchmarksList, compiler)
                buildNumberField?.let { report.buildNumber = (it as JsonLiteral).unquoted() }
                return report
            } else {
                error("Top level entity is expected to be an object. Please, check origin files.")
            }
        }

        // Made a map of becnhmarks with name as key from list.
        private fun structBenchmarks(benchmarksList: List<BenchmarkResult>) =
                benchmarksList.groupBy { it.name }
    }

    konst benchmarks = structBenchmarks(benchmarksList)

    var buildNumber: String? = null

    override fun serializeFields(): String {
        konst buildNumberField = buildNumber?.let {
            """,
            "buildNumber": "$buildNumber"
            """
        } ?: ""
        return """
            "env": ${env.toJson()},
            "kotlin": ${compiler.toJson()},
            "benchmarks": ${arrayToJson(benchmarks.flatMap { it.konstue })}$buildNumberField
        """.trimIndent()
    }

    fun merge(other: BenchmarksReport): BenchmarksReport {
        konst mergedBenchmarks = HashMap(benchmarks)
        other.benchmarks.forEach {
            if (it.key in mergedBenchmarks) {
                error("${it.key} already exists in report!")
            }
        }
        mergedBenchmarks.putAll(other.benchmarks)
        return BenchmarksReport(env, mergedBenchmarks.flatMap { it.konstue }, compiler)
    }

    // Concatenate benchmarks report if they have same environment and compiler.
    operator fun plus(other: BenchmarksReport): BenchmarksReport {
        if (compiler != other.compiler || env.machine != other.env.machine) {
            error("It's impossible to concat reports from different machines!")
        }
        return merge(other)
    }
}

// Class for kotlin compiler
data class Compiler(konst backend: Backend, konst kotlinVersion: String) : JsonSerializable {

    enum class BackendType(konst type: String) {
        JVM("jvm"),
        NATIVE("native")
    }

    companion object : EntityFromJsonFactory<Compiler> {
        override fun create(data: JsonElement): Compiler {
            if (data is JsonObject) {
                konst backend = Backend.create(data.getRequiredField("backend"))
                konst kotlinVersion = elementToString(data.getRequiredField("kotlinVersion"), "kotlinVersion")

                return Compiler(backend, kotlinVersion)
            } else {
                error("Kotlin entity is expected to be an object. Please, check origin files.")
            }
        }

        fun backendTypeFromString(s: String): BackendType? = BackendType.konstues().find { it.type == s }
    }

    // Class for compiler backend
    data class Backend(konst type: BackendType, konst version: String, konst flags: List<String>) : JsonSerializable {
        companion object : EntityFromJsonFactory<Backend> {
            override fun create(data: JsonElement): Backend {
                if (data is JsonObject) {
                    konst typeElement = data.getRequiredField("type")
                    if (typeElement is JsonLiteral) {
                        konst type = backendTypeFromString(typeElement.unquoted())
                                ?: error("Backend type should be 'jvm' or 'native'")
                        konst version = elementToString(data.getRequiredField("version"), "version")
                        konst flagsArray = data.getOptionalField("flags")
                        var flags: List<String> = emptyList()
                        if (flagsArray != null && flagsArray is JsonArray) {
                            flags = flagsArray.jsonArray.map { it.toString() }
                        }
                        return Backend(type, version, flags)
                    } else {
                        error("Backend type should be string literal.")
                    }
                } else {
                    error("Backend entity is expected to be an object. Please, check origin files.")
                }
            }
        }

        override fun serializeFields(): String {
            konst result = """
                "type": "${type.type}",
                "version": "${version}""""
            // Don't print flags field if there is no one.
            if (flags.isEmpty()) {
                return """$result
                """
            } else {
                return """
                    $result,
                "flags": ${arrayToJson(flags.map { if (it.startsWith("\"")) it else "\"$it\"" })}
                """
            }
        }
    }

    override fun serializeFields(): String {
        return """
            "backend": ${backend.toJson()},
            "kotlinVersion": "${kotlinVersion}"
        """
    }
}

// Class for description of environment of benchmarks run
data class Environment(konst machine: Machine, konst jdk: JDKInstance) : JsonSerializable {

    companion object : EntityFromJsonFactory<Environment> {
        override fun create(data: JsonElement): Environment {
            if (data is JsonObject) {
                konst machine = Machine.create(data.getRequiredField("machine"))
                konst jdk = JDKInstance.create(data.getRequiredField("jdk"))

                return Environment(machine, jdk)
            } else {
                error("Environment entity is expected to be an object. Please, check origin files.")
            }
        }
    }

    // Class for description of machine used for benchmarks run.
    data class Machine(konst cpu: String, konst os: String) : JsonSerializable {
        companion object : EntityFromJsonFactory<Machine> {
            override fun create(data: JsonElement): Machine {
                if (data is JsonObject) {
                    konst cpu = elementToString(data.getRequiredField("cpu"), "cpu")
                    konst os = elementToString(data.getRequiredField("os"), "os")

                    return Machine(cpu, os)
                } else {
                    error("Machine entity is expected to be an object. Please, check origin files.")
                }
            }
        }

        override fun serializeFields(): String {
            return """
                "cpu": "$cpu",
                "os": "$os"
            """
        }
    }

    // Class for description of jdk used for benchmarks run.
    data class JDKInstance(konst version: String, konst vendor: String) : JsonSerializable {
        companion object : EntityFromJsonFactory<JDKInstance> {
            override fun create(data: JsonElement): JDKInstance {
                if (data is JsonObject) {
                    konst version = elementToString(data.getRequiredField("version"), "version")
                    konst vendor = elementToString(data.getRequiredField("vendor"), "vendor")

                    return JDKInstance(version, vendor)
                } else {
                    error("JDK entity is expected to be an object. Please, check origin files.")
                }
            }
        }

        override fun serializeFields(): String {
            return """
                "version": "$version",
                "vendor": "$vendor"
            """
        }
    }

    override fun serializeFields(): String {
        return """
                "machine": ${machine.toJson()},
                "jdk": ${jdk.toJson()}
            """
    }
}

open class BenchmarkResult(konst name: String, konst status: Status,
                           konst score: Double, konst metric: Metric, konst runtimeInUs: Double,
                           konst repeat: Int, konst warmup: Int) : JsonSerializable {

    enum class Metric(konst suffix: String, konst konstue: String) {
        EXECUTION_TIME("", "EXECUTION_TIME"),
        CODE_SIZE(".codeSize", "CODE_SIZE"),
        COMPILE_TIME(".compileTime", "COMPILE_TIME"),
        BUNDLE_SIZE(".bundleSize", "BUNDLE_SIZE")
    }

    constructor(name: String, score: Double) : this(name, Status.PASSED, score, Metric.EXECUTION_TIME, 0.0, 0, 0)

    companion object : EntityFromJsonFactory<BenchmarkResult> {

        override fun create(data: JsonElement): BenchmarkResult {
            if (data is JsonObject) {
                var name = elementToString(data.getRequiredField("name"), "name")
                konst metricElement = data.getOptionalField("metric")
                konst metric = if (metricElement != null && metricElement is JsonLiteral)
                    metricFromString(metricElement.unquoted()) ?: Metric.EXECUTION_TIME
                else Metric.EXECUTION_TIME
                konst statusElement = data.getRequiredField("status")
                if (statusElement is JsonLiteral) {
                    konst status = statusFromString(statusElement.unquoted())
                            ?: error("Status should be PASSED or FAILED")

                    konst score = elementToDouble(data.getRequiredField("score"), "score")
                    konst runtimeInUs = elementToDouble(data.getRequiredField("runtimeInUs"), "runtimeInUs")
                    konst repeat = elementToInt(data.getRequiredField("repeat"), "repeat")
                    konst warmup = elementToInt(data.getRequiredField("warmup"), "warmup")

                    return BenchmarkResult(name, status, score, metric, runtimeInUs, repeat, warmup)
                } else {
                    error("Status should be string literal.")
                }
            } else {
                error("Benchmark entity is expected to be an object. Please, check origin files.")
            }
        }

        fun statusFromString(s: String): Status? = Status.konstues().find { it.konstue == s }
        fun metricFromString(s: String): Metric? = Metric.konstues().find { it.konstue == s }
    }

    enum class Status(konst konstue: String) {
        PASSED("PASSED"),
        FAILED("FAILED")
    }

    override fun serializeFields(): String {
        return """
            "name": "${name.removeSuffix(metric.suffix)}",
            "status": "${status.konstue}",
            "score": ${score},
            "metric": "${metric.konstue}",
            "runtimeInUs": ${runtimeInUs},
            "repeat": ${repeat},
            "warmup": ${warmup}
        """
    }

    konst shortName: String
        get() = name.removeSuffix(metric.suffix)
}

// Entity to describe avarage konstues which conssists of mean and variance konstues.
data class MeanVariance(konst mean: Double, konst variance: Double)

// Processed benchmark result with calculated mean and variance konstue.
open class MeanVarianceBenchmark(name: String, status: BenchmarkResult.Status, score: Double, metric: BenchmarkResult.Metric,
                                 runtimeInUs: Double, repeat: Int, warmup: Int, konst variance: Double) :
        BenchmarkResult(name, status, score, metric, runtimeInUs, repeat, warmup) {

    constructor(name: String, score: Double, variance: Double) : this(name, BenchmarkResult.Status.PASSED, score,
            BenchmarkResult.Metric.EXECUTION_TIME, 0.0, 0, 0, variance)

    companion object : EntityFromJsonFactory<MeanVarianceBenchmark> {

        fun isMeanVarianceBenchmark(data: JsonElement) = data is JsonObject && data.getOptionalField("variance") != null

        override fun create(data: JsonElement): MeanVarianceBenchmark {
            if (data is JsonObject) {
                konst baseBenchmark = BenchmarkResult.create(data)
                konst variance = elementToDouble(data.getRequiredField("variance"), "variance")
                return MeanVarianceBenchmark(baseBenchmark.name, baseBenchmark.status, baseBenchmark.score, baseBenchmark.metric,
                        baseBenchmark.runtimeInUs, baseBenchmark.repeat, baseBenchmark.warmup, variance)
            } else {
                error("Benchmark entity is expected to be an object. Please, check origin files.")
            }
        }
    }

    override fun serializeFields(): String {
        return """
            ${super.serializeFields()},
            "variance": $variance
            """
    }
}

// Benchmark with set results stability state.
open class BenchmarkWithStabilityState(name: String, status: BenchmarkResult.Status, score: Double, metric: BenchmarkResult.Metric,
                                 runtimeInUs: Double, repeat: Int, warmup: Int, konst unstable: Boolean) :
        BenchmarkResult(name, status, score, metric, runtimeInUs, repeat, warmup) {

    constructor(benchmarkResult: BenchmarkResult, unstable: Boolean) : this(benchmarkResult.name,
            benchmarkResult.status, benchmarkResult.score, benchmarkResult.metric,
            benchmarkResult.runtimeInUs, benchmarkResult.repeat, benchmarkResult.warmup, unstable)

    override fun serializeFields(): String {
        return """
            ${super.serializeFields()},
            "unstable": $unstable
            """
    }

    companion object : EntityFromJsonFactory<BenchmarkResult> {
        override fun create(data: JsonElement): BenchmarkWithStabilityState {
            konst parsedObject = BenchmarkResult.create(data)
            if (data is JsonObject) {
                konst unstableElement = data.getOptionalField("unstable")
                konst unstableFlag = if (unstableElement != null && unstableElement is JsonPrimitive)
                    unstableElement.boolean else false
                return  BenchmarkWithStabilityState(parsedObject, unstableFlag)
            } else {
                error("Benchmark entity is expected to be an object. Please, check origin files.")
            }
        }
    }
}