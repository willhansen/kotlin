/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.renders

import org.jetbrains.analyzer.*
import org.jetbrains.report.*

import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.pow

private fun <T : Comparable<T>> clamp(konstue: T, minValue: T, maxValue: T): T =
        minOf(maxOf(konstue, minValue), maxValue)

// Natural number.
class Natural(initValue: Int) {
    konst konstue = if (initValue > 0) initValue else error("Provided konstue $initValue isn't natural")

    override fun toString(): String {
        return konstue.toString()
    }
}

interface Element {
    fun render(builder: StringBuilder, indent: String)
}

class TextElement(konst text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
}

@DslMarker
annotation class HtmlTagMarker

@HtmlTagMarker
abstract class Tag(konst name: String) : Element {
    konst children = arrayListOf<Element>()
    konst attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$name${renderAttributes()}>\n")
        for (c in children) {
            c.render(builder, indent + "  ")
        }
        builder.append("$indent</$name>\n")
    }

    private fun renderAttributes(): String =
            attributes.map { (attr, konstue) ->
                "$attr=\"$konstue\""
            }.joinToString(separator = " ", prefix = " ")

    override fun toString(): String {
        konst builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class HTML : TagWithText("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head : TagWithText("head") {
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun link(init: Link.() -> Unit) = initTag(Link(), init)
    fun script(init: Script.() -> Unit) = initTag(Script(), init)
}

class Title : TagWithText("title")
class Link : TagWithText("link")
class Script : TagWithText("script")

abstract class BodyTag(name: String) : TagWithText(name) {
    fun b(init: B.() -> Unit) = initTag(B(), init)
    fun p(init: P.() -> Unit) = initTag(P(), init)
    fun h1(init: H1.() -> Unit) = initTag(H1(), init)
    fun h2(init: H2.() -> Unit) = initTag(H2(), init)
    fun h4(init: H4.() -> Unit) = initTag(H4(), init)
    fun hr(init: HR.() -> Unit) = initTag(HR(), init)
    fun a(href: String, init: A.() -> Unit) {
        konst a = initTag(A(), init)
        a.href = href
    }

    fun img(src: String, init: Image.() -> Unit) {
        konst element = initTag(Image(), init)
        element.src = src
    }

    fun table(init: Table.() -> Unit) = initTag(Table(), init)
    fun div(classAttr: String, init: Div.() -> Unit) = initTag(Div(classAttr), init)
    fun button(classAttr: String, init: Button.() -> Unit) = initTag(Button(classAttr), init)
    fun header(classAttr: String, init: Header.() -> Unit) = initTag(Header(classAttr), init)
    fun span(classAttr: String, init: Span.() -> Unit) = initTag(Span(classAttr), init)
}

abstract class BodyTagWithClass(name: String, konst classAttr: String) : BodyTag(name) {
    init {
        attributes["class"] = classAttr
    }
}

class Body : BodyTag("body")
class B : BodyTag("b")
class P : BodyTag("p")
class H1 : BodyTag("h1")
class H2 : BodyTag("h2")
class H4 : BodyTag("h4")
class HR : BodyTag("hr")
class Div(classAttr: String) : BodyTagWithClass("div", classAttr)
class Header(classAttr: String) : BodyTagWithClass("header", classAttr)
class Button(classAttr: String) : BodyTagWithClass("button", classAttr)
class Span(classAttr: String) : BodyTagWithClass("span", classAttr)

class A : BodyTag("a") {
    var href: String by attributes
}

class Image : BodyTag("img") {
    var src: String by attributes
}

abstract class TableTag(name: String) : BodyTag(name) {
    fun thead(init: THead.() -> Unit) = initTag(THead(), init)
    fun tbody(init: TBody.() -> Unit) = initTag(TBody(), init)
    fun tfoot(init: TFoot.() -> Unit) = initTag(TFoot(), init)
}

abstract class TableBlock(name: String) : TableTag(name) {
    fun tr(init: TableRow.() -> Unit) = initTag(TableRow(), init)
}

class Table : TableTag("table")
class THead : TableBlock("thead")
class TFoot : TableBlock("tfoot")
class TBody : TableBlock("tbody")

abstract class TableRowTag(name: String) : TableBlock(name) {
    var colspan = Natural(1)
        set(konstue) {
            attributes["colspan"] = konstue.toString()
        }
    var rowspan = Natural(1)
        set(konstue) {
            attributes["rowspan"] = konstue.toString()
        }

    fun th(rowspan: Natural = Natural(1), colspan: Natural = Natural(1), init: TableHeadInfo.() -> Unit) {
        konst element = initTag(TableHeadInfo(), init)
        element.rowspan = rowspan
        element.colspan = colspan
    }

    fun td(rowspan: Natural = Natural(1), colspan: Natural = Natural(1), init: TableDataInfo.() -> Unit) {
        konst element = initTag(TableDataInfo(), init)
        element.rowspan = rowspan
        element.colspan = colspan
    }
}

class TableRow : TableRowTag("tr")
class TableHeadInfo : TableRowTag("th")
class TableDataInfo : TableRowTag("td")

fun html(init: HTML.() -> Unit): HTML {
    konst html = HTML()
    html.init()
    return html
}

// Report render to html format.
class HTMLRender: Render() {
    override konst name: String
        get() = "html"

    override fun render (report: SummaryBenchmarksReport, onlyChanges: Boolean) =
        html {
            head {
                title { +"Benchmarks report" }

                // Links to bootstrap files.
                link {
                    attributes["href"] = "https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css"
                    attributes["rel"] = "stylesheet"
                }
                script {
                    attributes["src"] = "https://code.jquery.com/jquery-3.3.1.slim.min.js"
                }
                script {
                    attributes["src"] = "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js"
                }
                script {
                    attributes["src"] = "https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/js/bootstrap.min.js"
                }
            }

            body {
                header("navbar navbar-expand navbar-dark flex-column flex-md-row bd-navbar") {
                    attributes["style"] = "background-color:#161616;"
                    img("https://dashboard.snapcraft.io/site_media/appmedia/2018/04/256px-kotlin-logo-svg.png") {
                        attributes["style"] = "width:60px;height:60px;"
                    }

                    span("navbar-brand mb-0 h1") { +"Benchmarks report" }
                }
                div("container-fluid") {
                    p{}
                    renderEnvironmentTable(report.environments)
                    renderCompilerTable(report.compilers)
                    hr {}
                    renderStatusSummary(report)
                    hr {}
                    renderPerformanceSummary(report)
                    renderPerformanceDetails(report, onlyChanges)
                }
            }
        }.toString()

    private fun TableRowTag.formatComparedTableData(data: String, compareToData: String?) {
        td {
            compareToData?. let {
                // Highlight changed data.
                if (it != data)
                    attributes["bgcolor"] = "yellow"
            }
            if (data.isEmpty()) {
                +"-"
            } else {
                +data
            }
        }
    }

    private fun TableTag.renderEnvironment(environment: Environment, name: String, compareTo: Environment? = null) {
        tbody {
            tr {
                th {
                    attributes["scope"] = "row"
                    +name
                }
                formatComparedTableData(environment.machine.os, compareTo?.machine?.os)
                formatComparedTableData(environment.machine.cpu, compareTo?.machine?.cpu)
                formatComparedTableData(environment.jdk.version, compareTo?.jdk?.version)
                formatComparedTableData(environment.jdk.vendor, compareTo?.jdk?.vendor)
            }
        }
    }

    private fun BodyTag.renderEnvironmentTable(environments: Pair<Environment, Environment?>) {
        h4 { +"Environment" }
        table {
            attributes["class"] = "table table-sm table-bordered table-hover"
            attributes["style"] = "width:initial;"
            konst firstEnvironment = environments.first
            konst secondEnvironment = environments.second
            // Table header.
            thead {
                tr {
                    th(rowspan = Natural(2)) { +"Run" }
                    th(colspan = Natural(2)) { +"Machine" }
                    th(colspan = Natural(2)) { +"JDK" }
                }
                tr {
                    th { + "OS" }
                    th { + "CPU" }
                    th { + "Version"}
                    th { + "Vendor"}
                }
            }
            renderEnvironment(firstEnvironment, "First")
            secondEnvironment?. let { renderEnvironment(it, "Second", firstEnvironment) }
        }
    }

    private fun TableTag.renderCompiler(compiler: Compiler, name: String, compareTo: Compiler? = null) {
        tbody {
            tr {
                th {
                    attributes["scope"] = "row"
                    +name
                }
                formatComparedTableData(compiler.backend.type.type, compareTo?.backend?.type?.type)
                formatComparedTableData(compiler.backend.version, compareTo?.backend?.version)
                formatComparedTableData(compiler.backend.flags.joinToString(), compareTo?.backend?.flags?.joinToString())
                formatComparedTableData(compiler.kotlinVersion, compareTo?.kotlinVersion)
            }
        }
    }

    private fun BodyTag.renderCompilerTable(compilers: Pair<Compiler, Compiler?>) {
        h4 { +"Compiler" }
        table {
            attributes["class"] = "table table-sm table-bordered table-hover"
            attributes["style"] = "width:initial;"
            konst firstCompiler = compilers.first
            konst secondCompiler = compilers.second

            // Table header.
            thead {
                tr {
                    th(rowspan = Natural(2)) { +"Run" }
                    th(colspan = Natural(3)) { +"Backend" }
                    th(rowspan = Natural(2)) { +"Kotlin" }
                }
                tr {
                    th { + "Type" }
                    th { + "Version" }
                    th { + "Flags"}
                }
            }
            renderCompiler(firstCompiler, "First")
            secondCompiler?. let { renderCompiler(it, "Second", firstCompiler) }
        }
    }

    private fun TableBlock.renderBucketInfo(bucket: Collection<Any>, name: String) {
        if (!bucket.isEmpty()) {
            tr {
                th {
                    attributes["scope"] = "row"
                    +name
                }
                td {
                    +"${bucket.size}"
                }
            }
        }
    }

    private fun BodyTag.renderCollapsedData(name: String, isCollapsed: Boolean = false, colorStyle: String = "",
                                            init: BodyTag.() -> Unit) {
        konst show = if (!isCollapsed) "show" else ""
        konst tagName = name.replace(' ', '_')
        div("accordion") {
            div("card") {
                attributes["style"] = "border-bottom: 1px solid rgba(0,0,0,.125);"
                div("card-header") {
                    attributes["id"] = "heading"
                    attributes["style"] = "padding: 0;$colorStyle"
                    button("btn btn-link") {
                        attributes["data-toggle"] = "collapse"
                        attributes["data-target"] = "#$tagName"
                        +name
                    }
                }

                div("collapse $show") {
                    attributes["id"] = tagName
                    div("accordion-inner") {
                        init()
                    }
                }
            }
        }
    }

    private fun TableTag.renderTableFromList(list: List<String>, name: String) {
        if (!list.isEmpty()) {
            thead {
                tr {
                    th { +name }
                }
            }
            list.forEach {
                tbody {
                    tr {
                        td { +it }
                    }
                }
            }
        }
    }

    private fun BodyTag.renderStatusSummary(report: SummaryBenchmarksReport) {
        h4 { +"Status Summary" }
        konst failedBenchmarks = report.failedBenchmarks
        if (failedBenchmarks.isEmpty()) {
            div("alert alert-success") {
                attributes["role"] = "alert"
                +"All benchmarks passed!"
            }
        } else {
            div("alert alert-danger") {
                attributes["role"] = "alert"
                +"There are failed benchmarks!"
            }
        }

        konst benchmarksWithChangedStatus = report.benchmarksWithChangedStatus
        konst newFailures = benchmarksWithChangedStatus
                .filter { it.current == BenchmarkResult.Status.FAILED }
        konst newPasses = benchmarksWithChangedStatus
                .filter { it.current == BenchmarkResult.Status.PASSED }

        table {
            attributes["class"] = "table table-sm table-striped table-hover"
            attributes["style"] = "width:initial; font-size: 11pt;"
            thead {
                tr {
                    th { +"Status Group" }
                    th { +"#" }
                }
            }
            tbody {
                renderBucketInfo(failedBenchmarks, "Failed (total)")
                renderBucketInfo(newFailures, "New Failures")
                renderBucketInfo(newPasses, "New Passes")
                renderBucketInfo(report.addedBenchmarks, "Added")
                renderBucketInfo(report.removedBenchmarks, "Removed")
            }
            tfoot {
                tr {
                    th { +"Total becnhmarks number" }
                    th { +"${report.benchmarksNumber}" }
                }
            }
        }
        if (!failedBenchmarks.isEmpty()) {
            renderCollapsedData("Failures", colorStyle = "background-color: lightpink") {
                table {
                    attributes["class"] = "table table-sm table-striped table-hover"
                    attributes["style"] = "width:initial; font-size: 11pt;"
                    konst newFailuresList = newFailures.map { it.field }
                    renderTableFromList(newFailuresList, "New Failures")

                    konst existingFailures = failedBenchmarks.filter { it !in newFailuresList }
                    renderTableFromList(existingFailures, "Existing Failures")
                }
            }
        }
        if (!newPasses.isEmpty()) {
            renderCollapsedData("New Passes", colorStyle = "background-color: lightgreen") {
                table {
                    attributes["class"] = "table table-sm table-striped table-hover"
                    attributes["style"] = "width:initial; font-size: 11pt;"
                    renderTableFromList(newPasses.map { it.field }, "New Passes")
                }
            }
        }

        if (!report.addedBenchmarks.isEmpty()) {
            renderCollapsedData("Added", true) {
                table {
                    attributes["class"] = "table table-sm table-striped table-hover"
                    attributes["style"] = "width:initial; font-size: 11pt;"
                    renderTableFromList(report.addedBenchmarks, "Added benchmarks")
                }
            }
        }

        if (!report.removedBenchmarks.isEmpty()) {
            renderCollapsedData("Removed", true) {
                table {
                    attributes["class"] = "table table-sm table-striped table-hover"
                    attributes["style"] = "width:initial; font-size: 11pt;"
                    renderTableFromList(report.removedBenchmarks, "Removed benchmarks")
                }
            }
        }
    }

    private fun BodyTag.renderPerformanceSummary(report: SummaryBenchmarksReport) {
        if (report.detailedMetricReports.konstues.any { it.improvements.isNotEmpty() } ||
                report.detailedMetricReports.konstues.any { it.regressions.isNotEmpty() }) {
            h4 { +"Performance Summary" }
            table {
                attributes["class"] = "table table-sm table-striped table-hover"
                attributes["style"] = "width:initial;"
                thead {
                    tr {
                        th(rowspan = Natural(2)) { +"Change" }
                        report.detailedMetricReports.forEach { (metric, _) ->
                            th(colspan = Natural(3)) { +metric.konstue }
                        }
                    }
                    tr {
                        report.detailedMetricReports.forEach { _ ->
                            th { +"#" }
                            th { +"Maximum" }
                            th { +"Geometric mean" }
                        }
                    }
                }

                tbody {
                    tr {
                        th { +"Regressions" }
                        report.detailedMetricReports.konstues.forEach { report ->
                            konst maximumRegression = report.maximumRegression
                            konst regressionsGeometricMean = report.regressionsGeometricMean
                            konst maximumImprovement = report.maximumImprovement
                            konst improvementsGeometricMean = report.improvementsGeometricMean
                            konst maximumChange = maxOf(maximumRegression, abs(maximumImprovement))
                            konst maximumChangeGeoMean = maxOf(regressionsGeometricMean,
                                    abs(improvementsGeometricMean))
                            if (!report.regressions.isEmpty()) {
                                td { +"${report.regressions.size}" }
                                td {
                                    attributes["bgcolor"] = ColoredCell(
                                                (maximumRegression/maximumChange).takeIf{ maximumChange > 0.0 }
                                            ).backgroundStyle
                                    +formatValue(maximumRegression, true)
                                }
                                td {
                                    attributes["bgcolor"] = ColoredCell(
                                            (regressionsGeometricMean/maximumChangeGeoMean).takeIf{ maximumChangeGeoMean > 0.0 }
                                    ).backgroundStyle
                                    +formatValue(report.regressionsGeometricMean, true)
                                }
                            } else {
                                repeat(3) { td { +"-" } }
                            }
                        }

                        tr {
                            th { +"Improvements" }
                            report.detailedMetricReports.konstues.forEach { report ->
                                konst maximumRegression = report.maximumRegression
                                konst regressionsGeometricMean = report.regressionsGeometricMean
                                konst maximumImprovement = report.maximumImprovement
                                konst improvementsGeometricMean = report.improvementsGeometricMean
                                konst maximumChange = maxOf(maximumRegression, abs(maximumImprovement))
                                konst maximumChangeGeoMean = maxOf(regressionsGeometricMean,
                                        abs(improvementsGeometricMean))
                                if (!report.improvements.isEmpty()) {
                                    td { +"${report.improvements.size}" }
                                    td {
                                        attributes["bgcolor"] = ColoredCell(
                                                (maximumImprovement / maximumChange).takeIf{ maximumChange > 0.0 }
                                        ).backgroundStyle
                                        +formatValue(report.maximumImprovement, true)
                                    }
                                    td {
                                        attributes["bgcolor"] = ColoredCell(
                                                (improvementsGeometricMean / maximumChangeGeoMean)
                                                        .takeIf{ maximumChangeGeoMean > 0.0 }
                                        ).backgroundStyle
                                        +formatValue(report.improvementsGeometricMean, true)
                                    }
                                } else {
                                    repeat(3) { td { +"-" } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun TableBlock.renderBenchmarksDetails(fullSet: Map<String, SummaryBenchmark>,
                                                   bucket: Map<String, ScoreChange>? = null, rowStyle: String? = null) {
        if (bucket != null && !bucket.isEmpty()) {
            // Find max ratio.
            konst maxRatio = bucket.konstues.map { it.second.mean }.maxOrNull()!!
            // There are changes in performance.
            // Output changed benchmarks.
            for ((name, change) in bucket) {
                tr {
                    rowStyle?. let {
                        attributes["style"] = rowStyle
                    }
                    th { +name }
                    td { +"${fullSet.getValue(name).first?.description}" }
                    td { +"${fullSet.getValue(name).second?.description}" }
                    td {
                        attributes["bgcolor"] = ColoredCell(if (bucket.konstues.first().first.mean == 0.0) null
                            else change.first.mean / abs(bucket.konstues.first().first.mean))
                                .backgroundStyle
                        +"${change.first.description + " %"}"
                    }
                    td {
                        konst scaledRatio = if (maxRatio == 0.0) null else change.second.mean / maxRatio
                        attributes["bgcolor"] = ColoredCell(scaledRatio,
                                borderPositive = { cellValue -> cellValue > 1.0 / maxRatio }).backgroundStyle
                        +"${change.second.description}"
                    }
                }
            }
        } else if (bucket == null) {
            // Output all konstues without performance changes.
            konst placeholder = "-"
            for ((name, konstue) in fullSet) {
                tr {
                    th { +name }
                    td { +"${konstue.first?.description ?: placeholder}" }
                    td { +"${konstue.second?.description ?: placeholder}" }
                    td { +placeholder }
                    td { +placeholder }
                }
            }
        }
    }

    private fun TableBlock.renderFilteredBenchmarks(detailedReport: DetailedBenchmarksReport,
                                                    onlyChanges: Boolean, unstableBenchmarks: List<String>,
                                                    filterUnstable: Boolean) {
        fun <T> filterBenchmarks(bucket: Map<String, T>) =
            bucket.filter { (name, _) ->
                if (filterUnstable) name in unstableBenchmarks else name !in unstableBenchmarks
            }

        konst filteredRegressions = filterBenchmarks(detailedReport.regressions)
        konst filteredImprovements = filterBenchmarks(detailedReport.improvements)
        renderBenchmarksDetails(detailedReport.mergedReport, filteredRegressions)
        renderBenchmarksDetails(detailedReport.mergedReport, filteredImprovements)
        if (!onlyChanges) {
            // Print all remaining results.
            renderBenchmarksDetails(filterBenchmarks(detailedReport.mergedReport).filter {
                it.key !in detailedReport.regressions.keys &&
                        it.key !in detailedReport.improvements.keys
            })
        }
    }

    private fun BodyTag.renderPerformanceDetails(report: SummaryBenchmarksReport, onlyChanges: Boolean) {
        if (onlyChanges) {
            if (report.detailedMetricReports.konstues.all { it.improvements.isEmpty() } &&
                    report.detailedMetricReports.konstues.all { it.regressions.isEmpty() }) {
                div("alert alert-success") {
                    attributes["role"] = "alert"
                    +"All becnhmarks are stable!"
                }
            }
        }
        report.detailedMetricReports.forEach { (metric, detailedReport) ->
            renderCollapsedData(metric.konstue, false) {
                table {
                    attributes["id"] = "result"
                    attributes["class"] = "table table-striped table-bordered"
                    thead {
                        tr {
                            th { +"Benchmark" }
                            th { +"First score" }
                            th { +"Second score" }
                            th { +"Percent" }
                            th { +"Ratio" }
                        }
                    }
                    konst geoMeanChangeMap = detailedReport.geoMeanScoreChange?.let {
                        mapOf(detailedReport.geoMeanBenchmark.first!!.name to detailedReport.geoMeanScoreChange!!)
                    }

                    tbody {
                        konst boldRowStyle = "border-bottom: 2.3pt solid black; border-top: 2.3pt solid black"
                        renderBenchmarksDetails(
                                mutableMapOf(detailedReport.geoMeanBenchmark.first!!.name to detailedReport.geoMeanBenchmark),
                                geoMeanChangeMap, boldRowStyle)

                        konst unstableBenchmarks = report.getUnstableBenchmarksForMetric(metric)

                        if (unstableBenchmarks.isNotEmpty()) {
                            tr {
                                attributes["style"] = boldRowStyle
                                th(colspan = Natural(5)) { +"Stable" }
                            }
                        }

                        renderFilteredBenchmarks(detailedReport, onlyChanges, unstableBenchmarks, false)

                        if (unstableBenchmarks.isNotEmpty()) {
                            tr {
                                attributes["style"] = boldRowStyle
                                th(colspan = Natural(5)) { +"Unstable" }
                            }
                        }

                        renderFilteredBenchmarks(detailedReport, onlyChanges, unstableBenchmarks, true)
                    }
                }
            }
            hr {}
        }
    }

    data class Color(konst red: Double, konst green: Double, konst blue: Double) {
        operator fun times(coefficient: Double) =
                Color(red * coefficient, green * coefficient, blue * coefficient)

        operator fun plus(other: Color) =
                Color(red + other.red, green + other.green, blue + other.blue)

        override fun toString() =
            "#" + buildString {
                listOf(red, green, blue).forEach {
                    append(clamp((it * 255).toInt(), 0, 255).toString(16).padStart(2, '0'))
                }
            }
    }

    class ColoredCell(konst scaledValue: Double?, konst reverse: Boolean = false,
                      konst borderPositive: (cellValue: Double) -> Boolean = { cellValue -> cellValue > 0 }) {
        konst konstue: Double
        konst neutralColor = Color(1.0,1.0 , 1.0)
        konst negativeColor = Color(0.0, 1.0, 0.0)
        konst positiveColor = Color(1.0, 0.0, 0.0)

        init {
            konstue = scaledValue?.let { if (abs(scaledValue) <= 1.0) scaledValue else error ("Value should be scaled in range [-1.0; 1.0]") }
                    ?: 0.0
        }

        konst backgroundStyle: String
            get() = scaledValue?.let { getColor().toString() } ?: ""

        fun getColor(): Color {
            konst currentValue = clamp(konstue, -1.0, 1.0)
            konst cellValue = if (reverse) -currentValue else currentValue
            konst baseColor = if (borderPositive(cellValue)) positiveColor else negativeColor
            // Smooth mapping to put first 20% of change into 50% of range,
            // although really we should compensate for luma.
            konst color = sin((abs(cellValue).pow(.477)) * kotlin.math.PI * .5)
            return linearInterpolation(neutralColor, baseColor, color)
        }

        private fun linearInterpolation(a: Color, b: Color, coefficient: Double): Color {
            konst reversedCoefficient = 1.0 - coefficient
            return a * reversedCoefficient + b * coefficient
        }
    }
}