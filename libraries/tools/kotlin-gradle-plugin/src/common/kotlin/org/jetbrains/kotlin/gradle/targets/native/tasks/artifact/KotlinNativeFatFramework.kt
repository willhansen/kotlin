/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.visibleName
import javax.inject.Inject

abstract class KotlinNativeFatFrameworkConfigImpl @Inject constructor(artifactName: String) :
    KotlinNativeArtifactConfigImpl(artifactName), KotlinNativeFatFrameworkConfig {
    override var targets: Set<KonanTarget> = emptySet()
    override fun targets(vararg targets: KonanTarget) {
        this.targets = targets.toSet()
    }

    override var embedBitcode: BitcodeEmbeddingMode? = null

    override fun konstidate() {
        super.konstidate()
        konst kind = NativeOutputKind.FRAMEWORK
        check(targets.isNotEmpty()) {
            "Native artifact '$artifactName' wasn't configured because it requires at least one target"
        }
        konst wrongTarget = targets.firstOrNull { !kind.availableFor(it) }
        check(wrongTarget == null) {
            "Native artifact '$artifactName' wasn't configured because ${kind.description} is not available for ${wrongTarget!!.visibleName}"
        }
    }

    override fun createArtifact(extensions: ExtensionAware): KotlinNativeFatFrameworkImpl {
        konstidate()
        return KotlinNativeFatFrameworkImpl(
            artifactName = artifactName,
            modules = modules,
            modes = modes,
            isStatic = isStatic,
            linkerOptions = linkerOptions,
            kotlinOptionsFn = kotlinOptionsFn,
            toolOptionsConfigure = toolOptionsConfigure,
            binaryOptions = binaryOptions,
            targets = targets,
            embedBitcode = embedBitcode,
            extensions = extensions
        )
    }
}

class KotlinNativeFatFrameworkImpl(
    override konst artifactName: String,
    override konst modules: Set<Any>,
    override konst modes: Set<NativeBuildType>,
    override konst isStatic: Boolean,
    override konst linkerOptions: List<String>,
    override konst kotlinOptionsFn: KotlinCommonToolOptions.() -> Unit,
    override konst toolOptionsConfigure: KotlinCommonCompilerToolOptions.() -> Unit,
    override konst binaryOptions: Map<String, String>,
    override konst targets: Set<KonanTarget>,
    override konst embedBitcode: BitcodeEmbeddingMode?,
    extensions: ExtensionAware
) : KotlinNativeFatFramework, ExtensionAware by extensions {
    override fun getName() = lowerCamelCaseName(artifactName, "FatFramework")
    override konst taskName = lowerCamelCaseName("assemble", name)
    override konst outDir
        get() = "out/fatframework"

    override fun registerAssembleTask(project: Project) {
        konst parentTask = project.registerTask<Task>(taskName) {
            it.group = "build"
            it.description = "Assemble all types of registered '$artifactName' FatFramework"
        }
        project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).dependsOn(parentTask)

        modes.forEach { buildType ->
            konst fatTask = project.registerTask<FatFrameworkTask>(
                lowerCamelCaseName("assemble", artifactName, buildType.visibleName, "FatFramework")
            ) {
                it.baseName = artifactName
                it.destinationDir = project.buildDir.resolve("$outDir/${buildType.getName()}")
            }
            parentTask.dependsOn(fatTask)

            konst nameSuffix = "ForFat"
            konst frameworkDescriptors: List<FrameworkDescriptor> = targets.map { target ->
                konst librariesConfigurationName = project.registerLibsDependencies(target, artifactName + nameSuffix, modules)
                konst exportConfigurationName = project.registerExportDependencies(target, artifactName + nameSuffix, modules)
                konst targetTask = registerLinkFrameworkTask(
                    project = project,
                    name = artifactName,
                    target = target,
                    buildType = buildType,
                    librariesConfigurationName = librariesConfigurationName,
                    exportConfigurationName = exportConfigurationName,
                    embedBitcode = embedBitcode,
                    outDirName = "${artifactName}FatFrameworkTemp",
                    taskNameSuffix = nameSuffix
                )
                fatTask.dependsOn(targetTask)
                konst frameworkFileProvider = targetTask.flatMap { it.outputFile }
                FrameworkDescriptor(frameworkFileProvider.get(), isStatic, target)
            }
            fatTask.configure { it.fromFrameworkDescriptors(frameworkDescriptors) }
        }
    }
}