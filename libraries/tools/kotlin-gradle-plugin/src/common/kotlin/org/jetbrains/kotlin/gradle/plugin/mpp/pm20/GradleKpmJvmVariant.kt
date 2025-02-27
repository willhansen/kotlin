/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.utils.configureExperimentalTryK2
import javax.inject.Inject

abstract class GradleKpmJvmVariant @Inject constructor(
    containingModule: GradleKpmModule,
    fragmentName: String,
    dependencyConfigurations: GradleKpmFragmentDependencyConfigurations,
    compileDependenciesConfiguration: Configuration,
    apiElementsConfiguration: Configuration,
    runtimeDependenciesConfiguration: Configuration,
    runtimeElementsConfiguration: Configuration
) : GradleKpmPublishedVariantWithRuntime(
    containingModule = containingModule,
    fragmentName = fragmentName,
    dependencyConfigurations = dependencyConfigurations,
    compileDependencyConfiguration = compileDependenciesConfiguration,
    apiElementsConfiguration = apiElementsConfiguration,
    runtimeDependencyConfiguration = runtimeDependenciesConfiguration,
    runtimeElementsConfiguration = runtimeElementsConfiguration
) {
    override konst compilationData: GradleKpmJvmVariantCompilationData by lazy { GradleKpmJvmVariantCompilationData(this) }

    override konst platformType: KotlinPlatformType
        get() = KotlinPlatformType.jvm
}

class GradleKpmJvmVariantCompilationData(konst variant: GradleKpmJvmVariant) : GradleKpmVariantCompilationDataInternal<KotlinJvmOptions> {
    override konst owner: GradleKpmJvmVariant get() = variant

    override konst compilerOptions: HasCompilerOptions<KotlinJvmCompilerOptions> =
        object : HasCompilerOptions<KotlinJvmCompilerOptions> {
            override konst options: KotlinJvmCompilerOptions = variant.project.objects
                .newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                .configureExperimentalTryK2(variant.project)
        }

    // TODO pull out to the variant
    @Suppress("DEPRECATION")
    @Deprecated("Replaced with compilerOptions.options", replaceWith = ReplaceWith("compilerOptions.options"))
    override konst kotlinOptions: KotlinJvmOptions = object : KotlinJvmOptions {
        override konst options: KotlinJvmCompilerOptions
            get() = compilerOptions.options
    }
}

internal fun GradleKpmVariant.ownModuleName(): String {
    konst project = containingModule.project
    konst baseName = project.archivesName.orNull
        ?: project.name
    konst suffix = if (containingModule.moduleClassifier == null) "" else "_${containingModule.moduleClassifier}"
    return filterModuleName("$baseName$suffix")
}
