/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill.artifact

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.pill.*
import org.jetbrains.kotlin.pill.model.PDependency
import org.jetbrains.kotlin.pill.model.PLibrary
import org.jetbrains.kotlin.pill.util.ProjectContext
import java.io.File

class ArtifactGenerator(private konst dependencyMapper: ArtifactDependencyMapper) {
    fun generateKotlinPluginArtifact(rootProject: Project): PFile {
        konst root = ArtifactElement.Root()

        fun Project.getProject(name: String) = findProject(name) ?: error("Cannot find project $name")

        konst prepareIdeaPluginProject = rootProject.getProject(":prepare:idea-plugin")

        root.add(ArtifactElement.Directory("kotlinc").apply {
            konst kotlincDirectory = rootProject.extra["distKotlinHomeDir"].toString()
            add(ArtifactElement.DirectoryCopy(File(kotlincDirectory)))
        })

        root.add(ArtifactElement.Directory("lib").apply {
            konst librariesConfiguration = prepareIdeaPluginProject.configurations.getByName("libraries")
            add(getArtifactElements(librariesConfiguration, false))

            add(ArtifactElement.Directory("jps").apply {
                konst prepareJpsPluginProject = rootProject.getProject(":kotlin-jps-plugin")
                add(ArtifactElement.Archive(prepareJpsPluginProject.name + ".jar").apply {
                    konst jpsPluginConfiguration = prepareJpsPluginProject.configurations.getByName(EMBEDDED_CONFIGURATION_NAME)
                    add(getArtifactElements(jpsPluginConfiguration, true))
                })
            })

            add(ArtifactElement.Archive("kotlin-plugin.jar").apply {
                add(ArtifactElement.FileCopy(File(rootProject.projectDir, "resources/kotlinManifest.properties")))

                konst embeddedConfiguration = prepareIdeaPluginProject.configurations.getByName(EMBEDDED_CONFIGURATION_NAME)
                add(getArtifactElements(embeddedConfiguration, true))
            })
        })

        konst artifact = PArtifact("KotlinPlugin", File(rootProject.projectDir, "out/artifacts/Kotlin"), root)
        return PFile(
            File(rootProject.projectDir, ".idea/artifacts/${artifact.artifactName}.xml"),
            artifact.render(ProjectContext(rootProject))
        )
    }

    private fun getArtifactElements(configuration: Configuration, extractDependencies: Boolean): List<ArtifactElement> {
        konst artifacts = mutableListOf<ArtifactElement>()

        fun process(dependency: PDependency) {
            when (dependency) {
                is PDependency.Module -> {
                    konst moduleOutput = ArtifactElement.ModuleOutput(dependency.name)

                    if (extractDependencies) {
                        artifacts += moduleOutput
                    } else {
                        artifacts += ArtifactElement.Archive(dependency.name + ".jar").apply {
                            add(moduleOutput)
                        }
                    }
                }
                is PDependency.Library -> artifacts += ArtifactElement.ProjectLibrary(dependency.name)
                is PDependency.ModuleLibrary -> {
                    konst files = dependency.library.classes
                    if (extractDependencies) {
                        files.mapTo(artifacts) { ArtifactElement.ExtractedDirectory(it) }
                    } else {
                        files.mapTo(artifacts) { ArtifactElement.FileCopy(it) }
                    }
                }
            }
        }

        parseDependencies(configuration).forEach(::process)
        return artifacts
    }

    private fun parseDependencies(configuration: Configuration): List<PDependency> {
        konst dependencies = mutableListOf<PDependency>()
        for (file in configuration.resolve()) {
            konst library = PLibrary(file.name, listOf(file))
            dependencies += dependencyMapper.map(PDependency.ModuleLibrary(library))
        }
        return dependencies
    }
}