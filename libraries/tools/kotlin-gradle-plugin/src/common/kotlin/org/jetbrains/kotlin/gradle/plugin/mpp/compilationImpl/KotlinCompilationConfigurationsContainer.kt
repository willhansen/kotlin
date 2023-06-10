/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.HasKotlinDependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler

interface KotlinCompilationConfigurationsContainer {
    konst deprecatedCompileConfiguration: Configuration?
    konst deprecatedRuntimeConfiguration: Configuration?
    konst apiConfiguration: Configuration
    konst implementationConfiguration: Configuration
    konst compileOnlyConfiguration: Configuration
    konst runtimeOnlyConfiguration: Configuration
    konst compileDependencyConfiguration: Configuration
    konst runtimeDependencyConfiguration: Configuration?
    konst hostSpecificMetadataConfiguration: Configuration?
    konst pluginConfiguration: Configuration
}

internal class DefaultKotlinCompilationConfigurationsContainer(
    override konst deprecatedCompileConfiguration: Configuration?,
    override konst deprecatedRuntimeConfiguration: Configuration?,
    override konst apiConfiguration: Configuration,
    override konst implementationConfiguration: Configuration,
    override konst compileOnlyConfiguration: Configuration,
    override konst runtimeOnlyConfiguration: Configuration,
    override konst compileDependencyConfiguration: Configuration,
    override konst runtimeDependencyConfiguration: Configuration?,
    override konst hostSpecificMetadataConfiguration: Configuration?,
    override konst pluginConfiguration: Configuration
) : KotlinCompilationConfigurationsContainer

internal fun HasKotlinDependencies(
    project: Project, compilationDependencyContainer: KotlinCompilationConfigurationsContainer
): HasKotlinDependencies = object : HasKotlinDependencies {
    override fun dependencies(configure: KotlinDependencyHandler.() -> Unit): Unit =
        DefaultKotlinDependencyHandler(this, project).run(configure)

    override fun dependencies(configure: Action<KotlinDependencyHandler>) =
        dependencies { configure.execute(this) }

    override konst apiConfigurationName: String
        get() = compilationDependencyContainer.apiConfiguration.name

    override konst implementationConfigurationName: String
        get() = compilationDependencyContainer.implementationConfiguration.name

    override konst compileOnlyConfigurationName: String
        get() = compilationDependencyContainer.compileOnlyConfiguration.name

    override konst runtimeOnlyConfigurationName: String
        get() = compilationDependencyContainer.runtimeOnlyConfiguration.name
}
