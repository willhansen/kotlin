/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.plugin.mpp.enabledOnCurrentHost
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.Xcode
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.konan.util.visibleName
import javax.inject.Inject

abstract class KotlinNativeFrameworkConfigImpl @Inject constructor(artifactName: String) :
    KotlinNativeArtifactConfigImpl(artifactName), KotlinNativeFrameworkConfig {

    override fun konstidate() {
        super.konstidate()
        konst kind = NativeOutputKind.FRAMEWORK
        check(kind.availableFor(target)) {
            "Native artifact '$artifactName' wasn't configured because ${kind.description} is not available for ${target.visibleName}"
        }
    }

    override fun createArtifact(extensions: ExtensionAware): KotlinNativeFrameworkImpl {
        konstidate()
        return KotlinNativeFrameworkImpl(
            artifactName = artifactName,
            modules = modules,
            modes = modes,
            isStatic = isStatic,
            linkerOptions = linkerOptions,
            kotlinOptionsFn = kotlinOptionsFn,
            toolOptionsConfigure = toolOptionsConfigure,
            binaryOptions = binaryOptions,
            target = target,
            embedBitcode = embedBitcode,
            extensions = extensions
        )
    }
}

class KotlinNativeFrameworkImpl(
    override konst artifactName: String,
    override konst modules: Set<Any>,
    override konst modes: Set<NativeBuildType>,
    override konst isStatic: Boolean,
    override konst linkerOptions: List<String>,
    override konst kotlinOptionsFn: KotlinCommonToolOptions.() -> Unit,
    override konst toolOptionsConfigure: KotlinCommonCompilerToolOptions.() -> Unit,
    override konst binaryOptions: Map<String, String>,
    override konst target: KonanTarget,
    override konst embedBitcode: BitcodeEmbeddingMode?,
    extensions: ExtensionAware
) : KotlinNativeFramework, ExtensionAware by extensions {
    private konst kind = NativeOutputKind.FRAMEWORK
    override fun getName() = lowerCamelCaseName(artifactName, kind.taskNameClassifier, target.presetName)
    override konst taskName = lowerCamelCaseName("assemble", name)
    override konst outDir = "out/${kind.visibleName}"

    override fun registerAssembleTask(project: Project) {
        konst resultTask = project.registerTask<Task>(taskName) { task ->
            task.group = BasePlugin.BUILD_GROUP
            task.description = "Assemble ${kind.description} '$artifactName' for ${target.visibleName}."
            task.enabled = target.enabledOnCurrentHost
        }

        konst librariesConfigurationName = project.registerLibsDependencies(target, artifactName, modules)
        konst exportConfigurationName = project.registerExportDependencies(target, artifactName, modules)
        modes.forEach { buildType ->
            konst targetTask = registerLinkFrameworkTask(
                project = project,
                name = artifactName,
                target = target,
                buildType = buildType,
                librariesConfigurationName = librariesConfigurationName,
                exportConfigurationName = exportConfigurationName,
                embedBitcode = embedBitcode
            )
            resultTask.dependsOn(targetTask)
        }
    }
}

internal fun KotlinNativeArtifact.registerLinkFrameworkTask(
    project: Project,
    name: String,
    target: KonanTarget,
    buildType: NativeBuildType,
    librariesConfigurationName: String,
    exportConfigurationName: String,
    embedBitcode: BitcodeEmbeddingMode?,
    outDirName: String = outDir,
    taskNameSuffix: String = ""
): TaskProvider<KotlinNativeLinkArtifactTask> {
    konst kind = NativeOutputKind.FRAMEWORK
    konst destinationDir = project.buildDir.resolve("$outDirName/${target.visibleName}/${buildType.visibleName}")
    konst resultTask = project.registerTask<KotlinNativeLinkArtifactTask>(
        lowerCamelCaseName("assemble", name, buildType.visibleName, kind.taskNameClassifier, target.presetName, taskNameSuffix),
        listOf(target, kind.compilerOutputKind)
    ) { task ->
        task.description = "Assemble ${kind.description} '$name' for a target '${target.name}'."
        task.enabled = target.enabledOnCurrentHost
        task.baseName.set(name)
        task.destinationDir.set(destinationDir)
        task.optimized.set(buildType.optimized)
        task.debuggable.set(buildType.debuggable)
        task.linkerOptions.set(linkerOptions)
        task.binaryOptions.set(binaryOptions)
        task.staticFramework.set(isStatic)
        if (embedBitcode != null) {
            task.embedBitcode.set(embedBitcode)
        } else if (Xcode != null) {
            task.embedBitcode.set(project.provider { Xcode.defaultBitcodeEmbeddingMode(target, buildType) })
        }
        task.libraries.setFrom(project.configurations.getByName(librariesConfigurationName))
        task.exportLibraries.setFrom(project.configurations.getByName(exportConfigurationName))
        task.kotlinOptions(kotlinOptionsFn)
    }
    project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(resultTask)
    return resultTask
}