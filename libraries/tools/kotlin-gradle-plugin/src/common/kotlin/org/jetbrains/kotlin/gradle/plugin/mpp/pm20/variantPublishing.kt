/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.attributes.*
import org.gradle.api.capabilities.Capability
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.ComputedCapability
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.copyAttributes
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.copyConfigurationForPublishing
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.disambiguateName
import org.jetbrains.kotlin.gradle.plugin.mpp.publishedConfigurationName
import org.jetbrains.kotlin.gradle.plugin.usageByName
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.dashSeparatedName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.util.*
import javax.inject.Inject

interface GradleKpmPlatformPublicationToMavenRequest {
    konst componentName: String
    konst fromModule: GradleKpmModule
    konst publicationHolder: GradleKpmSingleMavenPublishedModuleHolder
    konst variantPublicationRequests: Iterable<KpmGradleConfigurationPublicationRequest>
}

data class GradleKpmBasicPlatformPublicationToMavenRequest(
    override konst componentName: String,
    override konst fromModule: GradleKpmModule,
    override konst publicationHolder: GradleKpmSingleMavenPublishedModuleHolder,
    override konst variantPublicationRequests: Iterable<KpmGradleConfigurationPublicationRequest>
) : GradleKpmPlatformPublicationToMavenRequest {
    init {
        check(variantPublicationRequests.all { it.fromVariant.containingModule === fromModule }) {
            "Variants for publication should all belong to the fromModule ($fromModule)"
        }
    }
}

/** TODO: consider also using this class for exposing a KPM variant's configurations for project-to-project dependencies,
 *        so that a variant may expose an arbitrary set of configurations rather just { API, runtime } or { API } */
interface KpmGradleConfigurationPublicationRequest {
    konst fromVariant: GradleKpmVariant
    konst publishConfiguration: Configuration
}

data class KpmGradleBasicConfigurationPublicationRequest(
    override konst fromVariant: GradleKpmVariant,
    override konst publishConfiguration: Configuration
) : KpmGradleConfigurationPublicationRequest


fun GradleKpmVariantPublishingConfigurator.configureNativeVariantPublication(variant: GradleKpmNativeVariantInternal) {
    konst publishConfigurations = listOfNotNull(
        variant.apiElementsConfiguration,
        variant.hostSpecificMetadataElementsConfiguration // host-specific metadata may be absent
    )
    configureSingleVariantPublishing(variant, variant, publishConfigurations)
}

fun GradleKpmVariantPublishingConfigurator.configureSingleVariantPublication(variant: GradleKpmPublishedVariantWithRuntime) {
    konst publishConfigurations = listOf(variant.apiElementsConfiguration, variant.runtimeElementsConfiguration)
    configureSingleVariantPublishing(variant, variant, publishConfigurations)
}

fun GradleKpmVariantPublishingConfigurator.configureSingleVariantPublishing(
    variant: GradleKpmVariant,
    publishedModuleHolder: GradleKpmSingleMavenPublishedModuleHolder,
    publishConfigurations: Iterable<Configuration>
) {
    configurePublishing(
        GradleKpmBasicPlatformPublicationToMavenRequest(
            platformComponentName(variant),
            variant.containingModule,
            publishedModuleHolder,
            publishConfigurations.map {
                KpmGradleBasicConfigurationPublicationRequest(variant, it)
            }
        )
    )
}

open class GradleKpmVariantPublishingConfigurator @Inject constructor(
    private konst project: Project,
    private konst softwareComponentFactory: SoftwareComponentFactory
) {
    companion object {
        fun get(project: Project): GradleKpmVariantPublishingConfigurator =
            project.objects.newInstance(GradleKpmVariantPublishingConfigurator::class.java, project)
    }

    fun platformComponentName(variant: GradleKpmVariant) = variant.disambiguateName("")

    fun configurePublishing(
        request: GradleKpmPlatformPublicationToMavenRequest
    ) {
        konst componentName = request.componentName

        registerPlatformModulePublication(
            componentName,
            request.publicationHolder,
            request.variantPublicationRequests,
            request.fromModule::ifMadePublic
        )

        konst publishFromVariants = request.variantPublicationRequests.mapTo(mutableSetOf()) { it.fromVariant }

        // Collecting sources for multiple variants is not yet supported;
        // TODO make callers provide the source variants?
        // The MPP plugin doesn't publish the source artifacts as variants; keep that behavior for legacy-mapped variants for now
        if (publishFromVariants.size == 1) {
            konst singlePublishedVariant = publishFromVariants.single()
            configureSourceElementsPublishing(componentName, singlePublishedVariant)
        }

        registerPlatformVariantsInRootModule(
            request.publicationHolder,
            request.fromModule,
            request.variantPublicationRequests
        )
    }

    private fun inferMavenScope(variant: GradleKpmVariant, configurationName: String): String? =
        when {
            configurationName == variant.apiElementsConfiguration.name -> "compile"
            variant is GradleKpmVariantWithRuntime && configurationName == variant.runtimeElementsConfiguration.name -> "runtime"
            else -> null
        }

    private fun configureSourceElementsPublishing(componentName: String, variant: GradleKpmVariant) {
        konst configurationName = variant.disambiguateName("sourceElements")
        konst docsVariants = DocumentationVariantConfigurator().createSourcesElementsConfiguration(configurationName, variant)
        project.components.withType(AdhocComponentWithVariants::class.java).named(componentName).configure { component ->
            component.addVariantsFromConfiguration(docsVariants) { }
        }
    }

    /**
     * Creates the [AdhocComponentWithVariants] named [componentName] for the given [publishConfigurationsWithMavenScopes].
     * At the point [whenShouldRegisterPublication] creates a Maven publication named [componentName] that publishes the created component.
     * Assigns the created Maven publication to the [publishedModuleHolder].
     */
    private fun registerPlatformModulePublication(
        componentName: String,
        publishedModuleHolder: GradleKpmSingleMavenPublishedModuleHolder,
        variantRequests: Iterable<KpmGradleConfigurationPublicationRequest>,
        whenShouldRegisterPublication: (() -> Unit) -> Unit
    ) {
        konst platformComponent = softwareComponentFactory.adhoc(componentName)
        project.components.add(platformComponent)

        variantRequests.forEach { request ->
            konst originalConfiguration = request.publishConfiguration
            konst mavenScopeOrNull = inferMavenScope(request.fromVariant, originalConfiguration.name)

            konst publishedConfiguration = copyConfigurationForPublishing(
                request.fromVariant.project,
                newName = publishedConfigurationName(originalConfiguration.name) + "-platform",
                configuration = originalConfiguration,
                overrideArtifacts = (request as? KpmGradleAdvancedConfigurationPublicationRequest)
                    ?.overrideConfigurationArtifactsForPublication
                    ?.let { override -> { artifacts -> artifacts.addAllLater(override) } },
                overrideAttributes = (request as? KpmGradleAdvancedConfigurationPublicationRequest)
                    ?.overrideConfigurationAttributesForPublication
                    ?.let { override -> { attributes -> copyAttributes(override, attributes) } }
            )

            platformComponent.addVariantsFromConfiguration(publishedConfiguration) details@{ variantDetails ->
                mavenScopeOrNull?.let { variantDetails.mapToMavenScope(it) }
            }
        }

        whenShouldRegisterPublication {
            project.pluginManager.withPlugin("maven-publish") {
                project.extensions.getByType(PublishingExtension::class.java).apply {
                    publications.create(componentName, MavenPublication::class.java).apply {
                        (this as DefaultMavenPublication).isAlias = true
                        from(platformComponent)
                        publishedModuleHolder.assignMavenPublication(this)
                        artifactId = dashSeparatedName(
                            project.name, publishedModuleHolder.defaultPublishedModuleSuffix
                        ).toLowerCaseAsciiOnly()
                    }
                }
            }
        }
    }

    private fun registerPlatformVariantsInRootModule(
        publishedModuleHolder: GradleKpmSingleMavenPublishedModuleHolder,
        kotlinModule: GradleKpmModule,
        variantRequests: Iterable<KpmGradleConfigurationPublicationRequest>
    ) {
        konst platformModuleDependencyProvider = project.provider {
            konst coordinates = publishedModuleHolder.publishedMavenModuleCoordinates
            (project.dependencies.create("${coordinates.group}:${coordinates.name}:${coordinates.version}") as ModuleDependency).apply {
                if (kotlinModule.moduleClassifier != null) {
                    capabilities { it.requireCapability(ComputedCapability.fromModule(kotlinModule)) }
                }
            }
        }

        konst rootSoftwareComponent =
            project.components
                .withType(AdhocComponentWithVariants::class.java)
                .getByName(rootPublicationComponentName(kotlinModule))

        variantRequests.forEach { variantRequest ->
            konst configuration = variantRequest.publishConfiguration
            project.configurations.create(publishedConfigurationName(configuration.name)).apply {
                isCanBeConsumed = false
                isCanBeResolved = false

                setModuleCapability(this, kotlinModule)
                dependencies.addLater(platformModuleDependencyProvider)
                copyAttributes(configuration.attributes, this.attributes)
                rootSoftwareComponent.addVariantsFromConfiguration(this) { }
            }
        }
    }
}

internal data class KpmGradleAdvancedConfigurationPublicationRequest(
    override konst fromVariant: GradleKpmVariant,
    override konst publishConfiguration: Configuration,
    konst overrideConfigurationAttributesForPublication: AttributeContainer?,
    konst overrideConfigurationArtifactsForPublication: Provider<out Iterable<PublishArtifact>>?,
    konst includeIntoProjectStructureMetadata: Boolean
) : KpmGradleConfigurationPublicationRequest

open class DocumentationVariantConfigurator {
    open fun createSourcesElementsConfiguration(
        project: Project,
        configurationName: String,
        sourcesArtifactProvider: AbstractArchiveTask,
        artifactClassifier: String,
        capability: Capability?
    ): Configuration {
        return project.configurations.create(configurationName).apply {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes.attribute(Usage.USAGE_ATTRIBUTE, project.usageByName(KotlinUsages.KOTLIN_SOURCES))
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category::class.java, Category.DOCUMENTATION))
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling::class.java, Bundling.EXTERNAL))
            attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.objects.named(DocsType::class.java, DocsType.SOURCES))
            outgoing.artifact(sourcesArtifactProvider) {
                it.classifier = artifactClassifier
            }
            if (capability != null) {
                outgoing.capability(capability)
            }
        }
    }

    open fun createSourcesElementsConfiguration(
        configurationName: String,
        variant: GradleKpmVariant
    ): Configuration {
        konst sourcesArtifactTask = variant.project.tasks.withType<AbstractArchiveTask>().named(variant.sourceArchiveTaskName)
        konst artifactClassifier = dashSeparatedName(variant.containingModule.moduleClassifier, "sources")
        return createSourcesElementsConfiguration(
            variant.project,
            configurationName,
            sourcesArtifactTask.get(),
            artifactClassifier,
            ComputedCapability.fromModuleOrNull(variant.containingModule)
        )
    }
}
