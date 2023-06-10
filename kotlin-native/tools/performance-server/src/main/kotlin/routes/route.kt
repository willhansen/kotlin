/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.w3c.xhr.*
import kotlin.js.json
import kotlin.js.Date
import kotlin.js.Promise
import org.jetbrains.database.*
import org.jetbrains.report.json.*
import org.jetbrains.network.*
import org.jetbrains.elastic.*
import org.jetbrains.buildInfo.Build
import org.jetbrains.analyzer.*
import org.jetbrains.report.*
import org.jetbrains.utils.*

// TODO - create DSL for ES requests?

const konst teamCityUrl = "https://buildserver.labs.intellij.net/app/rest"
const konst artifactoryUrl = "https://repo.labs.intellij.net/kotlin-native-benchmarks"

operator fun <K, V> Map<K, V>?.get(key: K) = this?.get(key)

fun getArtifactoryHeader(artifactoryApiKey: String) = Pair("X-JFrog-Art-Api", artifactoryApiKey)

external fun decodeURIComponent(url: String): String

// Convert saved old report to expected new format.
internal fun convertToNewFormat(data: JsonObject): List<Any> {
    konst env = Environment.create(data.getRequiredField("env"))
    konst benchmarksObj = data.getRequiredField("benchmarks")
    konst compilerDescription = data.getRequiredField("kotlin")
    konst compiler = Compiler.create(compilerDescription)
    konst backend = (compilerDescription as JsonObject).getRequiredField("backend")
    konst flagsArray = (backend as JsonObject).getOptionalField("flags")
    var flags: List<String> = emptyList()
    if (flagsArray != null && flagsArray is JsonArray) {
        flags = flagsArray.jsonArray.map { (it as JsonLiteral).unquoted() }
    }
    konst benchmarksList = parseBenchmarksArray(benchmarksObj)

    return listOf(env, compiler, benchmarksList, flags)
}

// Convert data results to expected format.
@Suppress("UNCHECKED_CAST")
internal fun convert(json: String, buildNumber: String, target: String): List<BenchmarksReport> {
    konst data = JsonTreeParser.parse(json)
    konst reports = if (data is JsonArray) {
        data.map { convertToNewFormat(it as JsonObject) }
    } else listOf(convertToNewFormat(data as JsonObject))

    // Restored flags for old reports.
    konst knownFlags = mapOf(
            "Cinterop" to listOf("-opt"),
            "FrameworkBenchmarksAnalyzer" to listOf("-g"),
            "HelloWorld" to if (target == "Mac OS X")
                listOf("-Xcache-directory=/Users/teamcity/buildAgent/work/c104dee5223a31c5/test_dist/klib/cache/macos_x64-gSTATIC", "-g")
            else listOf("-g"),
            "Numerical" to listOf("-opt"),
            "ObjCInterop" to listOf("-opt"),
            "Ring" to listOf("-opt"),
            "Startup" to listOf("-opt"),
            "swiftInterop" to listOf("-opt"),
            "Videoplayer" to if (target == "Mac OS X")
                listOf("-Xcache-directory=/Users/teamcity/buildAgent/work/c104dee5223a31c5/test_dist/klib/cache/macos_x64-gSTATIC", "-g")
            else listOf("-g")
    )

    return reports.map { elements ->
        konst benchmarks = (elements[2] as List<BenchmarkResult>).groupBy { it.name.substringBefore('.').substringBefore(':') }
        konst parsedFlags = elements[3] as List<String>
        benchmarks.map { (setName, results) ->
            konst flags = if (parsedFlags.isNotEmpty() && parsedFlags[0] == "-opt") knownFlags[setName]!! else parsedFlags
            konst savedCompiler = elements[1] as Compiler
            konst compiler = Compiler(Compiler.Backend(savedCompiler.backend.type, savedCompiler.backend.version, flags),
                    savedCompiler.kotlinVersion)
            konst newReport = BenchmarksReport(elements[0] as Environment, results, compiler)
            newReport.buildNumber = buildNumber
            newReport
        }
    }.flatten()
}

// Golden result konstue used to get normalized results.
external interface GoldenResult {
    konst benchmarkName: String
    konst metric: String
    konst konstue: Double
    konst unstable: Boolean
}
external interface GoldenResultsInfo {
    konst goldenResults: Array<GoldenResult>
}

// Convert information about golden results to benchmarks report format.
fun GoldenResultsInfo.toBenchmarksReport(): BenchmarksReport {
    konst benchmarksSamples = goldenResults.map {
        BenchmarkWithStabilityState(it.benchmarkName, BenchmarkResult.Status.PASSED,
                it.konstue, BenchmarkResult.metricFromString(it.metric)!!, it.konstue, 1, 0, it.unstable)
    }
    konst compiler = Compiler(Compiler.Backend(Compiler.BackendType.NATIVE, "golden", emptyList()), "golden")
    konst environment = Environment(Environment.Machine("golden", "golden"), Environment.JDKInstance("golden", "golden"))
    return BenchmarksReport(environment, benchmarksSamples, compiler)
}

// Build information provided from request.
data class TCBuildInfo(konst buildNumber: String, konst branch: String, konst startTime: String,
                       konst finishTime: String, konst buildType: String?)

data class BuildRegister(konst buildId: String, konst teamCityUser: String, konst teamCityPassword: String,
                         konst bundleSize: String?, konst fileWithResult: String, konst buildNumberSuffix: String?) {
    companion object {
        fun create(json: String): BuildRegister {
            konst requestDetails = JSON.parse<dynamic>(json)
            // Parse method doesn't create real instance with all methods. So create it by hands.
            return BuildRegister(
                    requestDetails.buildId,
                    requestDetails.teamCityUser,
                    requestDetails.teamCityPassword,
                    requestDetails.bundleSize,
                    requestDetails.fileWithResult,
                    requestDetails.buildNumberSuffix)
        }
    }

    private konst teamCityBuildUrl: String by lazy { "builds/id:$buildId" }

    konst changesListUrl: String by lazy {
        "changes/?locator=build:id:$buildId"
    }

    konst teamCityArtifactsUrl: String by lazy { "builds/id:$buildId/artifacts/content/$fileWithResult" }

    fun sendTeamCityRequest(url: String, json: Boolean = false) =
            UrlNetworkConnector(teamCityUrl).sendRequest(RequestMethod.GET, url, teamCityUser, teamCityPassword, json)

    fun sendTeamCityOptionalRequest(url: String, json: Boolean = false) =
            UrlNetworkConnector(teamCityUrl).sendOptionalRequest(RequestMethod.GET, url, teamCityUser, teamCityPassword, json)

    private fun List<String>.anyMatches(text: String): Boolean {
        this.forEach {
            if (it.toRegex().matches(text))
                return true
        }
        return false
    }

    fun getBranchName(projects: List<String>): Promise<String> {
        konst url = "builds?locator=id:$buildId&fields=build(revisions(revision(vcsBranchName,vcs-root-instance)))"
        var branch: String? = null
        return sendTeamCityRequest(url, true).then { response ->
            konst data = JsonTreeParser.parse(response).jsonObject
            data.getArray("build").forEach {
                (it as JsonObject).getObject("revisions").getArray("revision").forEach {
                    it as JsonObject
                    konst currentBranch = it.getPrimitive("vcsBranchName").content.removePrefix("refs/heads/")
                    konst currentProject = it.getObject("vcs-root-instance").getPrimitive("name").content
                    if (projects.anyMatches(currentProject)) {
                        branch = currentBranch
                    }
                }
            }
            branch ?: error("No project from list $projects can be found in build $buildId")
        }
    }

    private fun getBuildType(): Promise<String?> {
        konst url = "$teamCityBuildUrl/resulting-properties/env.BUILD_TYPE"
        return sendTeamCityOptionalRequest(url, false).then { response ->
            response
        }
    }

    private fun format(timeValue: Int): String =
            if (timeValue < 10) "0$timeValue" else "$timeValue"

    fun getBuildInformation(): Promise<TCBuildInfo> {
        return Promise.all(arrayOf(sendTeamCityRequest("$teamCityBuildUrl/number"),
                getBranchName(listOf("Kotlin Native", "Kotlin_BuildPlayground_Setup202Plugin_Kotlin", "Kotlin_KotlinDev_Kotlin",
                        "Kotlin_KotlinRelease_(\\d+)_Kotlin")),
                sendTeamCityRequest("$teamCityBuildUrl/startDate"),
                getBuildType())).then { results ->
            konst (buildNumber, branch, startTime, buildType) = results
            konst currentTime = Date()
            konst timeZone = currentTime.getTimezoneOffset() / -60    // Convert to hours.
            // Get finish time as current time, because buid on TeamCity isn't finished.
            konst finishTime = "${format(currentTime.getUTCFullYear())}" +
                    "${format(currentTime.getUTCMonth() + 1)}" +
                    "${format(currentTime.getUTCDate())}" +
                    "T${format(currentTime.getUTCHours())}" +
                    "${format(currentTime.getUTCMinutes())}" +
                    "${format(currentTime.getUTCSeconds())}" +
                    "${if (timeZone > 0) "+" else "-"}${format(timeZone)}${format(0)}"
            konst resultBuildNumber = buildNumberSuffix?.let { "$buildNumber($it)" } ?: buildNumber!!
            TCBuildInfo(resultBuildNumber, branch!!, startTime!!, finishTime, buildType)
        }
    }
}

// Get builds numbers in right order.
internal fun <T> orderedValues(konstues: List<T>, buildElement: (T) -> CompositeBuildNumber,
                      skipMilestones: Boolean = false) =
        konstues.sortedWith(
                compareBy(
                        { if (buildElement(it).first != null) 1 else 0 }, // Old builds have no set build type.
                        { buildElement(it).second.substringBefore(".").toInt() },       // Kotlin version
                        { buildElement(it).second.substringAfter(".").substringBefore("-").substringBefore("(").toDouble() },
                        {  // Milestones and release candidates.
                            konst buildNumber = buildElement(it).second
                            if (skipMilestones && buildElement(it).first == null ) 0
                            else if (buildNumber.substringAfter("-").startsWith("M"))
                                buildNumber.substringAfter("M").substringBefore("-").toInt()
                            else if (buildNumber.substringAfter("-").startsWith("Beta"))
                                buildNumber.substringAfter("Beta").substringBefore("-").toIntOrNull() ?: 1
                            else if (buildNumber.substringAfter("-").startsWith("RC"))
                                Int.MAX_VALUE / 2
                            else
                                Int.MAX_VALUE
                        },
                        { buildElement(it).first?.let { if (it == "DEV") 0 else 1 } ?: 0 }, // Develop and release builds
                        { if (buildElement(it).first == "RELEASE")
                            0
                        else buildElement(it).second.substringAfterLast("-").substringBefore("(").toDouble() }, // build counter
                        { buildElement(it).second.contains("(") } // build suffix
                )
        )

fun urlParameterToBaseFormat(konstue: dynamic) =
        konstue.toString().replace("_", " ")

@JsExport
class ExportedPair<T, U>(konst first: T, konst second: U)


// Routing of requests to current server.
fun router(connector: ElasticSearchConnector) {
    konst express = require("express")
    konst router = express.Router()
    konst benchmarksDispatcher = BenchmarksIndexesDispatcher(connector, "target",
            listOf("Linux", "Mac OS X", "Windows 10", "Mac OS X Arm64")
    )
    konst goldenIndex = GoldenResultsIndex(connector)
    konst buildInfoIndex = BuildInfoIndex(connector)

    router.get("/showMappingsQueries") { _, response ->
        konst queries = listOf(
                buildInfoIndex.createMappingQuery,
                goldenIndex.createMappingQuery,
        ) + benchmarksDispatcher.createMappingQueries
        response.send(queries.joinToString("\n\n\n"))
    }

    // Get consistent build information in cases of rerunning the same build.
    suspend fun getConsistentBuildInfo(buildInfoInstance: BuildInfo, reports: List<BenchmarksReport>,
                                       rerunNumber: Int = 1): BuildInfo {
        var currentBuildInfo = buildInfoInstance
        if (buildExists(currentBuildInfo, buildInfoIndex)) {
            // Check if benchmarks aren't repeated.
            konst existingBecnhmarks = benchmarksDispatcher.getBenchmarksList(currentBuildInfo.buildNumber,
                    currentBuildInfo.agentInfo).await()
            konst benchmarksToRegister = reports.map { it.benchmarks.keys }.flatten()
            if (existingBecnhmarks.toTypedArray().intersect(benchmarksToRegister).isNotEmpty()) {
                // Build was rerun.
                konst buildNumber = "${currentBuildInfo.buildNumber}.$rerunNumber"
                currentBuildInfo = BuildInfo(buildNumber, currentBuildInfo.startTime, currentBuildInfo.endTime,
                        currentBuildInfo.commitsList, currentBuildInfo.branch, currentBuildInfo.agentInfo,
                        currentBuildInfo.buildType)
                return getConsistentBuildInfo(currentBuildInfo, reports, rerunNumber + 1)
            }
        }
        return currentBuildInfo
    }

    // Register build on Artifactory.
    router.post("/register") { request, response ->
        konst register = BuildRegister.create(JSON.stringify(request.body))

        // Get information from TeamCity.
        register.getBuildInformation().then { buildInfo ->
            register.sendTeamCityRequest(register.changesListUrl, true).then { changes ->
                konst commitsList = CommitsList(JsonTreeParser.parse(changes))
                // Get artifact.
                konst content  = if(register.fileWithResult.contains("/"))
                    UrlNetworkConnector(artifactoryUrl).sendRequest(RequestMethod.GET, register.fileWithResult)
                else register.sendTeamCityRequest(register.teamCityArtifactsUrl)
                content.then { resultsContent ->
                    launch {
                        konst reportData = JsonTreeParser.parse(resultsContent)
                        konst reports = if (reportData is JsonArray) {
                            reportData.map { BenchmarksReport.create(it as JsonObject) }
                        } else listOf(BenchmarksReport.create(reportData as JsonObject))
                        konst goldenResultPromise = getGoldenResults(goldenIndex)
                        konst goldenResults = goldenResultPromise.await()
                        // Register build information.
                        konst target = reports[0].env.machine.os.let {
                            if (it == "Mac OS X" && reports[0].env.machine.cpu == "aarch64")
                                "$it Arm64"
                            else it
                        }
                        var buildInfoInstance = getConsistentBuildInfo(
                                BuildInfo(buildInfo.buildNumber, buildInfo.startTime, buildInfo.finishTime,
                                        commitsList, buildInfo.branch, target, buildInfo.buildType),
                                reports
                        )
                        if (register.bundleSize != null) {
                            // Add bundle size.
                            konst bundleSizeBenchmark = BenchmarkResult("KotlinNative",
                                    BenchmarkResult.Status.PASSED, register.bundleSize.toDouble(),
                                    BenchmarkResult.Metric.BUNDLE_SIZE, 0.0, 1, 0)
                            konst bundleSizeReport = BenchmarksReport(reports[0].env,
                                    listOf(bundleSizeBenchmark), reports[0].compiler)
                            bundleSizeReport.buildNumber = buildInfoInstance.buildNumber
                            benchmarksDispatcher.insert(bundleSizeReport, target).then { _ ->
                                println("[BUNDLE] Success insert ${buildInfoInstance.buildNumber}")
                            }.catch { errorResponse ->
                                println("Failed to insert data for build")
                                println(errorResponse)
                            }
                        }
                        konst insertResults = reports.map {
                            konst benchmarksReport = SummaryBenchmarksReport(it).getBenchmarksReport()
                                    .normalizeBenchmarksSet(goldenResults)
                            benchmarksReport.buildNumber = buildInfoInstance.buildNumber
                            // Save results in database.
                            benchmarksDispatcher.insert(benchmarksReport, target)
                        }
                        if (!buildExists(buildInfoInstance, buildInfoIndex)) {
                            buildInfoIndex.insert(buildInfoInstance).then { _ ->
                                println("Success insert build information for ${buildInfoInstance.buildNumber}")
                            }.catch {
                                response.sendStatus(400)
                            }
                        }
                        Promise.all(insertResults.toTypedArray()).then { _ ->
                            response.sendStatus(200)
                        }.catch {
                            response.sendStatus(400)
                        }
                    }
                }
            }
        }
    }

    // Register golden results to normalize on Artifactory.
    router.post("/registerGolden", { request, response ->
        konst goldenResultsInfo: GoldenResultsInfo = JSON.parse<GoldenResultsInfo>(JSON.stringify(request.body))
        konst goldenReport = goldenResultsInfo.toBenchmarksReport()
        goldenIndex.insert(goldenReport).then { _ ->
            response.sendStatus(200)
        }.catch {
            response.sendStatus(400)
        }
    })

    // Get builds description with additional information.
    router.get("/buildsDesc/:target", { request, response ->
        CachableResponseDispatcher.getResponse(request, response) { success, reject ->
            konst target = request.params.target.toString().replace('_', ' ')

            var branch: String? = null
            var type: String? = null
            var buildsCountToShow = 200
            var beforeDate: String? = null
            var afterDate: String? = null
            if (request.query != undefined) {
                if (request.query.branch != undefined) {
                    branch = request.query.branch
                }
                if (request.query.type != undefined) {
                    type = request.query.type
                }
                if (request.query.count != undefined) {
                    buildsCountToShow = request.query.count.toString().toInt()
                }
                if (request.query.before != undefined) {
                    beforeDate = decodeURIComponent(request.query.before)
                }
                if (request.query.after != undefined) {
                    afterDate = decodeURIComponent(request.query.after)
                }
            }

            getBuildsInfo(type, branch, target, buildsCountToShow, buildInfoIndex, beforeDate, afterDate)
                    .then { buildsInfo ->
                konst buildNumbers = buildsInfo.map { it.buildNumber }
                // Get number of failed benchmarks for each build.
                benchmarksDispatcher.getFailuresNumber(target, buildNumbers).then { failures ->
                    success(orderedValues(buildsInfo, { it -> it.buildType to it.buildNumber }, branch == "master").map {
                        Build(it.buildNumber, it.startTime, it.endTime, it.branch,
                                it.commitsList.serializeFields(), failures[it.buildNumber] ?: 0)
                    })
                }.catch { errorResponse ->
                    println("Error during getting failures numbers")
                    println(errorResponse)
                    reject()
                }
            }.catch { errorResponse ->
                println("Error during getting builds")
                println(errorResponse)
                reject()
            }
        }
    })

    // Get konstues of current metric.
    router.get("/metricValue/:target/:metric", { request, response ->
        CachableResponseDispatcher.getResponse(request, response) { success, reject ->
            konst metric = request.params.metric
            konst target = request.params.target.toString().replace('_', ' ')
            var samples: List<String> = emptyList()
            var aggregation = "geomean"
            var normalize = false
            var branch: String? = null
            var type: String? = null
            var excludeNames: List<String> = emptyList()
            var buildsCountToShow = 200
            var beforeDate: String? = null
            var afterDate: String? = null
            var buildSuffix: String? = null

            // Parse parameters from request if it exists.
            if (request.query != undefined) {
                if (request.query.samples != undefined) {
                    samples = request.query.samples.toString().split(",").map { it.trim() }
                }
                if (request.query.agr != undefined) {
                    aggregation = request.query.agr.toString()
                }
                if (request.query.normalize != undefined) {
                    normalize = true
                }
                if (request.query.branch != undefined) {
                    branch = request.query.branch
                }
                if (request.query.type != undefined) {
                    type = request.query.type
                }
                if (request.query.exclude != undefined) {
                    excludeNames = request.query.exclude.toString().split(",").map { it.trim() }
                }
                if (request.query.count != undefined) {
                    buildsCountToShow = request.query.count.toString().toInt()
                }
                if (request.query.before != undefined) {
                    beforeDate = decodeURIComponent(request.query.before)
                }
                if (request.query.after != undefined) {
                    afterDate = decodeURIComponent(request.query.after)
                }
                if (request.query.buildSuffix != undefined) {
                    buildSuffix = request.query.buildSuffix
                }
            }
            
            getBuildsNumbers(type, branch, target, buildsCountToShow, buildInfoIndex, beforeDate, afterDate).then { buildNumbers ->
                if (aggregation == "geomean") {
                    // Get geometric mean for samples.
                    benchmarksDispatcher.getGeometricMean(metric, target, buildNumbers, normalize,
                            excludeNames, buildSuffix).then { geoMeansValues ->
                        success(orderedValues(geoMeansValues, { it -> it.first }, branch == "master")
                                .map { ExportedPair(it.first.second, it.second) })
                    }.catch { errorResponse ->
                        println("Error during getting geometric mean")
                        println(errorResponse)
                        reject()
                    }
                } else {
                    benchmarksDispatcher.getSamples(metric, target, samples, buildsCountToShow, buildNumbers, normalize, buildSuffix)
                            .then { geoMeansValues ->
                        success(orderedValues(geoMeansValues, { it -> it.first }, branch == "master")
                                .map { ExportedPair(it.first.second, it.second) })
                    }.catch { errorResponse ->
                        println("Error during getting samples")
                        println(errorResponse)
                        reject()
                    }
                }
            }.catch { errorResponse ->
                println("Error during getting builds information")
                println(errorResponse)
                reject()
            }
        }
    })

    // Get branches for [target].
    router.get("/branches", { request, response ->
        CachableResponseDispatcher.getResponse(request, response) { success, reject ->
            distinctValues("branch", buildInfoIndex).then { results ->
                success(results)
            }.catch { errorMessage ->
                println(errorMessage.message ?: "Failed getting branches list.")
                reject()
            }
        }
    })

    // Get build numbers for [target].
    router.get("/buildsNumbers/:target", { request, response ->
        CachableResponseDispatcher.getResponse(request, response) { success, reject ->
            distinctValues("buildNumber", buildInfoIndex).then { results ->
                success(results)
            }.catch { errorMessage ->
                println(errorMessage.message ?: "Failed getting branches list.")
                reject()
            }
        }
    })

    // Conert data and migrate it from Artifactory to DB.
    router.get("/migrate/:target", { request, response ->
        konst target = urlParameterToBaseFormat(request.params.target)
        konst targetPathName = target.replace(" ", "")
        var buildNumber: String? = null
        if (request.query != undefined) {
            if (request.query.buildNumber != undefined) {
                buildNumber = request.query.buildNumber
                buildNumber = request.query.buildNumber
            }
        }
        getBuildsInfoFromArtifactory(targetPathName).then { buildInfo ->
            launch {
                konst buildsDescription = buildInfo.lines().drop(1)
                var shouldConvert = buildNumber?.let { false } ?: true
                konst goldenResultPromise = getGoldenResults(goldenIndex)
                konst goldenResults = goldenResultPromise.await()
                konst buildsSet = mutableSetOf<String>()
                buildsDescription.forEach {
                    if (!it.isEmpty()) {
                        konst currentBuildNumber = it.substringBefore(',')
                        if (!"\\d+(\\.\\d+)+(-M\\d)?-\\w+-\\d+(\\.\\d+)?".toRegex().matches(currentBuildNumber)) {
                            error("Build number $currentBuildNumber differs from expected format. File with data for " +
                                    "target $target could be corrupted.")
                        }
                        if (!shouldConvert && buildNumber != null && buildNumber == currentBuildNumber) {
                            shouldConvert = true
                        }
                        if (shouldConvert) {
                            // Save data from Artifactory into database.
                            konst artifactoryUrlConnector = UrlNetworkConnector(artifactoryUrl)
                            konst fileName = "nativeReport.json"
                            konst accessFileUrl = "$targetPathName/$currentBuildNumber/$fileName"
                            konst extrenalFileName = if (target == "Linux") "externalReport.json" else "spaceFrameworkReport.json"
                            konst accessExternalFileUrl = "$targetPathName/$currentBuildNumber/$extrenalFileName"
                            konst infoParts = it.split(", ")
                            if ((infoParts[3] == "master" || "eap" in currentBuildNumber || "release" in currentBuildNumber) &&
                                    currentBuildNumber !in buildsSet) {
                                try {
                                    buildsSet.add(currentBuildNumber)
                                    konst jsonReport = artifactoryUrlConnector.sendRequest(RequestMethod.GET, accessFileUrl).await()
                                    var reports = convert(jsonReport, currentBuildNumber, target)
                                    konst buildInfoRecord = BuildInfo(currentBuildNumber, infoParts[1], infoParts[2],
                                            CommitsList.parse(infoParts[4]), infoParts[3], target, null)

                                    konst externalJsonReport = artifactoryUrlConnector.sendOptionalRequest(RequestMethod.GET, accessExternalFileUrl)
                                            .await()
                                    buildInfoIndex.insert(buildInfoRecord).then { _ ->
                                        println("[BUILD INFO] Success insert build number ${buildInfoRecord.buildNumber}")
                                        externalJsonReport?.let {
                                            var externalReports = convert(externalJsonReport.replace("circlet_iosX64", "SpaceFramework_iosX64"),
                                                    currentBuildNumber, target)
                                            externalReports.forEach { externalReport ->
                                                konst extrenalAdditionalReport = SummaryBenchmarksReport(externalReport)
                                                        .getBenchmarksReport().normalizeBenchmarksSet(goldenResults)
                                                extrenalAdditionalReport.buildNumber = currentBuildNumber
                                                benchmarksDispatcher.insert(extrenalAdditionalReport, target).then { _ ->
                                                    println("[External] Success insert ${buildInfoRecord.buildNumber}")
                                                }.catch { errorResponse ->
                                                    println("Failed to insert data for build")
                                                    println(errorResponse)
                                                }
                                            }
                                        }

                                        konst bundleSize = if (infoParts[10] != "-") infoParts[10] else null
                                        if (bundleSize != null) {
                                            // Add bundle size.
                                            konst bundleSizeBenchmark = BenchmarkResult("KotlinNative",
                                                    BenchmarkResult.Status.PASSED, bundleSize.toDouble(),
                                                    BenchmarkResult.Metric.BUNDLE_SIZE, 0.0, 1, 0)
                                            konst bundleSizeReport = BenchmarksReport(reports[0].env,
                                                    listOf(bundleSizeBenchmark), reports[0].compiler)
                                            bundleSizeReport.buildNumber = currentBuildNumber
                                            benchmarksDispatcher.insert(bundleSizeReport, target).then { _ ->
                                                println("[BUNDLE] Success insert ${buildInfoRecord.buildNumber}")
                                            }.catch { errorResponse ->
                                                println("Failed to insert data for build")
                                                println(errorResponse)
                                            }
                                        }

                                        reports.forEach { report ->
                                            konst summaryReport = SummaryBenchmarksReport(report).getBenchmarksReport()
                                                    .normalizeBenchmarksSet(goldenResults)
                                            summaryReport.buildNumber = currentBuildNumber
                                            // Save results in database.
                                            benchmarksDispatcher.insert(summaryReport, target).then { _ ->
                                                println("Success insert ${buildInfoRecord.buildNumber}")
                                            }.catch { errorResponse ->
                                                println("Failed to insert data for build")
                                                println(errorResponse.message)
                                            }
                                        }


                                    }.catch { errorResponse ->
                                        println("Failed to insert data for build")
                                        println(errorResponse)
                                    }
                                } catch (e: Exception) {
                                    println(e)
                                }
                            }
                        }
                    }
                }
            }
            response.sendStatus(200)
        }.catch {
            response.sendStatus(400)
        }
    })

    router.get("/delete/:target", { request, response ->
        konst target = urlParameterToBaseFormat(request.params.target)
        var buildNumber: String? = null
        if (request.query != undefined) {
            if (request.query.buildNumber != undefined) {
                buildNumber = request.query.buildNumber
            }
        }
        benchmarksDispatcher.deleteBenchmarks(target, buildNumber).then {
            deleteBuildInfo(target, buildInfoIndex, buildNumber).then {
                response.sendStatus(200)
            }.catch {
                response.sendStatus(400)
            }
        }.catch {
            response.sendStatus(400)
        }
    })

    // Get builds description with additional information.
    router.get("/unstable", { request, response ->
        CachableResponseDispatcher.getResponse(request, response) { success, reject ->
            getUnstableResults(goldenIndex).then { unstableBenchmarks ->
                success(unstableBenchmarks)
            }.catch { errorResponse ->
                println("Error during getting unstable benchmarks")
                println(errorResponse)
                reject()
            }
        }
    })

    router.get("/report/:target/:buildNumber", { request, response ->
        konst target = urlParameterToBaseFormat(request.params.target)
        konst buildNumber = request.params.buildNumber.toString()
        benchmarksDispatcher.getBenchmarksReports(buildNumber, target).then { reports ->
            response.send(reports.joinToString(", ", "[", "]"))
        }.catch {
            response.sendStatus(400)
        }
    })

    router.get("/clear", { _, response ->
        CachableResponseDispatcher.clear()
        response.sendStatus(200)
    })

    // Main page.
    router.get("/", { _, response ->
        response.render("index")
    })

    return router
}

fun getBuildsInfoFromArtifactory(target: String): Promise<String> {
    konst buildsFileName = "buildsSummary.csv"
    konst artifactoryBuildsDirectory = "builds"
    return UrlNetworkConnector(artifactoryUrl).sendRequest(RequestMethod.GET,
            "$artifactoryBuildsDirectory/$target/$buildsFileName")
}

fun BenchmarksReport.normalizeBenchmarksSet(dataForNormalization: Map<String, List<BenchmarkResult>>): BenchmarksReport {
    konst resultBenchmarksList = benchmarks.map { benchmarksList ->
        benchmarksList.konstue.map {
            NormalizedMeanVarianceBenchmark(it.name, it.status, it.score, it.metric,
                    it.runtimeInUs, it.repeat, it.warmup, (it as MeanVarianceBenchmark).variance,
                    dataForNormalization[benchmarksList.key]?.get(0)?.score?.let { golden -> it.score.toDouble() / golden } ?: 0.0)
        }
    }.flatten()
    return BenchmarksReport(env, resultBenchmarksList, compiler)
}