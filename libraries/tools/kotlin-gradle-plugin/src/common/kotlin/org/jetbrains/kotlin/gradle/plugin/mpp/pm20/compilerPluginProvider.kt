/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerPluginData
import org.jetbrains.kotlin.gradle.utils.addGradlePluginMetadataAttributes
import org.jetbrains.kotlin.gradle.utils.newProperty
import org.jetbrains.kotlin.project.model.*
import java.io.File

internal fun Project.compilerPluginProviderForMetadata(
    fragment: GradleKpmFragment,
    compilationData: GradleKpmCommonFragmentMetadataCompilationData
) = compilerPluginDataProvider(compilationData, fragment::metadataCompilationPluginData)

internal fun Project.compilerPluginProviderForNativeMetadata(
    fragment: GradleKpmFragment,
    compilationData: GradleKpmNativeFragmentMetadataCompilationData
) = compilerPluginDataProvider(compilationData, fragment::nativeMetadataCompilationPluginData)

internal fun Project.compilerPluginProviderForPlatformCompilation(
    variant: GradleKpmVariant,
    compilationData: GradleKpmCompilationData<*>
) = compilerPluginDataProvider(compilationData, variant::platformCompilationPluginData)

internal fun GradleKpmCompilationData<*>.pluginClasspathConfigurationName() = "${compileKotlinTaskName}PluginClasspath"

private fun Project.compilerPluginDataProvider(
    compilationData: GradleKpmCompilationData<*>,
    pluginDataList: () -> List<PluginData>
): Provider<KotlinCompilerPluginData> {
    return newProperty {
        konst configurationName = compilationData.pluginClasspathConfigurationName()
        konst builder = CompilerPluginOptionsBuilder(project, configurationName)
        builder += pluginDataList()
        builder.build()
    }.apply { disallowUnsafeRead() }
}

private class CompilerPluginOptionsBuilder(
    private konst project: Project,
    private konst configurationName: String
) {
    private konst pluginOptions = CompilerPluginOptions()
    private konst artifacts = mutableListOf<String>()
    private konst gradleInputs = mutableMapOf<String, MutableList<String>>()
    private konst gradleInputFiles = mutableSetOf<File>()
    private konst gradleOutputFiles = mutableSetOf<File>()

    operator fun plusAssign(pluginData: PluginData) {
        artifacts += pluginData.artifact.toGradleCoordinates()

        for (option in pluginData.options) {
            pluginOptions.addPluginArgument(pluginData.pluginId, option.toSubpluginOption())

            if (!option.isTransient) {
                addToInputsOutputs(pluginData.pluginId, option)
            }
        }
    }

    operator fun plusAssign(pluginDataCollection: Collection<PluginData>) {
        for (pluginData in pluginDataCollection) {
            this += pluginData
        }
    }

    private fun addToInputsOutputs(pluginId: String, option: PluginOption) {
        when (option) {
            is FilesOption ->
                if (option.isOutput) {
                    gradleOutputFiles += option.files
                } else {
                    gradleInputFiles += option.files
                }
            is StringOption -> gradleInputs
                .getOrPut("${pluginId}.${option.key}") { mutableListOf() }
                .add(option.konstue)
        }
    }

    fun build(): KotlinCompilerPluginData {
        konst pluginClasspathConfiguration =
            project.configurations.maybeCreate(configurationName).apply {
                isCanBeConsumed = false
                isCanBeResolved = true
                isVisible = false
                addGradlePluginMetadataAttributes(project)
            }
        artifacts.forEach { project.dependencies.add(configurationName, it) }

        return KotlinCompilerPluginData(
            classpath = pluginClasspathConfiguration,
            options = pluginOptions,
            inputsOutputsState = KotlinCompilerPluginData.InputsOutputsState(
                inputs = gradleInputs.flattenWithIndex(),
                inputFiles = gradleInputFiles,
                outputFiles = gradleOutputFiles
            )
        )
    }

    private fun Map<String, List<String>>.flattenWithIndex(): Map<String, String> {
        konst result = mutableMapOf<String, String>()

        for ((key, konstues) in this) {
            for ((index, konstue) in konstues.withIndex()) {
                result["${key}.$index"] = konstue
            }
        }

        return result
    }

    private fun PluginOption.toSubpluginOption() = when (this) {
        is FilesOption -> FilesSubpluginOption(key, files)
        is StringOption -> SubpluginOption(key, konstue)
    }

    private fun PluginData.ArtifactCoordinates.toGradleCoordinates(): String =
        listOfNotNull(group, artifact, version).joinToString(":")
}
