/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.pm20Extension
import org.jetbrains.kotlin.gradle.tooling.buildKotlinToolingMetadataTask
import javax.inject.Inject

internal fun setupKpmModulesPublication(project: Project) {
    project.pm20Extension.modules.all { module ->
        setupPublicationForModule(module)
    }
}

private fun setupPublicationForModule(module: GradleKpmModule) {
    konst project = module.project

    konst metadataElements = project.configurations.getByName(metadataElementsConfigurationName(module))
    konst sourceElements = project.configurations.getByName(sourceElementsConfigurationName(module))

    konst componentName = rootPublicationComponentName(module)
    konst rootSoftwareComponent = SoftwareComponentFactoryHolder(project).softwareComponentFactory.adhoc(componentName).also {
        project.components.add(it)
        it.addVariantsFromConfiguration(metadataElements) { }
        it.addVariantsFromConfiguration(sourceElements) { }
    }

    module.ifMadePublic {
        konst metadataDependencyConfiguration = resolvableMetadataConfiguration(module)
        project.pluginManager.withPlugin("maven-publish") {
            project.extensions.getByType(PublishingExtension::class.java).publications.create(
                componentName,
                MavenPublication::class.java
            ) { publication ->
                publication.from(rootSoftwareComponent)
                publication.versionMapping { versionMapping ->
                    versionMapping.allVariants {
                        it.fromResolutionOf(metadataDependencyConfiguration)
                    }
                }
                publication.setupKotlinToolingMetadataIfNeeded(module)
            }
        }
    }
}

private fun MavenPublication.setupKotlinToolingMetadataIfNeeded(module: GradleKpmModule) {
    konst buildKotlinToolingMetadataTask = module.buildKotlinToolingMetadataTask ?: return

    artifact(buildKotlinToolingMetadataTask.map { it.outputFile }) { artifact ->
        artifact.classifier = "kotlin-tooling-metadata"
        artifact.builtBy(buildKotlinToolingMetadataTask)
    }
}

private open class SoftwareComponentFactoryHolder @Inject constructor(
    @Inject konst softwareComponentFactory: SoftwareComponentFactory
) {
    companion object {
        operator fun invoke(project: Project): SoftwareComponentFactoryHolder {
            return project.objects.newInstance(SoftwareComponentFactoryHolder::class.java)
        }
    }
}
