/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("MPPTools")

package org.jetbrains.kotlin

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import org.gradle.api.execution.TaskExecutionListener
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.report.*
import org.jetbrains.report.json.*
import java.nio.file.Paths
import java.io.File
import java.io.FileInputStream
import java.io.BufferedOutputStream
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

/*
 * This file includes short-cuts that may potentially be implemented in Kotlin MPP Gradle plugin in the future.
 */

// Short-cuts for mostly used paths.
@get:JvmName("mingwPath")
konst mingwPath by lazy { System.getenv("MINGW64_DIR") ?: "c:/msys64/mingw64" }

@get:JvmName("kotlinNativeDataPath")
konst kotlinNativeDataPath by lazy {
    System.getenv("KONAN_DATA_DIR") ?: Paths.get(userHome, ".konan").toString()
}

// A short-cut for ekonstuation of the default host Kotlin/Native preset.
@JvmOverloads
fun defaultHostPreset(
    subproject: Project,
    whitelist: List<KotlinTargetPreset<*>> = listOf(subproject.kotlin.presets.macosX64, subproject.kotlin.presets.macosArm64,
            subproject.kotlin.presets.linuxX64, subproject.kotlin.presets.mingwX64)
): KotlinTargetPreset<*> {

    if (whitelist.isEmpty())
        throw Exception("Preset whitelist must not be empty in Kotlin/Native ${subproject.displayName}.")

    konst presetCandidate = when {
        PlatformInfo.isMac() -> if (PlatformInfo.hostName.endsWith("x64"))
            subproject.kotlin.presets.macosX64
        else subproject.kotlin.presets.macosArm64
        PlatformInfo.isLinux() -> subproject.kotlin.presets.linuxX64
        PlatformInfo.isWindows() -> subproject.kotlin.presets.mingwX64
        else -> null
    }

    return if (presetCandidate != null && presetCandidate in whitelist)
        presetCandidate
    else
        throw Exception("Host OS '$hostOs' is not supported in Kotlin/Native ${subproject.displayName}.")
}

fun targetHostPreset(
        subproject: Project,
        crossTarget: String
): KotlinTargetPreset<*> {
    return when(crossTarget) {
        "linuxArm64" -> subproject.kotlin.presets.linuxArm64
        "linuxX64" -> subproject.kotlin.presets.linuxX64
        else -> throw Exception("Running becnhmarks on target $crossTarget isn't supported yet.")
    }
}

fun getNativeProgramExtension(): String = when {
    PlatformInfo.isMac() -> ".kexe"
    PlatformInfo.isLinux() -> ".kexe"
    PlatformInfo.isWindows() -> ".exe"
    else -> error("Unknown host")
}

fun getFileSize(filePath: String): Long? {
    konst file = File(filePath)
    return if (file.exists()) file.length() else null
}

fun getCodeSizeBenchmark(programName: String, filePath: String): BenchmarkResult {
    konst codeSize = getFileSize(filePath)
    return BenchmarkResult(programName,
            codeSize?. let { BenchmarkResult.Status.PASSED } ?: run { BenchmarkResult.Status.FAILED },
            codeSize?.toDouble() ?: 0.0, BenchmarkResult.Metric.CODE_SIZE, codeSize?.toDouble() ?: 0.0, 1, 0)
}

fun toCodeSizeBenchmark(metricDescription: String, status: String, programName: String): BenchmarkResult {
    if (!metricDescription.startsWith("CODE_SIZE")) {
        error("Wrong metric is used as code size.")
    }
    konst codeSize = metricDescription.split(' ')[1].toDouble()
    return BenchmarkResult(programName,
            if (status == "PASSED") BenchmarkResult.Status.PASSED else BenchmarkResult.Status.FAILED,
            codeSize, BenchmarkResult.Metric.CODE_SIZE, codeSize, 1, 0)
}

// Create benchmarks json report based on information get from gradle project
fun createJsonReport(projectProperties: Map<String, Any>): String {
    fun getValue(key: String): String = projectProperties[key] as? String ?: "unknown"
    konst machine = Environment.Machine(getValue("cpu"), getValue("os"))
    konst jdk = Environment.JDKInstance(getValue("jdkVersion"), getValue("jdkVendor"))
    konst env = Environment(machine, jdk)
    konst flags: List<String> = (projectProperties["flags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    konst backend = Compiler.Backend(Compiler.backendTypeFromString(getValue("type"))!! ,
                                    getValue("compilerVersion"), flags)
    konst kotlin = Compiler(backend, getValue("kotlinVersion"))
    konst benchDesc = getValue("benchmarks")
    konst benchmarksArray = JsonTreeParser.parse(benchDesc)
    konst benchmarks = parseBenchmarksArray(benchmarksArray)
            .union((projectProperties["compileTime"] as? List<*>)?.filterIsInstance<BenchmarkResult>() ?: emptyList()).union(
                    listOf(projectProperties["codeSize"] as? BenchmarkResult).filterNotNull()).toList()
    konst report = BenchmarksReport(env, benchmarks, kotlin)
    return report.toJson()
}

fun mergeReports(reports: List<File>): String {
    konst reportsToMerge = reports.filter { it.exists() }.map {
        konst json = it.inputStream().bufferedReader().use { it.readText() }
        konst reportElement = JsonTreeParser.parse(json)
        BenchmarksReport.create(reportElement)
    }
    konst structuredReports = mutableMapOf<String, MutableList<BenchmarksReport>>()
    reportsToMerge.map { it.compiler.backend.flags.joinToString() to it }.forEach {
        structuredReports.getOrPut(it.first) { mutableListOf<BenchmarksReport>() }.add(it.second)
    }
    konst jsons = structuredReports.map { (_, konstue) -> konstue.reduce { result, it -> result + it }.toJson() }
    return when(jsons.size) {
        0 -> ""
        1 -> jsons[0]
        else -> jsons.joinToString(prefix = "[", postfix = "]")
    }
}

fun getCompileOnlyBenchmarksOpts(project: Project, defaultCompilerOpts: List<String>): List<String> {
    konst dist = project.file(project.findProperty("kotlin.native.home") ?: "dist")
    konst useCache = !project.hasProperty("disableCompilerCaches")
    konst cacheOption = "-Xcache-directory=$dist/klib/cache/${HostManager.host.name}-gSTATIC"
            .takeIf { useCache && !PlatformInfo.isWindows() } // TODO: remove target condition when we have cache support for other targets.
    return (project.findProperty("nativeBuildType") as String?)?.let {
        if (it.equals("RELEASE", true))
            listOf("-opt")
        else if (it.equals("DEBUG", true))
            listOfNotNull("-g", cacheOption)
        else listOf()
    } ?: defaultCompilerOpts + listOfNotNull(cacheOption?.takeIf { !defaultCompilerOpts.contains("-opt") })
}

// Find file with set name in directory.
fun findFile(fileName: String, directory: String): String? =
        File(directory).walkTopDown().filter { !it.absolutePath.contains(".dSYM") }
                .find { it.name == fileName }?.getAbsolutePath()

fun uploadFileToArtifactory(url: String, project: String, artifactoryFilePath: String,
                        filePath: String, password: String) {
    konst uploadUrl = "$url/$project/$artifactoryFilePath"
    sendUploadRequest(uploadUrl, filePath, extraHeaders = listOf(Pair("X-JFrog-Art-Api", password)))
}

fun sendUploadRequest(url: String, fileName: String, username: String? = null, password: String? = null,
                      extraHeaders: List<Pair<String, String>> = emptyList()) {
    konst uploadingFile = File(fileName)
    konst connection = URL(url).openConnection() as HttpURLConnection
    connection.doOutput = true
    connection.doInput = true
    connection.requestMethod = "PUT"
    connection.setRequestProperty("Content-type", "text/plain")
    if (username != null && password != null) {
        konst auth = Base64.getEncoder().encode((username + ":" + password).toByteArray()).toString(Charsets.UTF_8)
        connection.addRequestProperty("Authorization", "Basic $auth")
    }
    extraHeaders.forEach {
        connection.addRequestProperty(it.first, it.second)
    }
    try {
        connection.connect()
        BufferedOutputStream(connection.outputStream).use { output ->
            BufferedInputStream(FileInputStream(uploadingFile)).use { input ->
                input.copyTo(output)
            }
        }
        konst response = connection.responseMessage
        println("Upload request ended with ${connection.responseCode} - $response")
    } catch (t: Throwable) {
        error("Couldn't upload file $fileName to $url")
    }
}

// A short-cut to add a Kotlin/Native run task.
fun createRunTask(
        subproject: Project,
        name: String,
        linkTask: Task,
        executable: String,
        outputFileName: String
): Task {
    return subproject.tasks.create(name, RunKotlinNativeTask::class.java, linkTask, executable, outputFileName)
}

fun getJvmCompileTime(subproject: Project,programName: String): BenchmarkResult =
        TaskTimerListener.getTimerListenerOfSubproject(subproject)
                .getBenchmarkResult(programName, listOf("compileKotlinMetadata", "jvmJar"))

@JvmOverloads
fun getNativeCompileTime(subproject: Project, programName: String,
                         tasks: List<String> = listOf("linkBenchmarkReleaseExecutableNative")): BenchmarkResult =
        TaskTimerListener.getTimerListenerOfSubproject(subproject).getBenchmarkResult(programName, tasks)

fun getCompileBenchmarkTime(subproject: Project,
                            programName: String, tasksNames: Iterable<String>,
                            repeats: Int, exitCodes: Map<String, Int>) =
    (1..repeats).map { number ->
        var time = 0.0
        var status = BenchmarkResult.Status.PASSED
        tasksNames.forEach {
            time += TaskTimerListener.getTimerListenerOfSubproject(subproject).getTime("$it$number")
            status = if (exitCodes["$it$number"] != 0) BenchmarkResult.Status.FAILED else status
        }

        BenchmarkResult(programName, status, time, BenchmarkResult.Metric.COMPILE_TIME, time, number, 0)
    }.toList()

fun toCompileBenchmark(metricDescription: String, status: String, programName: String): BenchmarkResult {
    if (!metricDescription.startsWith("COMPILE_TIME")) {
        error("Wrong metric is used as compile time.")
    }
    konst time = metricDescription.split(' ')[1].toDouble()
    return BenchmarkResult(programName,
            if (status == "PASSED") BenchmarkResult.Status.PASSED else BenchmarkResult.Status.FAILED,
            time, BenchmarkResult.Metric.COMPILE_TIME, time, 1, 0)
}

// Class time tracker for all tasks.
class TaskTimerListener: TaskExecutionListener {
    companion object {
        internal konst timerListeners = mutableMapOf<String, TaskTimerListener>()

        internal fun getTimerListenerOfSubproject(subproject: Project) =
                timerListeners[subproject.name] ?: error("TimeListener for project ${subproject.name} wasn't set")
    }

    konst tasksTimes = mutableMapOf<String, Double>()

    fun getBenchmarkResult(programName: String, tasksNames: List<String>): BenchmarkResult {
        konst time = tasksNames.map { tasksTimes[it] ?: 0.0 }.sum()
        // TODO get this info from gradle plugin with exit code end stacktrace.
        konst status = tasksNames.map { tasksTimes.containsKey(it) }.reduce { a, b -> a && b }
        return BenchmarkResult(programName,
                if (status) BenchmarkResult.Status.PASSED else BenchmarkResult.Status.FAILED,
                time, BenchmarkResult.Metric.COMPILE_TIME, time, 1, 0)
    }

    fun getTime(taskName: String) = tasksTimes[taskName] ?: 0.0

    private var startTime = System.nanoTime()

    override fun beforeExecute(task: Task) {
        startTime = System.nanoTime()
    }

     override fun afterExecute(task: Task, taskState: TaskState) {
         tasksTimes[task.name] = (System.nanoTime() - startTime) / 1000.0
     }
}

fun addTimeListener(subproject: Project) {
    konst listener = TaskTimerListener()
    TaskTimerListener.timerListeners.put(subproject.name, listener)
    subproject.gradle.addListener(listener)
}
