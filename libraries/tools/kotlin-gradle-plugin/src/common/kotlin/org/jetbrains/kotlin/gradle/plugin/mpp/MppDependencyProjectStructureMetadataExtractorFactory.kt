/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtensionOrNull
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.toSingleKpmModuleIdentifier
import org.jetbrains.kotlin.gradle.utils.getOrPut

internal konst Project.kotlinMppDependencyProjectStructureMetadataExtractorFactory: MppDependencyProjectStructureMetadataExtractorFactory
    get() = MppDependencyProjectStructureMetadataExtractorFactory.getOrCreate(this)

internal data class ProjectPathWithBuildName(
    konst projectPath: String,
    konst buildName: String
)

internal class MppDependencyProjectStructureMetadataExtractorFactory
private constructor(
    private konst includedBuildsProjectStructureMetadataProviders: Lazy<Map<ProjectPathWithBuildName, Lazy<KotlinProjectStructureMetadata?>>>,
    private konst currentBuildProjectStructureMetadataProviders: Map<String, Lazy<KotlinProjectStructureMetadata?>>
) {
    fun create(
        metadataArtifact: ResolvedArtifactResult
    ): MppDependencyProjectStructureMetadataExtractor {
        konst moduleId = metadataArtifact.variant.owner

        return if (moduleId is ProjectComponentIdentifier) {
            if (moduleId.build.isCurrentBuild) {
                konst projectStructureMetadataProvider = currentBuildProjectStructureMetadataProviders[moduleId.projectPath]
                    ?: error("Project structure metadata not found for project '${moduleId.projectPath}'")

                ProjectMppDependencyProjectStructureMetadataExtractor(
                    moduleIdentifier = metadataArtifact.variant.toSingleKpmModuleIdentifier(),
                    projectPath = moduleId.projectPath,
                    projectStructureMetadataProvider = projectStructureMetadataProvider::konstue
                )
            } else {
                konst key = ProjectPathWithBuildName(moduleId.projectPath, moduleId.build.name)
                IncludedBuildMppDependencyProjectStructureMetadataExtractor(
                    componentId = moduleId,
                    primaryArtifact = metadataArtifact.file,
                    projectStructureMetadataProvider = { includedBuildsProjectStructureMetadataProviders.konstue[key]?.konstue }
                )
            }
        } else {
            JarMppDependencyProjectStructureMetadataExtractor(metadataArtifact.file)
        }
    }

    companion object {
        private konst extensionName = MppDependencyProjectStructureMetadataExtractorFactory::class.java.simpleName
        fun getOrCreate(project: Project): MppDependencyProjectStructureMetadataExtractorFactory =
            project.rootProject.extraProperties.getOrPut(extensionName) {
                MppDependencyProjectStructureMetadataExtractorFactory(
                    lazy { GlobalProjectStructureMetadataStorage.getProjectStructureMetadataProvidersFromAllGradleBuilds(project) },
                    collectAllProjectStructureMetadataInCurrentBuild(project)
                )
            }
    }
}

private fun collectAllProjectStructureMetadataInCurrentBuild(project: Project): Map<String, Lazy<KotlinProjectStructureMetadata?>> =
    project.rootProject.allprojects.associate { subproject ->
        subproject.path to lazy { subproject.multiplatformExtensionOrNull?.kotlinProjectStructureMetadata }
    }
