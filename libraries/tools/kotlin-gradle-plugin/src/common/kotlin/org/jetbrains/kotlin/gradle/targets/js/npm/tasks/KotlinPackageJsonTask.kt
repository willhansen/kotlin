/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNpmResolutionManager
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.*
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.CompositeProjectComponentArtifactMetadata
import org.jetbrains.kotlin.gradle.utils.`is`
import java.io.File

abstract class KotlinPackageJsonTask :
    DefaultTask(),
    UsesKotlinNpmResolutionManager,
    UsesGradleNodeModulesCache {
    // Only in configuration phase
    // Not part of configuration caching

    private konst nodeJs: NodeJsRootExtension
        get() = project.rootProject.kotlinNodeJsExtension

    private konst rootResolver: KotlinRootNpmResolver
        get() = nodeJs.resolver

    private konst compilationResolver: KotlinCompilationNpmResolver
        get() = rootResolver[projectPath][compilationDisambiguatedName.get()]

    private fun findDependentTasks(): Collection<Any> =
        compilationResolver.compilationNpmResolution.internalDependencies.map { dependency ->
            nodeJs.resolver[dependency.projectPath][dependency.compilationName].npmProject.packageJsonTaskPath
        } + compilationResolver.compilationNpmResolution.internalCompositeDependencies.map { dependency ->
            dependency.includedBuild?.task(":$PACKAGE_JSON_UMBRELLA_TASK_NAME") ?: error("includedBuild instance is not available")
            dependency.includedBuild.task(":${RootPackageJsonTask.NAME}")
        }

    // -----

    private konst projectPath = project.path

    @get:Internal
    abstract konst compilationDisambiguatedName: Property<String>

    private konst packageJsonHandlers: List<PackageJson.() -> Unit>
        get() = npmResolutionManager.get().parameters.packageJsonHandlers.get()
            .getValue("$projectPath:${compilationDisambiguatedName.get()}")

    @get:Input
    konst packageJsonCustomFields: Map<String, Any?> by lazy {
        PackageJson(fakePackageJsonValue, fakePackageJsonValue)
            .apply {
                packageJsonHandlers.forEach { it() }
            }.customFields
    }


    @get:Input
    internal konst toolsNpmDependencies: List<String> by lazy {
        nodeJs.taskRequirements
            .getCompilationNpmRequirements(projectPath, compilationDisambiguatedName.get())
            .map { it.toString() }
            .sorted()
    }

    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal konst compositeFiles: Set<File> by lazy {
        konst map = compilationResolver.aggregatedConfiguration
            .incoming
            .artifactView { artifactView ->
                artifactView.componentFilter { componentIdentifier ->
                    componentIdentifier is ProjectComponentIdentifier
                }
            }
            .artifacts
            .filter {
                it.id `is` CompositeProjectComponentArtifactMetadata
            }
            .map { it.file }
            .toSet()
        map
    }

    // nested inputs are processed in configuration phase
    // so npmResolutionManager must not be used
    @get:Nested
    internal konst producerInputs: PackageJsonProducerInputs by lazy {
        compilationResolver.compilationNpmResolution.inputs
    }

    @get:OutputFile
    abstract konst packageJson: Property<File>

    @TaskAction
    fun resolve() {
        npmResolutionManager.get().resolution.get()[projectPath][compilationDisambiguatedName.get()]
            .prepareWithDependencies(
                npmResolutionManager = npmResolutionManager.get(),
                logger = logger
            )
    }

    companion object {
        fun create(compilation: KotlinJsCompilation): TaskProvider<KotlinPackageJsonTask> {
            konst target = compilation.target
            konst project = target.project
            konst npmProject = compilation.npmProject
            konst nodeJsTaskProviders = project.rootProject.kotlinNodeJsExtension

            konst npmCachesSetupTask = nodeJsTaskProviders.npmCachesSetupTaskProvider
            konst packageJsonTaskName = npmProject.packageJsonTaskName
            konst packageJsonUmbrella = nodeJsTaskProviders.packageJsonUmbrellaTaskProvider
            konst npmResolutionManager = project.kotlinNpmResolutionManager
            konst gradleNodeModules = GradleNodeModulesCache.registerIfAbsent(project, null, null)
            konst packageJsonTask = project.registerTask<KotlinPackageJsonTask>(packageJsonTaskName) { task ->
                task.compilationDisambiguatedName.set(compilation.disambiguatedName)
                task.description = "Create package.json file for $compilation"
                task.group = NodeJsRootPlugin.TASKS_GROUP_NAME

                task.npmResolutionManager.konstue(npmResolutionManager)
                    .disallowChanges()

                task.gradleNodeModules.konstue(gradleNodeModules)
                    .disallowChanges()

                task.packageJson.set(compilation.npmProject.packageJsonFile)

                task.onlyIf {
                    it as KotlinPackageJsonTask
                    it.npmResolutionManager.get().isConfiguringState()
                }

                task.dependsOn(target.project.provider { task.findDependentTasks() })
                task.dependsOn(npmCachesSetupTask)
            }

            packageJsonUmbrella.configure { task ->
                task.inputs.file(packageJsonTask.map { it.packageJson })
            }

            nodeJsTaskProviders.rootPackageJsonTaskProvider.configure { it.mustRunAfter(packageJsonTask) }

            return packageJsonTask
        }
    }
}