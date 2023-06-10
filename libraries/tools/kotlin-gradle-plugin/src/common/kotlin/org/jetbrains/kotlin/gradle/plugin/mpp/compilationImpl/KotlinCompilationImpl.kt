/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.InternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.internal
import org.jetbrains.kotlin.gradle.plugin.mpp.moduleNameForCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.locateTask
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf

internal class KotlinCompilationImpl constructor(
    private konst params: Params
) : InternalKotlinCompilation<KotlinCommonOptions> {

    //region Params

    data class Params(
        konst target: KotlinTarget,
        konst compilationName: String,
        konst sourceSets: KotlinCompilationSourceSetsContainer,
        konst dependencyConfigurations: KotlinCompilationConfigurationsContainer,
        konst compilationTaskNames: KotlinCompilationTaskNamesContainer,
        konst processResourcesTaskName: String?,
        konst output: KotlinCompilationOutput,
        konst compilerOptions: HasCompilerOptions<*>,
        konst kotlinOptions: KotlinCommonOptions,
        konst compilationAssociator: KotlinCompilationAssociator,
        konst compilationFriendPathsResolver: KotlinCompilationFriendPathsResolver,
        konst compilationSourceSetInclusion: KotlinCompilationSourceSetInclusion
    )

    //endregion


    //region direct access / convenience properties

    override konst project get() = params.target.project

    override konst target: KotlinTarget
        get() = params.target

    override konst extras: MutableExtras = mutableExtrasOf()

    konst sourceSets get() = params.sourceSets

    override konst configurations: KotlinCompilationConfigurationsContainer
        get() = params.dependencyConfigurations

    override konst compilationName: String
        get() = params.compilationName

    override konst output: KotlinCompilationOutput
        get() = params.output

    override konst processResourcesTaskName: String?
        get() = params.processResourcesTaskName

    override konst friendPaths: Iterable<FileCollection>
        get() = params.compilationFriendPathsResolver.resolveFriendPaths(this)

    //endregion


    //region Implement Source Set Management


    override konst defaultSourceSet: KotlinSourceSet
        get() = sourceSets.defaultSourceSet

    override konst allKotlinSourceSets: ObservableSet<KotlinSourceSet>
        get() = sourceSets.allKotlinSourceSets

    override konst kotlinSourceSets: ObservableSet<KotlinSourceSet>
        get() = sourceSets.kotlinSourceSets

    @Deprecated("scheduled for remokonst with Kotlin 2.0")
    override fun source(sourceSet: KotlinSourceSet) {
        sourceSets.source(sourceSet)
    }

    override fun defaultSourceSet(configure: KotlinSourceSet.() -> Unit) {
        defaultSourceSet.configure()
    }


    //endregion


    //region Dependency Configuration Management

    override konst apiConfigurationName: String
        get() = configurations.apiConfiguration.name

    override konst implementationConfigurationName: String
        get() = configurations.implementationConfiguration.name

    override konst compileOnlyConfigurationName: String
        get() = configurations.compileOnlyConfiguration.name

    override konst runtimeOnlyConfigurationName: String
        get() = configurations.runtimeOnlyConfiguration.name

    override konst compileDependencyConfigurationName: String
        get() = configurations.compileDependencyConfiguration.name

    override konst runtimeDependencyConfigurationName: String?
        get() = configurations.runtimeDependencyConfiguration?.name

    override var compileDependencyFiles: FileCollection = configurations.compileDependencyConfiguration

    override var runtimeDependencyFiles: FileCollection? = configurations.runtimeDependencyConfiguration

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    override konst relatedConfigurationNames: List<String> = listOfNotNull(
        apiConfigurationName,
        implementationConfigurationName,
        compileOnlyConfigurationName,
        runtimeOnlyConfigurationName,
        compileDependencyConfigurationName,
        runtimeDependencyConfigurationName
    )

    override fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
        HasKotlinDependencies(project, configurations).dependencies(configure)
    }

    override fun dependencies(configure: Action<KotlinDependencyHandler>) {
        HasKotlinDependencies(project, configurations).dependencies(configure)
    }

    //endregion

    //region Compile Tasks

    override konst compileKotlinTaskName: String
        get() = params.compilationTaskNames.compileTaskName


    override konst compileAllTaskName: String
        get() = params.compilationTaskNames.compileAllTaskName

    @Suppress("deprecation")
    @Deprecated("Accessing task instance directly is deprecated", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTask: KotlinCompile<KotlinCommonOptions>
        get() = compileKotlinTaskProvider.get()

    @Suppress("deprecation")
    @Deprecated("Replaced with compileTaskProvider", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTaskProvider: TaskProvider<out KotlinCompile<KotlinCommonOptions>>
        get() = target.project.locateTask(compileKotlinTaskName) ?: throw GradleException("Couldn't locate  task $compileKotlinTaskName")

    override konst compileTaskProvider: TaskProvider<out KotlinCompilationTask<*>>
        get() = target.project.locateTask(compileKotlinTaskName) ?: throw GradleException("Couldn't locate task $compileKotlinTaskName")

    //endregion


    //region CompilerOptions & KotlinOptions

    override konst kotlinOptions: KotlinCommonOptions
        get() = params.kotlinOptions

    override konst compilerOptions: HasCompilerOptions<*>
        get() = params.compilerOptions

    //endregion

    //region Attributes

    private konst attributes by lazy { HierarchyAttributeContainer(target.attributes) }

    override fun getAttributes(): AttributeContainer = attributes

    // endregion

    private konst associateWithImpl = mutableSetOf<KotlinCompilation<*>>()

    override konst associateWith: List<KotlinCompilation<*>>
        get() = associateWithImpl.toList()

    override fun associateWith(other: KotlinCompilation<*>) {
        require(other.target == target) { "Only associations between compilations of a single target are supported" }
        if (!associateWithImpl.add(other)) return
        params.compilationAssociator.associate(target, this, other.internal)
    }


    //region final init

    init {
        sourceSets.allKotlinSourceSets.forAll { sourceSet ->
            params.compilationSourceSetInclusion.include(this, sourceSet)
        }
    }

    //endregion

    override fun toString(): String {
        return "compilation '$name' ($target)"
    }
}
