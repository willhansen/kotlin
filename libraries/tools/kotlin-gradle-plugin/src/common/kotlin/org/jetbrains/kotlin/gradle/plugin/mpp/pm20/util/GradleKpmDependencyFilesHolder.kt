/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmAbstractFragmentMetadataCompilationData
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmVariant
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmVariantWithRuntime
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.resolvableMetadataConfigurationName
import org.jetbrains.kotlin.gradle.utils.filesProvider

interface GradleKpmDependencyFilesHolder {
    konst dependencyConfigurationName: String
    var dependencyFiles: FileCollection

    companion object
}

internal fun GradleKpmDependencyFilesHolder.Companion.ofVariantCompileDependencies(variant: GradleKpmVariant): GradleKpmDependencyFilesHolder =
    object : GradleKpmDependencyFilesHolder {
        override konst dependencyConfigurationName: String
            get() = variant.compileDependenciesConfiguration.name
        override var dependencyFiles: FileCollection
            get() = variant.compileDependencyFiles
            set(konstue) { variant.compileDependencyFiles = konstue }
    }

internal fun GradleKpmDependencyFilesHolder.Companion.ofVariantRuntimeDependencies(variant: GradleKpmVariantWithRuntime): GradleKpmDependencyFilesHolder =
    object : GradleKpmDependencyFilesHolder {
        override konst dependencyConfigurationName: String
            get() = variant.runtimeDependenciesConfiguration.name
        override var dependencyFiles: FileCollection
            get() = variant.runtimeDependencyFiles
            set(konstue) { variant.runtimeDependencyFiles = konstue }
    }

internal fun GradleKpmDependencyFilesHolder.Companion.ofMetadataCompilationDependencies(
    compilationData: GradleKpmAbstractFragmentMetadataCompilationData<*>
) = object : GradleKpmDependencyFilesHolder {
    override konst dependencyConfigurationName: String
        get() = compilationData.fragment.containingModule.resolvableMetadataConfigurationName

    override var dependencyFiles: FileCollection
        get() = compilationData.compileDependencyFiles
        set(konstue) { compilationData.compileDependencyFiles = konstue }
}

class SimpleDependencyFilesHolder(
    override konst dependencyConfigurationName: String,
    override var dependencyFiles: FileCollection
) : GradleKpmDependencyFilesHolder

internal fun Project.newDependencyFilesHolder(dependencyConfigurationName: String): GradleKpmDependencyFilesHolder =
    SimpleDependencyFilesHolder(
        dependencyConfigurationName,
        project.filesProvider { project.configurations.getByName(dependencyConfigurationName) }
    )
