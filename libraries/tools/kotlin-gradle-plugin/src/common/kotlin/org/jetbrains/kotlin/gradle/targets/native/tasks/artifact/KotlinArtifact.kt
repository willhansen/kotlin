/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName

abstract class KotlinArtifactConfigImpl(
    override konst artifactName: String
) : KotlinArtifactConfig {
    override konst modules = mutableSetOf<Any>()
    override fun setModules(vararg project: Any) {
        modules.clear()
        modules.addAll(project)
    }

    override fun addModule(project: Any) {
        modules.add(project)
    }

    protected open fun konstidate() {
        check(modules.isNotEmpty()) {
            "Native artifact '$artifactName' wasn't configured because it requires at least one module for linking"
        }
    }
}

abstract class KotlinNativeArtifactConfigImpl(artifactName: String) : KotlinArtifactConfigImpl(artifactName), KotlinNativeArtifactConfig {
    override var modes: Set<NativeBuildType> = NativeBuildType.DEFAULT_BUILD_TYPES
    override fun modes(vararg modes: NativeBuildType) {
        this.modes = modes.toSet()
    }

    override var isStatic: Boolean = false
    override var linkerOptions: List<String> = emptyList()

    internal var toolOptionsConfigure: KotlinCommonCompilerToolOptions.() -> Unit = {}
    override fun toolOptions(configure: Action<KotlinCommonCompilerToolOptions>) {
        toolOptionsConfigure = configure::execute
    }

    @Suppress("DEPRECATION")
    internal var kotlinOptionsFn: KotlinCommonToolOptions.() -> Unit = {}
    override fun kotlinOptions(fn: Action<KotlinCommonToolOptions>) {
        kotlinOptionsFn = fn::execute
    }

    internal konst binaryOptions: MutableMap<String, String> = mutableMapOf()
    override fun binaryOption(name: String, konstue: String) {
        binaryOptions[name] = konstue
    }

    override fun konstidate() {
        super.konstidate()
        check(modes.isNotEmpty()) {
            "Native artifact '$artifactName' wasn't configured because it requires at least one build type in modes"
        }
    }
}

internal fun Project.registerLibsDependencies(target: KonanTarget, artifactName: String, deps: Set<Any>): String {
    konst librariesConfigurationName = lowerCamelCaseName(target.presetName, artifactName, "linkLibrary")
    configurations.maybeCreate(librariesConfigurationName).apply {
        isVisible = false
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = true
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
        attributes.attribute(KotlinNativeTarget.konanTargetAttribute, target.name)
        attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
    }
    deps.forEach { dependencies.add(librariesConfigurationName, it) }
    return librariesConfigurationName
}

internal fun Project.registerExportDependencies(target: KonanTarget, artifactName: String, deps: Set<Any>): String {
    konst exportConfigurationName = lowerCamelCaseName(target.presetName, artifactName, "linkExport")
    configurations.maybeCreate(exportConfigurationName).apply {
        isVisible = false
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
        attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
        attributes.attribute(KotlinNativeTarget.konanTargetAttribute, target.name)
        attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
    }
    deps.forEach { dependencies.add(exportConfigurationName, it) }
    return exportConfigurationName
}