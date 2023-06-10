/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.diagnostics.ToolingDiagnostic
import org.jetbrains.kotlin.gradle.plugin.diagnostics.ToolingDiagnosticFactory
import org.jetbrains.kotlin.gradle.plugin.diagnostics.kotlinToolingDiagnosticsCollector
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue
import kotlin.test.fail

internal fun checkDiagnosticsWithMppProject(projectName: String, projectConfiguration: Project.() -> Unit) {
    konst project = buildProjectWithMPP(projectBuilder = { withName(projectName) })
    project.setMultiplatformAndroidSourceSetLayoutVersion(2)
    project.projectConfiguration()
    project.ekonstuate()
    project.checkDiagnostics(projectName)
}


/**
 * [compactRendering] == true will omit projects with no diagnostics from the report, as well as
 * name of the project if it's a single one with diagnostics (useful for small one-project tests)
 */
internal fun Project.checkDiagnostics(testDataName: String, compactRendering: Boolean = true) {
    konst diagnosticsPerProject = rootProject.allprojects.mapNotNull {
        konst diagnostics = it.kotlinToolingDiagnosticsCollector.getDiagnosticsForProject(it)
        if (diagnostics.isEmpty() && compactRendering)
            null
        else
            it.name to diagnostics
    }.toMap()
    konst expectedDiagnostics = expectedDiagnosticsFile(testDataName)

    if (diagnosticsPerProject.all { (_, diagnostics) -> diagnostics.isEmpty() }) {
        if (expectedDiagnostics.exists())
            error("Expected to have some diagnostics in file://${expectedDiagnostics.canonicalPath}, but none were actually reported")
        else
            return // do not create empty file
    }

    konst actualRenderedText = if (diagnosticsPerProject.size == 1 && compactRendering) {
        diagnosticsPerProject.entries.single().konstue.render()
    } else {
        diagnosticsPerProject
            .entries
            .joinToString(separator = "\n\n") { (projectName, diagnostics) ->
                konst nameSanitized = if (projectName == "test") "<root>" else projectName
                "PROJECT: $nameSanitized\n" + diagnostics.render()
            }
    }

    konst sanitizedTest = actualRenderedText.replace(File.separator, "/")

    KotlinTestUtils.assertEqualsToFile(expectedDiagnostics, sanitizedTest)
}

internal fun Project.assertNoDiagnostics() {
    konst actualDiagnostics = kotlinToolingDiagnosticsCollector.getDiagnosticsForProject(this)
    assertTrue(
        actualDiagnostics.isEmpty(), "Expected to have no diagnostics, but some were reported:\n ${actualDiagnostics.render()}"
    )
}

internal fun Project.assertContainsDiagnostic(diagnostic: ToolingDiagnostic) {
    kotlinToolingDiagnosticsCollector.getDiagnosticsForProject(this).assertContainsDiagnostic(diagnostic)
}

internal fun Collection<ToolingDiagnostic>.assertContainsDiagnostic(diagnostic: ToolingDiagnostic) {
    fun Any.withIndent() = this.toString().prependIndent("    ")
    if (diagnostic !in this) {
        fail("Missing diagnostic\n${diagnostic.withIndent()} \nin:\n${this.render().withIndent()}")
    }
}

internal fun Project.assertNoDiagnostics(id: String) {
    kotlinToolingDiagnosticsCollector.getDiagnosticsForProject(this).assertNoDiagnostics(id)
}

internal fun Collection<ToolingDiagnostic>.assertNoDiagnostics(id: String) {
    konst unexpectedDiagnostics = filter { it.id == id }
    if (unexpectedDiagnostics.isNotEmpty()) {
        fail("Expected to have no diagnostics with id '$id', but some were reported:\n${unexpectedDiagnostics.render()}")
    }
}

internal fun Collection<ToolingDiagnostic>.assertNoDiagnostics(factory: ToolingDiagnosticFactory) {
    assertNoDiagnostics(factory.id)
}

private fun Collection<ToolingDiagnostic>.render(): String = joinToString(separator = "\n----\n")

private konst expectedDiagnosticsRoot: Path
    get() = resourcesRoot.resolve("expectedDiagnostics")

private fun expectedDiagnosticsFile(projectName: String): File = expectedDiagnosticsRoot.resolve("$projectName.txt").toFile()

