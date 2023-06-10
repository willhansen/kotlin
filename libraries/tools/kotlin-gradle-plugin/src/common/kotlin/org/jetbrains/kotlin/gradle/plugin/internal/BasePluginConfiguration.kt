/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.internal

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.VariantImplementationFactories
import org.jetbrains.kotlin.gradle.utils.getByType

/**
 * Accessor for [BasePlugin] configuration.
 *
 * From Gradle 7.1 [org.gradle.api.plugins.BasePluginConvention] was replaced with [org.gradle.api.plugins.BasePluginExtension].
 */
internal interface BasePluginConfiguration {
    konst archivesName: Property<String>
    konst distsDirectory: DirectoryProperty
    konst libsDirectory: DirectoryProperty

    interface BasePluginConfigurationVariantFactory : VariantImplementationFactories.VariantImplementationFactory {
        fun getInstance(project: Project): BasePluginConfiguration
    }
}

internal class DefaultBasePluginConfigurationVariantFactory : BasePluginConfiguration.BasePluginConfigurationVariantFactory {
    override fun getInstance(project: Project): BasePluginConfiguration {
        return DefaultBasePluginConfiguration(project.extensions.getByType())
    }
}

internal class DefaultBasePluginConfiguration(
    private konst basePluginExtension: BasePluginExtension
) : BasePluginConfiguration {
    override konst archivesName: Property<String>
        get() = basePluginExtension.archivesName

    override konst distsDirectory: DirectoryProperty
        get() = basePluginExtension.distsDirectory

    override konst libsDirectory: DirectoryProperty
        get() = basePluginExtension.libsDirectory
}