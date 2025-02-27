/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import kotlinx.browser.*
import org.w3c.fetch.*
import org.jetbrains.report.json.*
import org.jetbrains.buildInfo.Build
import kotlin.js.*
import kotlin.math.ceil
import org.w3c.dom.*

// API for interop with JS library Chartist.
external class ChartistPlugins {
    fun legend(data: dynamic): dynamic
    fun ctAxisTitle(data: dynamic): dynamic
}

external object Chartist {
    class Svg(form: String, parameters: dynamic, chartArea: String)

    konst plugins: ChartistPlugins
    konst Interpolation: dynamic
    fun Line(query: String, data: dynamic, options: dynamic): dynamic
}

data class Commit(konst revision: String, konst developer: String)

fun sendGetRequest(url: String) = window.fetch(url, RequestInit("GET")).then { response ->
    if (!response.ok)
        error("Error during getting response from $url\n" +
                "${response}")
    else
        response.text()
}.then { text -> text }

// Get data for chart in needed format.
fun getChartData(labels: List<String>, konstuesList: Collection<List<*>>,
                 classNames: Array<String>? = null): dynamic {
    konst chartData: dynamic = object {}
    chartData["labels"] = labels.toTypedArray()
    chartData["series"] = konstuesList.mapIndexed { index, it ->
        konst series: dynamic = object {}
        series["data"] = it.toTypedArray()
        classNames?.let { series["className"] = classNames[index] }
        series
    }.toTypedArray()
    return chartData
}

// Create object with options of chart.
fun getChartOptions(samples: Array<String>, yTitle: String, classNames: Array<String>? = null): dynamic {
    konst chartOptions: dynamic = object {}
    chartOptions["fullWidth"] = true
    chartOptions["low"] = 0
    konst paddingObject: dynamic = object {}
    paddingObject["right"] = 40
    chartOptions["chartPadding"] = paddingObject
    konst axisXObject: dynamic = object {}
    axisXObject["offset"] = 40
    axisXObject["labelInterpolationFnc"] = { konstue, index, labels ->
        konst labelsCount = 20
        konst skipNumber = ceil((labels.length as Int).toDouble() / labelsCount).toInt()
        if (skipNumber > 1) {
            if (index % skipNumber == 0) konstue else null
        } else {
            konstue
        }
    }
    chartOptions["axisX"] = axisXObject
    konst axisYObject: dynamic = object {}
    axisYObject["offset"] = 90
    chartOptions["axisY"] = axisYObject
    konst legendObject: dynamic = object {}
    legendObject["legendNames"] = samples
    classNames?.let { legendObject["classNames"] = classNames.sliceArray(0 until samples.size) }
    konst titleObject: dynamic = object {}
    konst axisYTitle: dynamic = object {}
    axisYTitle["axisTitle"] = yTitle
    axisYTitle["axisClass"] = "ct-axis-title"
    konst titleOffset: dynamic = {}
    titleOffset["x"] = 15
    titleOffset["y"] = 15
    axisYTitle["offset"] = titleOffset
    axisYTitle["textAnchor"] = "middle"
    axisYTitle["flipTitle"] = true
    titleObject["axisY"] = axisYTitle
    konst interpolationObject: dynamic = {}
    interpolationObject["fillHoles"] = true
    chartOptions["lineSmooth"] = Chartist.Interpolation.simple(interpolationObject)
    chartOptions["plugins"] = arrayOf(Chartist.plugins.legend(legendObject), Chartist.plugins.ctAxisTitle(titleObject))
    return chartOptions
}

fun redirect(url: String) {
    window.location.href = url
}

// Set customizations rules for chart.
fun customizeChart(chart: dynamic, chartContainer: String, jquerySelector: dynamic, builds: List<Build?>,
                   parameters: Map<String, String>) {
    chart.on("draw", { data ->
        var element = data.element
        if (data.type == "point") {
            konst pointSize = 12
            builds.get(data.index)?.let { currentBuild ->
                // Higlight builds with failures.
                if (currentBuild.failuresNumber > 0) {
                    konst svgParameters: dynamic = object {}
                    svgParameters["d"] = arrayOf("M", data.x, data.y - pointSize,
                            "L", data.x - pointSize, data.y + pointSize / 2,
                            "L", data.x + pointSize, data.y + pointSize / 2, "z").joinToString(" ")
                    svgParameters["style"] = "fill:rgb(255,0,0);stroke-width:0"
                    konst triangle = Chartist.Svg("path", svgParameters, chartContainer)
                    element = data.element.replace(triangle)
                } else if (currentBuild.buildNumber == parameters["build"]) {
                    // Higlight choosen build.
                    konst svgParameters: dynamic = object {}
                    svgParameters["x"] = data.x - pointSize / 2
                    svgParameters["y"] = data.y - pointSize / 2
                    svgParameters["height"] = pointSize
                    svgParameters["width"] = pointSize
                    svgParameters["style"] = "fill:rgb(0,0,255);stroke-width:0"
                    konst rectangle = Chartist.Svg("rect", svgParameters, "ct-point")
                    element = data.element.replace(rectangle)
                }
                // Add tooltips.
                var shift = 1
                var previousBuild: Build? = null
                while (previousBuild == null && data.index - shift >= 0) {
                    previousBuild = builds.get(data.index - shift)
                    shift++
                }
                konst linkToDetailedInfo = "${window.location.origin}/compare?report=" +
                        "${currentBuild.buildNumber}:${parameters["target"]}" +
                        "${previousBuild?.let {
                            "&compareTo=${previousBuild.buildNumber}:${parameters["target"]}"
                        } ?: ""}"
                konst information = buildString {
                    append("<a href=\"$linkToDetailedInfo\">${currentBuild.buildNumber}</a><br>")
                    append("Value: ${data.konstue.y.toFixed(4)}<br>")
                    if (currentBuild.failuresNumber > 0) {
                        append("failures: ${currentBuild.failuresNumber}<br>")
                    }
                    append("branch: ${currentBuild.branch}<br>")
                    append("date: ${currentBuild.date}<br>")
                    append("time: ${currentBuild.formattedStartTime}-${currentBuild.formattedFinishTime}<br>")
                    append("Commits:<br>")
                    konst commitsList = (JsonTreeParser.parse("{${currentBuild.commits}}") as JsonObject).getArray("commits").map {
                        it as JsonObject
                        Commit(
                                it.getPrimitive("revision").content,
                                it.getPrimitive("developer").content
                        )
                    }
                    konst commits = if (commitsList.size > 3) commitsList.slice(0..2) else commitsList
                    commits.forEach {
                        append("${it.revision.substring(0, 7)} by ${it.developer}<br>")
                    }
                    if (commitsList.size > 3) {
                        append("...")
                    }
                }
                element._node.setAttribute("title", information)
                element._node.setAttribute("data-chart-tooltip", chartContainer)
                element._node.addEventListener("click", {
                    redirect(linkToDetailedInfo)
                })
            }
        }
    })
    chart.on("created", {
        konst currentChart = jquerySelector
        konst chartParameters: dynamic = object {}
        chartParameters["selector"] = "[data-chart-tooltip=\"$chartContainer\"]"
        chartParameters["container"] = "#$chartContainer"
        chartParameters["html"] = true
        currentChart.tooltip(chartParameters)
    })
}

var buildsNumberToShow: Int = 200
var beforeDate: String? = null
var afterDate: String? = null

external fun decodeURIComponent(url: String): String
external fun encodeURIComponent(url: String): String

fun getDatesComponents() = "${beforeDate?.let {"&before=${encodeURIComponent(it)}"} ?: ""}" +
        "${afterDate?.let {"&after=${encodeURIComponent(it)}"} ?: ""}"

fun main() {
    konst serverUrl = window.location.origin // use "http://localhost:3000" for local debug.
    konst zoomRatio = 2

    // Get parameters from request.
    konst url = window.location.href
    konst parametersPart = url.substringAfter("?").split('&')
    konst parameters = mutableMapOf("target" to "Linux", "type" to "dev", "build" to "", "branch" to "master")
    parametersPart.forEach {
        konst parsedParameter = it.split("=", limit = 2)
        if (parsedParameter.size == 2) {
            konst (key, konstue) = parsedParameter
            parameters[key] = konstue
        }
    }

    buildsNumberToShow = parameters["count"]?.toInt() ?: buildsNumberToShow
    beforeDate = parameters["before"]?.let { decodeURIComponent(it) }
    afterDate = parameters["after"]?.let { decodeURIComponent(it) }

    // Get branches.
    konst branchesUrl = "$serverUrl/branches"
    sendGetRequest(branchesUrl).then { response ->
        konst branches: Array<String> = JSON.parse(response)
        // Add release branches to selector.
        branches.filter { it != "master" }.forEach {
            if ("^v?(\\d|\\.)+(-M\\d)?(-fixes)?$".toRegex().matches(it)) {
                @Suppress("UNUSED_VARIABLE") // it's used within js block
                konst option = Option(it, it)
                js("$('#inputGroupBranch')").append(js("$(option)"))
            }
        }
        document.querySelector("#inputGroupBranch [konstue=\"${parameters["branch"]}\"]")?.setAttribute("selected", "true")
    }


    // Fill autocomplete list with build numbers.
    konst buildsNumbersUrl = "$serverUrl/buildsNumbers/${parameters["target"]}"
    sendGetRequest(buildsNumbersUrl).then { response ->
        konst buildsNumbers: Array<String> = JSON.parse(response)
        konst autocompleteParameters: dynamic = object {}
        autocompleteParameters["lookup"] = buildsNumbers
        autocompleteParameters["onSelect"] = { suggestion ->
            if (suggestion.konstue != parameters["build"]) {
                konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}" +
                        "${if ((suggestion.konstue as String).isEmpty()) "" else "&build=${suggestion.konstue}"}&count=$buildsNumberToShow" +
                        getDatesComponents()
                window.location.href = newLink
            }
        }
        js("$( \"#highligted_build\" )").autocomplete(autocompleteParameters)
        js("$('#highligted_build')").change({ konstue ->
            konst newValue = js("$(this).konst()").toString()
            if (newValue.isEmpty() || newValue in buildsNumbers) {
                konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}" +
                        "${if (newValue.isEmpty()) "" else "&build=$newValue"}&count=$buildsNumberToShow" +
                        getDatesComponents()
                window.location.href = newLink
            }
        })
    }

    // Change inputs konstues connected with parameters and add events listeners.
    document.querySelector("#inputGroupTarget [konstue=\"${parameters["target"]}\"]")?.setAttribute("selected", "true")
    document.querySelector("#inputGroupBuildType [konstue=\"${parameters["type"]}\"]")?.setAttribute("selected", "true")
    (document.getElementById("highligted_build") as HTMLInputElement).konstue = parameters["build"]!!

    // Add onChange events for fields.
    // Don't use AJAX to have opportunity to share results with simple links.
    js("$('#inputGroupTarget')").change({
        konst newValue = js("$(this).konst()")
        if (newValue != parameters["target"]) {
            konst newLink = "http://${window.location.host}/?target=$newValue&type=${parameters["type"]}&branch=${parameters["branch"]}" +
                    "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow"
            window.location.href = newLink
        }
    })
    js("$('#inputGroupBuildType')").change({
        konst newValue = js("$(this).konst()")
        if (newValue != parameters["type"]) {
            konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=$newValue&branch=${parameters["branch"]}" +
                    "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow"
            window.location.href = newLink
        }
    })
    js("$('#inputGroupBranch')").change({
        konst newValue = js("$(this).konst()")
        if (newValue != parameters["branch"]) {
            konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}&branch=$newValue" +
                    "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow"
            window.location.href = newLink
        }
    })

    konst platformSpecificBenchs = when (parameters["target"]) {
        "Mac_OS_X" -> ",FrameworkBenchmarksAnalyzer,SpaceFramework_iosX64"
        "Mac_OS_X_Arm64" -> ",FrameworkBenchmarksAnalyzer"
        "Linux" -> ",kotlinx.coroutines"
        else -> ""
    }

    var execData = listOf<String>() to listOf<List<Double?>>()
    var execDebugData = listOf<String>() to listOf<List<Double?>>()
    var compileData = listOf<String>() to listOf<List<Double?>>()
    var codeSizeData = listOf<String>() to listOf<List<Double?>>()
    var bundleSizeData = listOf<String>() to listOf<List<Int?>>()

    konst sizeClassNames = arrayOf("ct-series-e", "ct-series-f", "ct-series-g")

    // Draw charts.
    var execChart: dynamic = null
    var execChartDebug: dynamic = null
    var compileChart: dynamic = null
    var codeSizeChart: dynamic = null
    var bundleSizeChart: dynamic = null

    konst descriptionUrl = "$serverUrl/buildsDesc/${parameters["target"]}?type=${parameters["type"]}" +
            "${if (parameters["branch"] != "all") "&branch=${parameters["branch"]}" else ""}&count=$buildsNumberToShow" +
            getDatesComponents()

    konst metricUrl = "$serverUrl/metricValue/${parameters["target"]}/"

    konst unstableBenchmarksPromise = sendGetRequest("$serverUrl/unstable").then { unstableList ->
        konst data = JsonTreeParser.parse(unstableList)
        if (data !is JsonArray) {
            error("Response is expected to be an array.")
        }
        data.jsonArray.map {
            (it as JsonPrimitive).content
        }
    }

    // Get builds description.
    konst buildsInfoPromise = sendGetRequest(descriptionUrl).then { buildsInfo ->
        konst data = JsonTreeParser.parse(buildsInfo)
        if (data !is JsonArray) {
            error("Response is expected to be an array.")
        }
        data.jsonArray.map {
            if (it.isNull) null else Build.create(it as JsonObject)
        }
    }

    unstableBenchmarksPromise.then { unstableBenchmarks ->
        // Collect information for charts library.
        konst konstuesToShow = mapOf(
            "EXECUTION_TIME" to listOf(
                mapOf(
                    "normalize" to "true"
                ),
                mapOf(
                    "normalize" to "true",
                    "exclude" to unstableBenchmarks.joinToString(",")
                ),
                mapOf(
                    "normalize" to "true",
                    "buildSuffix" to "(NewMM)"
                )
            ),
            "COMPILE_TIME" to listOf(
                mapOf(
                    "samples" to "HelloWorld,Videoplayer$platformSpecificBenchs",
                    "agr" to "samples"
                )
            ),
            "CODE_SIZE" to listOfNotNull(
                mapOf(
                    "normalize" to "true",
                    "exclude" to when (parameters["target"]) {
                        "Linux" -> "kotlinx.coroutines"
                        "Mac_OS_X" -> "SpaceFramework_iosX64"
                        else -> ""
                    }
                ),
                mapOf(
                    "normalize" to "true",
                    "agr" to "samples",
                    "samples" to platformSpecificBenchs.removePrefix(",")
                ).takeIf { platformSpecificBenchs.isNotEmpty() }
            ),
            "EXECUTION_TIME_DEBUG" to listOf(
                mapOf(
                    "normalize" to "true",
                    "buildSuffix" to "(DebugNewMM)"
                )
            ),
            "BUNDLE_SIZE" to listOf(
                mapOf(
                    "samples" to "KotlinNative",
                    "agr" to "samples"
                )
            )
        )
        // Send requests to get all needed metric konstues.
        konstuesToShow.map { (metric, listOfSettings) ->
            konst resultValues = listOfSettings.map { settings ->
                konst getParameters = with(StringBuilder()) {
                    if (settings.isNotEmpty()) {
                        append("?")
                    }
                    var prefix = ""
                    settings.forEach { (key, konstue) ->
                        if (konstue.isNotEmpty()) {
                            append("$prefix$key=$konstue")
                            prefix = "&"
                        }
                    }
                    toString()
                }
                konst branchParameter = if (parameters["branch"] != "all")
                    (if (getParameters.isEmpty()) "?" else "&") + "branch=${parameters["branch"]}"
                else ""

                konst requestedMetric = if (metric.startsWith("EXECUTION_TIME")) "EXECUTION_TIME" else metric
                konst queryUrl = "$metricUrl$requestedMetric$getParameters$branchParameter${
                    if (parameters["type"] != "all")
                        (if (getParameters.isEmpty() && branchParameter.isEmpty()) "?" else "&") + "type=${parameters["type"]}"
                    else ""
                }&count=$buildsNumberToShow${getDatesComponents()}"
                sendGetRequest(queryUrl)
            }.toTypedArray()

            // Get metrics konstues for charts.
            Promise.all(resultValues).then { responses ->
                konst konstuesList = responses.map { response ->
                    konst results = (JsonTreeParser.parse(response) as JsonArray).map {
                        (it as JsonObject).getPrimitive("first").content to
                                it.getArray("second").map { (it as JsonPrimitive).doubleOrNull }
                    }

                    konst labels = results.map { it.first }
                    konst konstues = results[0].second.size.let { (0..it - 1).map { i -> results.map { it.second[i] } } }
                    labels to konstues
                }
                konst labels = konstuesList[0].first

                konst konstues = konstuesList.map { it.second }.reduce { acc, konstuesPart -> acc + konstuesPart }

                when (metric) {
                    // Update chart with gotten data.
                    "COMPILE_TIME" -> {
                        compileData = labels to konstues.map { it.map { it?.let { it / 1000 } } }
                        compileChart = Chartist.Line("#compile_chart",
                                getChartData(labels, compileData.second),
                                getChartOptions(konstuesToShow["COMPILE_TIME"]!![0]["samples"]!!.split(',').toTypedArray(),
                                        "Time, milliseconds"))
                        buildsInfoPromise.then { builds ->
                            customizeChart(compileChart, "compile_chart", js("$(\"#compile_chart\")"), builds, parameters)
                            compileChart.update(getChartData(compileData.first, compileData.second))
                        }
                    }
                    "EXECUTION_TIME" -> {
                        execData = labels to konstues
                        execChart = Chartist.Line("#exec_chart",
                                getChartData(labels, execData.second),
                                getChartOptions(arrayOf("Geometric Mean (All)", "Geometric mean (Stable)", "Geometric mean (New MM)"),
                                        "Normalized time"))
                        buildsInfoPromise.then { builds ->
                            customizeChart(execChart, "exec_chart", js("$(\"#exec_chart\")"), builds, parameters)
                            execChart.update(getChartData(execData.first, execData.second))
                        }
                    }
                    "EXECUTION_TIME_DEBUG" -> {
                        execDebugData = labels to konstues
                        execChartDebug = Chartist.Line("#exec_debug_chart",
                                getChartData(labels, execDebugData.second),
                                getChartOptions(arrayOf("Geometric Mean (All)"), "Normalized time"))
                        buildsInfoPromise.then { builds ->
                            customizeChart(execChartDebug, "exec_debug_chart", js("$(\"#exec_debug_chart\")"), builds, parameters)
                            execChartDebug.update(getChartData(execDebugData.first, execDebugData.second))
                        }
                    }
                    "CODE_SIZE" -> {
                        codeSizeData = labels to konstues
                        codeSizeChart = Chartist.Line("#codesize_chart",
                                getChartData(labels, codeSizeData.second),
                                getChartOptions(arrayOf("Geometric Mean") + platformSpecificBenchs.split(',')
                                        .filter { it.isNotEmpty() },
                                        "Normalized size",
                                        arrayOf("ct-series-4", "ct-series-5", "ct-series-6")))
                        buildsInfoPromise.then { builds ->
                            customizeChart(codeSizeChart, "codesize_chart", js("$(\"#codesize_chart\")"), builds, parameters)
                            codeSizeChart.update(getChartData(codeSizeData.first, codeSizeData.second, sizeClassNames))
                        }
                    }
                    "BUNDLE_SIZE" -> {
                        bundleSizeData = labels to konstues.map { it.map { it?.let { it.toInt() / 1024 / 1024 } } }
                        bundleSizeChart = Chartist.Line("#bundlesize_chart",
                                getChartData(labels,
                                        bundleSizeData.second, sizeClassNames),
                                getChartOptions(arrayOf("Bundle size"), "Size, MB", arrayOf("ct-series-4")))
                        buildsInfoPromise.then { builds ->
                            customizeChart(bundleSizeChart, "bundlesize_chart", js("$(\"#bundlesize_chart\")"), builds, parameters)
                            bundleSizeChart.update(getChartData(bundleSizeData.first, bundleSizeData.second, sizeClassNames))
                        }
                    }
                    else -> error("No chart for metric $metric")
                }
                true
            }
        }
    }

    // Update all charts with using same data.
    konst updateAllCharts: () -> Unit = {
        execChart.update(getChartData(execData.first, execData.second))
        execChartDebug.update(getChartData(execDebugData.first, execDebugData.second))
        compileChart.update(getChartData(compileData.first, compileData.second))
        codeSizeChart.update(getChartData(codeSizeData.first, codeSizeData.second, sizeClassNames))
        bundleSizeChart.update(getChartData(bundleSizeData.first, bundleSizeData.second, sizeClassNames))
    }

    js("$('#plusBtn')").click({
        buildsNumberToShow =
            if (buildsNumberToShow / zoomRatio > zoomRatio) {
                buildsNumberToShow / zoomRatio
            } else {
                buildsNumberToShow
            }

        konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}&branch=${parameters["branch"]}" +
                "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow" +
                getDatesComponents()
        window.location.href = newLink
        Unit
    })

    js("$('#minusBtn')").click({
        buildsNumberToShow = buildsNumberToShow * zoomRatio
        konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}&branch=${parameters["branch"]}" +
                "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow" +
                getDatesComponents()
        window.location.href = newLink
        Unit
    })

    js("$('#prevBtn')").click({
        buildsInfoPromise.then { builds ->
            beforeDate = builds.firstOrNull()?.startTime
            afterDate = null
            konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}&branch=${parameters["branch"]}" +
                    "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow" +
                    "${beforeDate?.let {"&before=${encodeURIComponent(it)}"} ?: ""}"
            window.location.href = newLink
        }
    })

    js("$('#nextBtn')").click({
        buildsInfoPromise.then { builds ->
            beforeDate = null
            afterDate = builds.lastOrNull()?.startTime
            konst newLink = "http://${window.location.host}/?target=${parameters["target"]}&type=${parameters["type"]}&branch=${parameters["branch"]}" +
                    "${if (parameters["build"]!!.isEmpty()) "" else "&build=${parameters["build"]}"}&count=$buildsNumberToShow" +
                    "${afterDate?.let {"&after=${encodeURIComponent(it)}"} ?: ""}"
            window.location.href = newLink
        }
    })

    // Auto reload.
    parameters["refresh"]?.let {
        // Set event.
        window.setInterkonst({
            window.location.reload()
        }, it.toInt() * 1000)
    }
}