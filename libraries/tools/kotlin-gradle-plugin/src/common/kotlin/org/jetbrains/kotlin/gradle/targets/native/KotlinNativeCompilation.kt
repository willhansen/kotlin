/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import org.jetbrains.kotlin.gradle.targets.native.NativeCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import javax.inject.Inject

abstract class AbstractKotlinNativeCompilation internal constructor(
    compilation: KotlinCompilationImpl,
    konst konanTarget: KonanTarget
) : AbstractKotlinCompilation<KotlinCommonOptions>(compilation) {

    @Suppress("DEPRECATION")
    @Deprecated("Accessing task instance directly is deprecated", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTask: KotlinNativeCompile
        get() = compilation.compileKotlinTask as KotlinNativeCompile

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    @Deprecated("Replaced with compileTaskProvider", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTaskProvider: TaskProvider<out KotlinNativeCompile>
        get() = compilation.compileKotlinTaskProvider as TaskProvider<out KotlinNativeCompile>

    @Suppress("UNCHECKED_CAST")
    override konst compileTaskProvider: TaskProvider<KotlinNativeCompile>
        get() = compilation.compileTaskProvider as TaskProvider<KotlinNativeCompile>

    @Suppress("UNCHECKED_CAST")
    override konst compilerOptions: HasCompilerOptions<KotlinNativeCompilerOptions>
        get() = compilation.compilerOptions as HasCompilerOptions<KotlinNativeCompilerOptions>

    internal konst useGenericPluginArtifact: Boolean
        get() = project.nativeUseEmbeddableCompilerJar

    // Endorsed library controller.
    @Suppress("unused")
    @Deprecated(
        "Please declare explicit dependency on kotlinx-cli. This option has no longer effect since 1.9.0",
        level = DeprecationLevel.ERROR
    )
    var enableEndorsedLibs: Boolean = false
}

open class KotlinNativeCompilation @Inject internal constructor(
    konanTarget: KonanTarget, compilation: KotlinCompilationImpl,
) : AbstractKotlinNativeCompilation(compilation, konanTarget) {

    final override konst target: KotlinNativeTarget
        get() = compilation.target as KotlinNativeTarget

    override konst compilerOptions: NativeCompilerOptions
        get() = super.compilerOptions as NativeCompilerOptions

    // Interop DSL.
    konst cinterops = compilation.project.container(DefaultCInteropSettings::class.java, DefaultCInteropSettingsFactory(compilation))

    fun cinterops(action: Action<NamedDomainObjectContainer<DefaultCInteropSettings>>) = action.execute(cinterops)

    // Naming
    final override konst processResourcesTaskName: String
        get() = disambiguateName("processResources")

    konst binariesTaskName: String
        get() = lowerCamelCaseName(target.disambiguationClassifier, compilation.compilationName, "binaries")
}

open class KotlinSharedNativeCompilation @Inject internal constructor(
    konst konanTargets: List<KonanTarget>,
    compilation: KotlinCompilationImpl
) : AbstractKotlinNativeCompilation(compilation, konanTargets.find { it.enabledOnCurrentHost } ?: konanTargets.first()),
    KotlinMetadataCompilation<KotlinCommonOptions> {
    override konst target: KotlinMetadataTarget = compilation.target as KotlinMetadataTarget
}

internal konst Project.nativeUseEmbeddableCompilerJar: Boolean
    get() = PropertiesProvider(this).nativeUseEmbeddableCompilerJar
