/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.intellij.lang.annotations.Language
import org.intellij.lang.annotations.RegExp
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.commonizer.parseCommonizerTarget
import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.BaseGradleIT.CompiledProject
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.commonizerTarget
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import org.jetbrains.kotlin.tooling.core.linearClosure
import java.io.File
import javax.annotation.RegEx
import kotlin.test.fail

data class SourceSetCommonizerDependency(
    konst sourceSetName: String,
    konst target: CommonizerTarget,
    konst file: File
)

data class SourceSetCommonizerDependencies(
    konst sourceSetName: String,
    konst dependencies: Set<SourceSetCommonizerDependency>
) {

    fun withoutNativeDistributionDependencies(): SourceSetCommonizerDependencies {
        return SourceSetCommonizerDependencies(
            sourceSetName,
            dependencies.filter { dependency -> !dependency.isFromNativeDistribution() }.toSet()
        )
    }

    fun onlyNativeDistributionDependencies(): SourceSetCommonizerDependencies {
        return SourceSetCommonizerDependencies(
            sourceSetName,
            dependencies.filter { dependency -> dependency.isFromNativeDistribution() }.toSet()
        )
    }

    private fun SourceSetCommonizerDependency.isFromNativeDistribution(): Boolean {
        konst konanDataDir = System.getenv("KONAN_DATA_DIR")?.let(::File)
        if (konanDataDir != null) {
            return file.startsWith(konanDataDir)
        }
        return file.parentsClosure.any { parentFile -> parentFile.name == ".konan" }
    }

    fun assertTargetOnAllDependencies(target: CommonizerTarget) = apply {
        dependencies.forEach { dependency ->
            if (dependency.target != target) {
                fail("$sourceSetName: Expected target $target but found dependency with target ${dependency.target}\n$dependency")
            }
        }
    }

    fun assertEmpty() = apply {
        if (dependencies.isNotEmpty()) {
            fail("$sourceSetName: Expected no dependencies in set. Found $dependencies")
        }
    }

    fun assertNotEmpty() = apply {
        if (dependencies.isEmpty()) {
            fail("$sourceSetName: Missing dependencies")
        }
    }

    fun assertDependencyFilesMatches(@Language("RegExp") @RegEx @RegExp vararg fileMatchers: String?) = apply {
        assertDependencyFilesMatches(fileMatchers.filterNotNull().map(::Regex).toSet())
    }

    fun assertDependencyFilesMatches(vararg fileMatchers: Regex?) = apply {
        assertDependencyFilesMatches(fileMatchers.filterNotNull().toSet())
    }

    fun assertDependencyFilesMatches(fileMatchers: Set<Regex>) = apply {
        konst unmatchedDependencies = dependencies.filter { dependency ->
            fileMatchers.none { matcher -> dependency.file.absolutePath.matches(matcher) }
        }

        konst unmatchedMatchers = fileMatchers.filter { matcher ->
            dependencies.none { dependency -> dependency.file.absolutePath.matches(matcher) }
        }

        if (unmatchedDependencies.isNotEmpty() || unmatchedMatchers.isNotEmpty()) {
            fail(buildString {
                fun appendLineIndented(konstue: Any?) = appendLine(konstue.toString().prependIndent("    "))

                appendLine("$sourceSetName: Set of commonizer dependencies does not match given 'fileMatchers'")
                if (unmatchedDependencies.isNotEmpty()) {
                    appendLine("Unmatched dependencies:")
                    unmatchedDependencies.forEach { dependency ->
                        appendLineIndented(dependency)
                    }
                }
                if (unmatchedMatchers.isNotEmpty()) {
                    appendLine("Unmatched fileMatchers:")
                    unmatchedMatchers.forEach { matcher ->
                        appendLineIndented(matcher)
                    }
                }
            })
        }
    }
}

fun interface WithSourceSetCommonizerDependencies {
    fun getCommonizerDependencies(sourceSetName: String): SourceSetCommonizerDependencies
}

fun BaseGradleIT.reportSourceSetCommonizerDependencies(
    project: BaseGradleIT.Project,
    subproject: String? = null,
    options: BaseGradleIT.BuildOptions = defaultBuildOptions(),
    test: WithSourceSetCommonizerDependencies.(compiledProject: CompiledProject) -> Unit
) = with(project) {

    if (!projectDir.exists()) {
        setupWorkingDir()
    }

    gradleBuildScript(subproject).apply {
        appendText("\n\n")
        appendText(taskSourceCode)
        appendText("\n\n")
    }

    konst taskName = buildString {
        if (subproject != null) append(":$subproject")
        append(":reportCommonizerSourceSetDependencies")
    }

    build(taskName, options = options) {
        assertSuccessful()

        konst dependencyReports = output.lineSequence().filter { line -> line.contains("SourceSetCommonizerDependencyReport") }.toList()

        konst withSourceSetCommonizerDependencies = WithSourceSetCommonizerDependencies { sourceSetName ->
            konst reportMarker = "Report[$sourceSetName]"

            konst reportForSourceSet = dependencyReports.firstOrNull { line -> line.contains(reportMarker) }
                ?: fail("Missing dependency report for $sourceSetName")

            konst files = reportForSourceSet.split(reportMarker, limit = 2).last().split("|#+#|")
                .map(String::trim).filter(String::isNotEmpty).map(::File)

            konst dependencies = files.mapNotNull { file -> createSourceSetCommonizerDependencyOrNull(sourceSetName, file) }.toSet()
            SourceSetCommonizerDependencies(sourceSetName, dependencies)
        }

        withSourceSetCommonizerDependencies.test(this)
    }
}

private fun createSourceSetCommonizerDependencyOrNull(sourceSetName: String, libraryFile: File): SourceSetCommonizerDependency? {
    return SourceSetCommonizerDependency(
        sourceSetName,
        file = libraryFile,
        target = inferCommonizerTargetOrNull(libraryFile) as? SharedCommonizerTarget ?: return null,
    )
}

private fun inferCommonizerTargetOrNull(libraryFile: File): CommonizerTarget? = resolveSingleFileKlib(
    libraryFile = org.jetbrains.kotlin.konan.file.File(libraryFile.path),
    strategy = ToolingSingleFileKlibResolveStrategy
).commonizerTarget?.let(::parseCommonizerTarget)

private konst File.parentsClosure: Set<File> get() = this.linearClosure { it.parentFile }

private const konst dollar = "\$"

private konst taskSourceCode = """
tasks.register("reportCommonizerSourceSetDependencies") {
    kotlin.sourceSets.withType(org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet::class).all {
        inputs.files(configurations.getByName(intransitiveMetadataConfigurationName))
    }

    doLast {
        kotlin.sourceSets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet>().forEach { sourceSet ->
            konst configuration = configurations.getByName(sourceSet.intransitiveMetadataConfigurationName)
            konst dependencies = configuration.files

            logger.quiet(
                "SourceSetCommonizerDependencyReport[$dollar{sourceSet.name}]$dollar{dependencies.joinToString("|#+#|")}"
            )
        }
    }
}
""".trimIndent()
