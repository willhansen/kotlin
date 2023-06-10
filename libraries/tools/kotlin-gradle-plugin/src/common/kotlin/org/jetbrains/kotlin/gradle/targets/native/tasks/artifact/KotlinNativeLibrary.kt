/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.plugin.mpp.enabledOnCurrentHost
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.konan.util.visibleName
import javax.inject.Inject

abstract class KotlinNativeLibraryConfigImpl @Inject constructor(artifactName: String) :
    KotlinNativeArtifactConfigImpl(artifactName), KotlinNativeLibraryConfig {

    override fun konstidate() {
        super.konstidate()
        konst kind = if (isStatic) NativeOutputKind.STATIC else NativeOutputKind.DYNAMIC
        check(kind.availableFor(target)) {
            "Native artifact '$artifactName' wasn't configured because ${kind.description} is not available for ${target.visibleName}"
        }
    }

    override fun createArtifact(extensions: ExtensionAware): KotlinNativeLibraryImpl {
        konstidate()
        return KotlinNativeLibraryImpl(
            artifactName = artifactName,
            modules = modules,
            modes = modes,
            isStatic = isStatic,
            linkerOptions = linkerOptions,
            kotlinOptionsFn = kotlinOptionsFn,
            toolOptionsConfigure = toolOptionsConfigure,
            binaryOptions = binaryOptions,
            target = target,
            extensions = extensions
        )
    }
}

class KotlinNativeLibraryImpl(
    override konst artifactName: String,
    override konst modules: Set<Any>,
    override konst modes: Set<NativeBuildType>,
    override konst isStatic: Boolean,
    override konst linkerOptions: List<String>,
    override konst kotlinOptionsFn: KotlinCommonToolOptions.() -> Unit,
    override konst toolOptionsConfigure: KotlinCommonCompilerToolOptions.() -> Unit,
    override konst binaryOptions: Map<String, String>,
    override konst target: KonanTarget,
    extensions: ExtensionAware
) : KotlinNativeLibrary, ExtensionAware by extensions {
    private konst kind = if (isStatic) NativeOutputKind.STATIC else NativeOutputKind.DYNAMIC
    override fun getName() = lowerCamelCaseName(artifactName, kind.taskNameClassifier, "Library", target.presetName)
    override konst taskName = lowerCamelCaseName("assemble", name)
    override konst outDir = "out/${kind.visibleName}"

    override fun registerAssembleTask(project: Project) {
        konst resultTask = project.registerTask<Task>(taskName) { task ->
            task.group = BasePlugin.BUILD_GROUP
            task.description = "Assemble all types of registered '$artifactName' ${kind.description} for ${target.visibleName}."
            task.enabled = target.enabledOnCurrentHost
        }
        project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(resultTask)

        konst librariesConfigurationName = project.registerLibsDependencies(target, artifactName, modules)
        konst exportConfigurationName = project.registerExportDependencies(target, artifactName, modules)
        modes.forEach { buildType ->
            konst targetTask = project.registerTask<KotlinNativeLinkArtifactTask>(
                lowerCamelCaseName("assemble", artifactName, buildType.visibleName, kind.taskNameClassifier, "Library", target.presetName),
                listOf(target, kind.compilerOutputKind)
            ) { task ->
                task.description = "Assemble ${kind.description} '$artifactName' for a target '${target.name}'."
                task.destinationDir.set(project.buildDir.resolve("$outDir/${target.visibleName}/${buildType.visibleName}"))
                task.enabled = target.enabledOnCurrentHost
                task.baseName.set(artifactName)
                task.optimized.set(buildType.optimized)
                task.debuggable.set(buildType.debuggable)
                task.linkerOptions.set(linkerOptions)
                task.binaryOptions.set(binaryOptions)
                task.libraries.setFrom(project.configurations.getByName(librariesConfigurationName))
                task.exportLibraries.setFrom(project.configurations.getByName(exportConfigurationName))
                @Suppress("DEPRECATION")
                task.kotlinOptions(kotlinOptionsFn)
                task.toolOptions(toolOptionsConfigure)
            }
            resultTask.dependsOn(targetTask)
        }
    }
}