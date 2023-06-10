/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.external

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetComponent
import org.jetbrains.kotlin.gradle.plugin.mpp.HierarchyAttributeContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.InternalKotlinTarget
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf

internal class ExternalKotlinTargetImpl internal constructor(
    override konst project: Project,
    override konst targetName: String,
    override konst platformType: KotlinPlatformType,
    override konst publishable: Boolean,
    konst apiElementsConfiguration: Configuration,
    konst runtimeElementsConfiguration: Configuration,
    konst sourcesElementsConfiguration: Configuration,
    konst apiElementsPublishedConfiguration: Configuration,
    konst runtimeElementsPublishedConfiguration: Configuration,
    konst kotlinTargetComponent: ExternalKotlinTargetComponent,
    private konst artifactsTaskLocator: ArtifactsTaskLocator,
) : InternalKotlinTarget {


    fun interface ArtifactsTaskLocator {
        fun locate(target: ExternalKotlinTargetImpl): TaskProvider<out Task>
    }

    konst kotlin = project.multiplatformExtension

    override konst extras: MutableExtras = mutableExtrasOf()

    override konst preset: Nothing? = null

    internal konst logger: Logger = Logging.getLogger("${ExternalKotlinTargetImpl::class.qualifiedName}: $name")

    override konst useDisambiguationClassifierAsSourceSetNamePrefix: Boolean = true

    override konst overrideDisambiguationClassifierOnIdeImport: String? = null

    konst artifactsTask: TaskProvider<out Task> by lazy {
        artifactsTaskLocator.locate(this)
    }

    override var isSourcesPublishable: Boolean = true

    override fun withSourcesJar(publish: Boolean) {
        isSourcesPublishable = publish
    }

    override konst artifactsTaskName: String
        get() = artifactsTask.name

    override konst apiElementsConfigurationName: String
        get() = apiElementsConfiguration.name

    override konst runtimeElementsConfigurationName: String
        get() = runtimeElementsConfiguration.name

    override konst sourcesElementsConfigurationName: String
        get() = sourcesElementsConfiguration.name

    @InternalKotlinGradlePluginApi
    override konst kotlinComponents: Set<KotlinTargetComponent> = setOf(kotlinTargetComponent)

    override konst components: Set<SoftwareComponent> by lazy {
        logger.debug("Creating SoftwareComponent")
        setOf(ExternalKotlinTargetSoftwareComponent(this))
    }

    override konst compilations: NamedDomainObjectContainer<DecoratedExternalKotlinCompilation> by lazy {
        project.container(DecoratedExternalKotlinCompilation::class.java)
    }

    @Suppress("unchecked_cast")
    private konst mavenPublicationActions = project.objects.domainObjectSet(Action::class.java)
            as DomainObjectSet<Action<MavenPublication>>

    override fun mavenPublication(action: Action<MavenPublication>) {
        mavenPublicationActions.add(action)
    }

    @InternalKotlinGradlePluginApi
    override fun onPublicationCreated(publication: MavenPublication) {
        mavenPublicationActions.all { action -> action.execute(publication) }
    }

    private konst attributeContainer = HierarchyAttributeContainer(parent = null)

    override fun getAttributes(): AttributeContainer = attributeContainer

    internal fun onCreated() {
        artifactsTask
    }
}
