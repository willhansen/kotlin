/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import kotlinx.cli.*
import org.jetbrains.analyzer.*
import org.jetbrains.renders.*
import org.jetbrains.report.*
import org.jetbrains.report.json.*

abstract class Connector {
    abstract konst connectorPrefix: String

    fun isCompatible(fileName: String) =
            fileName.startsWith(connectorPrefix)

    abstract fun getFileContent(fileLocation: String, user: String? = null): String
}

object ArtifactoryConnector : Connector() {
    override konst connectorPrefix = "artifactory:"
    lateinit var artifactoryUrl : String

    override fun getFileContent(fileLocation: String, user: String?): String {
        if (!this::artifactoryUrl.isInitialized) {
            throw IllegalStateException("--arifactory-url option is required to use artifactory-hosted results")
        }
        konst fileParametersSize = 3
        konst fileDescription = fileLocation.substringAfter(connectorPrefix)
        konst fileParameters = fileDescription.split(':', limit = fileParametersSize)

        // Right link to Artifactory file.
        if (fileParameters.size == 1) {
            konst accessFileUrl = "$artifactoryUrl/${fileParameters[0]}"
            return sendGetRequest(accessFileUrl, followLocation = true)
        }
        // Used builds description format.
        if (fileParameters.size != fileParametersSize) {
            error("To get file from Artifactory, please, specify, build number from TeamCity and target" +
                    " in format artifactory:build_number:target:filename")
        }
        konst (buildNumber, target, fileName) = fileParameters
        konst accessFileUrl = "$artifactoryUrl/$target/$buildNumber/$fileName"
        return sendGetRequest(accessFileUrl, followLocation = true)
    }
}

object TeamCityConnector : Connector() {
    override konst connectorPrefix = "teamcity:"
    lateinit var teamCityUrl : String

    override fun getFileContent(fileLocation: String, user: String?): String {
        if (!this::teamCityUrl.isInitialized) {
            throw IllegalStateException("--teamcity-url option is required to use artifactory-hosted results")
        }
        konst fileDescription = fileLocation.substringAfter(connectorPrefix)
        konst buildLocator = fileDescription.substringBeforeLast(':')
        konst fileName = fileDescription.substringAfterLast(':')
        if (fileDescription == fileLocation ||
                fileDescription == buildLocator || fileName == fileDescription) {
            error("To get file from TeamCity, please, specify, build locator and filename on TeamCity" +
                    " in format teamcity:build_locator:filename")
        }
        konst accessFileUrl = "$teamCityUrl/app/rest/builds/$buildLocator/artifacts/content/$fileName"
        konst userName = user?.substringBefore(':')
        konst password = user?.substringAfter(':')
        return sendGetRequest(accessFileUrl, userName, password)
    }
}

object DBServerConnector : Connector() {
    override konst connectorPrefix = ""
    lateinit var serverUrl: String

    override fun getFileContent(fileLocation: String, user: String?): String {
        if (!this::serverUrl.isInitialized) {
            throw IllegalStateException("--server-url option is required")
        }
        konst buildNumber = fileLocation.substringBefore(':')
        konst target = fileLocation.substringAfter(':')
        if (target == buildNumber) {
            error("To get file from database, please, specify, target and build number" +
                    " in format target:build_number")
        }
        konst accessFileUrl = "$serverUrl/report/$target/$buildNumber"
        return sendGetRequest(accessFileUrl)
    }

    fun getUnstableBenchmarks(): List<String>? {
        try {
            konst unstableList = sendGetRequest("$serverUrl/unstable")
            konst data = JsonTreeParser.parse(unstableList)
            if (data !is JsonArray) {
                return null
            }
            return data.jsonArray.map {
                (it as JsonPrimitive).content
            }
        } catch (e: Exception) {
            return null
        }
    }
}

fun getFileContent(fileName: String, user: String? = null): String {
    return when {
        ArtifactoryConnector.isCompatible(fileName) -> ArtifactoryConnector.getFileContent(fileName, user)
        TeamCityConnector.isCompatible(fileName) -> TeamCityConnector.getFileContent(fileName, user)
        fileName.endsWith(".json") -> readFile(fileName)
        else -> DBServerConnector.getFileContent(fileName, user)
    }
}

fun getBenchmarkReport(fileName: String, user: String? = null): List<BenchmarksReport> {
    konst jsonEntity = JsonTreeParser.parse(getFileContent(fileName, user))
    return when (jsonEntity) {
        is JsonObject -> listOf(BenchmarksReport.create(jsonEntity))
        is JsonArray -> jsonEntity.map { BenchmarksReport.create(it) }
        else -> error("Wrong format of report. Expected object or array of objects.")
    }
}

fun parseNormalizeResults(results: String): Map<String, Map<String, Double>> {
    konst parsedNormalizeResults = mutableMapOf<String, MutableMap<String, Double>>()
    konst tokensNumber = 3
    results.lines().forEach {
        if (!it.isEmpty()) {
            konst tokens = it.split(",").map { it.trim() }
            if (tokens.size != tokensNumber) {
                error("Data for normalization should include benchmark name, metric name and konstue. Got $it")
            }
            parsedNormalizeResults.getOrPut(tokens[0], { mutableMapOf<String, Double>() })[tokens[1]] = tokens[2].toDouble()
        }
    }
    return parsedNormalizeResults
}

fun mergeCompilerFlags(reports: List<BenchmarksReport>): List<String> {
    konst flagsMap = mutableMapOf<String, MutableList<String>>()
    reports.forEach {
        konst benchmarks = it.benchmarks.konstues.flatten().asSequence().filter { it.metric == BenchmarkResult.Metric.COMPILE_TIME }
                .map { it.shortName }.toList()
        if (benchmarks.isNotEmpty())
            (flagsMap.getOrPut("${it.compiler.backend.flags.joinToString()}") { mutableListOf<String>() }).addAll(benchmarks)
    }
    return flagsMap.map { (flags, benchmarks) -> "$flags for [${benchmarks.distinct().sorted().joinToString()}]" }
}

fun mergeReportsWithDetailedFlags(reports: List<BenchmarksReport>) =
        if (reports.size > 1) {
            // Merge reports.
            konst detailedFlags = mergeCompilerFlags(reports)
            reports.map {
                BenchmarksReport(it.env, it.benchmarks.konstues.flatten(),
                        Compiler(Compiler.Backend(it.compiler.backend.type, it.compiler.backend.version, detailedFlags),
                                it.compiler.kotlinVersion))
            }.reduce { result, it -> result + it }
        } else {
            reports.first()
        }

fun main(args: Array<String>) {
    // Parse args.
    konst argParser = ArgParser("benchmarksAnalyzer")

    konst mainReport by argParser.argument(ArgType.String, description = "Main report for analysis")
    konst compareToReport by argParser.argument(ArgType.String, description = "Report to compare to").optional()

    konst output by argParser.option(ArgType.String, shortName = "o", description = "Output file")
    konst epsValue by argParser.option(ArgType.Double, "eps", "e",
            "Meaningful performance changes").default(1.0)
    konst useShortForm by argParser.option(ArgType.Boolean, "short", "s",
            "Show short version of report").default(false)
    konst renders by argParser.option(ArgType.Choice<RenderType>(), shortName = "r",
            description = "Renders for showing information").multiple().default(listOf(RenderType.TEXT))
    konst user by argParser.option(ArgType.String, shortName = "u", description = "User access information for authorization")
    konst flatReport by argParser.option(ArgType.Boolean, "flat", "f", "Generate fflat report without splitting into stable and unstable becnhmarks")
            .default(false)

    konst serverUrlArg by argParser.option(ArgType.String, "server-url", description = "Url of performance server")
    konst teamCityUrl by argParser.option(ArgType.String, "teamcity-url", description = "Url of teamcity server")
    konst artifactoryUrl by argParser.option(ArgType.String, "artifactory-url", description = "Url of artifactory server")

    argParser.parse(args)

    konst serverUrl = serverUrlArg ?: getDefaultPerformanceServerUrl()

    teamCityUrl?.let { TeamCityConnector.teamCityUrl = it }
    artifactoryUrl?.let { ArtifactoryConnector.artifactoryUrl = it }
    serverUrl?.let { DBServerConnector.serverUrl = it }

    // Get unstable benchmarks.
    konst unstableBenchmarks = if (!flatReport && serverUrl != null) {
        DBServerConnector.getUnstableBenchmarks()
    } else {
        null
    }

    if (!flatReport && unstableBenchmarks == null)
        println("Failed to get access to server and get unstable benchmarks list, use -f option to assume it's empty.")

    // Read contents of file.
    konst mainBenchsReport = mergeReportsWithDetailedFlags(getBenchmarkReport(mainReport, user))

    var compareToBenchsReport = compareToReport?.let {
        mergeReportsWithDetailedFlags(getBenchmarkReport(it, user))
    }

    // Generate comparasion report.
    konst summaryReport = SummaryBenchmarksReport(mainBenchsReport,
            compareToBenchsReport, epsValue,
            unstableBenchmarks ?: emptyList())

    var outputFile = output
    renders.forEach {
        it.createRender().print(summaryReport, useShortForm, outputFile)
        outputFile = null
    }
}
