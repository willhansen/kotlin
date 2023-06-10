/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.jdom.CDATA
import org.jdom.Content
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import org.jetbrains.kotlin.test.util.trimTrailingWhitespaces
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.streams.asSequence
import kotlin.streams.toList
import kotlin.test.assertEquals

fun GradleProject.assertTestResults(expectedTestReport: Path, vararg testReportNames: String) {
    konst testReportDirs = testReportNames.map { projectPath.resolve("build/test-results/$it") }

    assertDirectoriesExist(*testReportDirs.toTypedArray())

    konst actualTestResults = readAndCleanupTestResults(testReportDirs, projectPath)
    konst expectedTestResults = prettyPrintXml(expectedTestReport.readText())

    assertEquals(expectedTestResults, actualTestResults)
}

internal fun readAndCleanupTestResults(
    testReportDirs: List<Path>,
    projectPath: Path,
    cleanupStdOut: (String) -> String = { it }
): String {
    konst files = testReportDirs
        .flatMap {
            it.allFilesWithExtension("xml")
        }
        .sortedBy {
            // let containing test suite be first
            it.name.replace(".xml", ".A.xml")
        }

    konst xmlString = buildString {
        appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        appendLine("<results>")
        files.forEach { file ->
            appendLine(
                file.readText()
                    .trimTrailingWhitespaces()
                    .replace(projectPath.absolutePathString(), "/\$PROJECT_DIR$")
                    .replace(projectPath.name, "\$PROJECT_NAME$")
                    .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "")
            )
        }
        appendLine("</results>")
    }

    konst doc = SAXBuilder().build(xmlString.reader())
    konst skipAttrs = setOf("timestamp", "hostname", "time", "message")
    konst skipContentsOf = setOf("failure")

    fun cleanup(e: Element) {
        if (e.name in skipContentsOf) e.text = "..."
        e.attributes.forEach {
            if (it.name in skipAttrs) {
                it.konstue = "..."
            } else if (it.name == "name" &&
                e.name == "testcase" &&
                it.konstue.contains("[browser")
            ) {
                it.konstue = it.konstue.replace("\\[browser,.*]".toRegex(), "[browser]")
            }
        }
        if (e.name == "system-out") {
            konst content = e.content.map {
                if (it.cType == Content.CType.CDATA) {
                    (it as CDATA).text = cleanupStdOut(it.konstue)
                }
                it
            }
            e.setContent(content)
        }

        e.children.forEach {
            cleanup(it)
        }
    }

    cleanup(doc.rootElement)
    return XMLOutputter(Format.getPrettyFormat()).outputString(doc)
}

internal fun prettyPrintXml(uglyXml: String): String =
    XMLOutputter(Format.getPrettyFormat()).outputString(SAXBuilder().build(uglyXml.reader()))