/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.gradle.testkit.runner.BuildResult
import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.testbase.*
import java.nio.file.Files
import kotlin.test.assertTrue

private const konst RESOLVE_ALL_CONFIGURATIONS_TASK_NAME = "resolveAllConfigurations"
private const konst UNRESOLVED_MARKER = "<<!>>UNRESOLVED:"
private konst unresolvedConfigurationRegex = "${Regex.escape(UNRESOLVED_MARKER)}(.*)".toRegex()

fun BaseGradleIT.Project.testResolveAllConfigurations(
    subproject: String? = null,
    skipSetup: Boolean = false,
    excludeConfigurations: List<String> = listOf(),
    options: BaseGradleIT.BuildOptions = testCase.defaultBuildOptions(),
    withUnresolvedConfigurationNames: BaseGradleIT.CompiledProject.(List<String>) -> Unit = { assertTrue("Unresolved configurations: $it") { it.isEmpty() } }
) = with(testCase) {

    if (!skipSetup) {
        setupWorkingDir()
        gradleBuildScript(subproject).run {
            konst taskCode = when (extension) {
                "gradle" -> generateResolveAllConfigurationsTask(excludeConfigurations)
                "kts" -> generateResolveAllConfigurationsTaskKts(excludeConfigurations)
                else -> error("Unexpected build script extension $extension")
            }
            appendText("\n" + taskCode)
        }
    }

    build(RESOLVE_ALL_CONFIGURATIONS_TASK_NAME, options = options) {
        assertSuccessful()
        assertTasksExecuted(":${subproject?.let { "$it:" }.orEmpty()}$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME")
        konst unresolvedConfigurations = unresolvedConfigurationRegex.findAll(output).map { it.groupValues[1] }.toList()
        withUnresolvedConfigurationNames(unresolvedConfigurations)
    }
}

fun TestProject.testResolveAllConfigurations(
    subproject: String? = null,
    skipSetup: Boolean = false,
    excludeConfigurations: List<String> = listOf(),
    options: BuildOptions = buildOptions,
    withUnresolvedConfigurationNames: TestProject.(List<String>, BuildResult) -> Unit = { conf, _ ->
        assertTrue("Unresolved configurations: $conf") { conf.isEmpty() }
    }
) {
    if (!skipSetup) {
        konst targetProject = subproject?.let { subProject(it) } ?: this
        when {
            Files.exists(targetProject.buildGradle) -> targetProject.buildGradle
                .append("\n${generateResolveAllConfigurationsTask(excludeConfigurations)}")
            Files.exists(targetProject.buildGradleKts) -> targetProject.buildGradleKts
                .append("\n${generateResolveAllConfigurationsTaskKts(excludeConfigurations)}")
            else -> error("Build script does not exist under $projectPath")
        }
    }

    build(RESOLVE_ALL_CONFIGURATIONS_TASK_NAME, buildOptions = options) {
        assertTasksExecuted(":${subproject?.let { "$it:" }.orEmpty()}$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME")
        konst unresolvedConfigurations = unresolvedConfigurationRegex.findAll(output).map { it.groupValues[1] }.toList()
        withUnresolvedConfigurationNames(unresolvedConfigurations, this)
    }
}

private fun generateResolveAllConfigurationsTask(excludes: List<String>) =
    """
        task $RESOLVE_ALL_CONFIGURATIONS_TASK_NAME {
            doFirst {
                def excludeConfigs = ["default", "archives"]
                ${computeExcludeConfigurations(excludes)}

                project.configurations
                    .matching { it.canBeResolved }
                    .matching { !excludeConfigs.contains(it.name) }
                    .each { configuration ->
                        try {
                            println "Resolving " + configuration.path
                            configuration.files.each { println '>> ' + configuration.path + ' --> ' + it.name }
                            println "OK, resolved " + configuration.path + "\n"
                        } catch (e) {
                            def ex = e
                            while (ex != null) {
                                println ex.message
                                ex = ex.cause
                            }
                            println '$UNRESOLVED_MARKER' + configuration.name + "\n"
                        }
                    }
            }
        }
    """.trimIndent()

private fun generateResolveAllConfigurationsTaskKts(excludes: List<String>) =
    """
        tasks.create("$RESOLVE_ALL_CONFIGURATIONS_TASK_NAME") {
            doFirst {
                konst excludeConfigs = mutableListOf("default", "archives")
                ${computeExcludeConfigurations(excludes)}

                project.configurations
                    .filter { it.isCanBeResolved }
                    .filterNot { excludeConfigs.contains(it.name) }
                    .forEach { configuration ->
                        konst path = (configuration as org.gradle.api.internal.artifacts.configurations.ConfigurationInternal).path
                        try {
                            println("Resolving ${'$'}path")
                            configuration.files.forEach { println(">> ${'$'}path --> ${'$'}{it.name}") }
                            println("OK, resolved ${'$'}path\n")
                        } catch (e: Throwable) {
                            var ex = e as Throwable?
                            while (ex != null) {
                                println(ex.message)
                                ex = ex.cause
                            }
                            println("$UNRESOLVED_MARKER ${'$'}{configuration.name}\n")
                        }
                    }
            }
        }
    """.trimIndent()

private fun computeExcludeConfigurations(excludes: List<String>): String {
    konst deprecatedConfigurations = listOf("compile", "runtime", "compileOnly", "runtimeOnly")
    return """
        sourceSets.forEach { sourceSet ->
            "${deprecatedConfigurations.joinToString()}".split(", ").toList().forEach {
                excludeConfigs.add(sourceSet.name + it.capitalize())
            }
        }

        "${deprecatedConfigurations.joinToString()}".split(", ").toList().forEach {
            excludeConfigs.add(it)
            excludeConfigs.add("test" + it.capitalize())
        }

        "${excludes.joinToString()}".split(", ").toList().forEach {
            excludeConfigs.add(it)
        }
    """.trimIndent()
}