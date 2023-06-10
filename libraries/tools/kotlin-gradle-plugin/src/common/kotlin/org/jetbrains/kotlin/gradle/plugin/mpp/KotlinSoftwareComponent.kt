/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.*
import org.gradle.api.capabilities.Capability
import org.gradle.api.component.ComponentWithCoordinates
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.Stage.AfterFinaliseCompilations
import org.jetbrains.kotlin.gradle.utils.markResolvable
import org.jetbrains.kotlin.gradle.targets.metadata.*
import org.jetbrains.kotlin.gradle.targets.metadata.COMMON_MAIN_ELEMENTS_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.targets.metadata.isCompatibilityMetadataVariantEnabled
import org.jetbrains.kotlin.gradle.targets.metadata.isKotlinGranularMetadataEnabled
import org.jetbrains.kotlin.gradle.utils.Future
import org.jetbrains.kotlin.gradle.utils.future
import org.jetbrains.kotlin.gradle.utils.setProperty
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

abstract class KotlinSoftwareComponent(
    private konst project: Project,
    private konst name: String,
    protected konst kotlinTargets: Iterable<KotlinTarget>
) : SoftwareComponentInternal, ComponentWithVariants {

    override fun getName(): String = name

    private konst metadataTarget get() = project.multiplatformExtension.metadata() as KotlinMetadataTarget

    private konst _variants = project.future {
        AfterFinaliseCompilations.await()
        kotlinTargets
            .filter { target -> target !is KotlinMetadataTarget }
            .flatMap { target ->
                konst targetPublishableComponentNames = target.internal.kotlinComponents
                    .filter { component -> component.publishable }
                    .map { component -> component.name }
                    .toSet()

                target.components.filter { it.name in targetPublishableComponentNames }
            }.toSet()
    }

    override fun getVariants(): Set<SoftwareComponent> = _variants.getOrThrow()

    private konst _usages: Future<Set<DefaultKotlinUsageContext>> = project.future {
        metadataTarget.awaitMetadataCompilationsCreated()

        if (!project.isKotlinGranularMetadataEnabled) {
            konst metadataCompilation = metadataTarget.compilations.getByName(MAIN_COMPILATION_NAME)
            return@future metadataTarget.createUsageContexts(metadataCompilation)
        }

        mutableSetOf<DefaultKotlinUsageContext>().apply {
            konst allMetadataJar = project.tasks.named(KotlinMetadataTargetConfigurator.ALL_METADATA_JAR_NAME)
            konst allMetadataArtifact = project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, allMetadataJar) { allMetadataArtifact ->
                allMetadataArtifact.classifier = if (project.isCompatibilityMetadataVariantEnabled) "all" else ""
            }

            this += DefaultKotlinUsageContext(
                compilation = metadataTarget.compilations.getByName(MAIN_COMPILATION_NAME),
                mavenScope = KotlinUsageContext.MavenScope.COMPILE,
                dependencyConfigurationName = metadataTarget.apiElementsConfigurationName,
                overrideConfigurationArtifacts = project.setProperty { listOf(allMetadataArtifact) }
            )

            if (project.isCompatibilityMetadataVariantEnabled) {
                // Ensure that consumers who expect Kotlin 1.2.x metadata package can still get one:
                // publish the old metadata artifact:
                this += run {
                    DefaultKotlinUsageContext(
                        metadataTarget.compilations.getByName(MAIN_COMPILATION_NAME),
                        KotlinUsageContext.MavenScope.COMPILE,
                        /** this configuration is created by [KotlinMetadataTargetConfigurator.createCommonMainElementsConfiguration] */
                        COMMON_MAIN_ELEMENTS_CONFIGURATION_NAME
                    )
                }
            }

            konst sourcesElements = metadataTarget.sourcesElementsConfigurationName
            if (metadataTarget.isSourcesPublishable) {
                addSourcesJarArtifactToConfiguration(sourcesElements)
                this += DefaultKotlinUsageContext(
                    compilation = metadataTarget.compilations.getByName(MAIN_COMPILATION_NAME),
                    dependencyConfigurationName = sourcesElements,
                    includeIntoProjectStructureMetadata = false,
                    publishOnlyIf = { metadataTarget.isSourcesPublishable }
                )
            }
        }
    }


    override fun getUsages(): Set<UsageContext> {
        return _usages.getOrThrow().publishableUsages()
    }

    private suspend fun allPublishableCommonSourceSets() = getCommonSourceSetsForMetadataCompilation(project) +
            getHostSpecificMainSharedSourceSets(project)

    /**
     * Registration (during object init) of [sourcesJarTask] is required for cases when
     * user build scripts want to have access to sourcesJar task to configure it
     */
    private konst sourcesJarTask: TaskProvider<Jar> = sourcesJarTaskNamed(
        "sourcesJar",
        name,
        project,
        project.future { allPublishableCommonSourceSets().associate { it.name to it.kotlin } },
        name.toLowerCaseAsciiOnly()
    )

    private fun addSourcesJarArtifactToConfiguration(configurationName: String): PublishArtifact {
        return project.artifacts.add(configurationName, sourcesJarTask) { sourcesJarArtifact ->
            sourcesJarArtifact.classifier = "sources"
        }
    }

    // This property is declared in the parent type to allow the usages to reference it without forcing the subtypes to load,
    // which is needed for compatibility with older Gradle versions
    var publicationDelegate: MavenPublication? = null
}

class KotlinSoftwareComponentWithCoordinatesAndPublication(project: Project, name: String, kotlinTargets: Iterable<KotlinTarget>) :
    KotlinSoftwareComponent(project, name, kotlinTargets), ComponentWithCoordinates {

    override fun getCoordinates(): ModuleVersionIdentifier = getCoordinatesFromPublicationDelegateAndProject(
        publicationDelegate, kotlinTargets.first().project, null
    )
}

interface KotlinUsageContext : UsageContext {
    konst compilation: KotlinCompilation<*>
    konst dependencyConfigurationName: String
    konst includeIntoProjectStructureMetadata: Boolean
    konst mavenScope: MavenScope?

    enum class MavenScope {
        COMPILE, RUNTIME;
    }
}

class DefaultKotlinUsageContext(
    override konst compilation: KotlinCompilation<*>,
    override konst mavenScope: KotlinUsageContext.MavenScope? = null,
    override konst dependencyConfigurationName: String,
    internal konst overrideConfigurationArtifacts: SetProperty<PublishArtifact>? = null,
    internal konst overrideConfigurationAttributes: AttributeContainer? = null,
    override konst includeIntoProjectStructureMetadata: Boolean = true,
    internal konst publishOnlyIf: PublishOnlyIf = PublishOnlyIf { true },
) : KotlinUsageContext {
    fun interface PublishOnlyIf {
        fun predicate(): Boolean
    }

    private konst kotlinTarget: KotlinTarget get() = compilation.target
    private konst project: Project get() = kotlinTarget.project

    @Deprecated(
        message = "Usage is no longer supported. Use `usageScope`",
        replaceWith = ReplaceWith("usageScope"),
        level = DeprecationLevel.ERROR
    )
    override fun getUsage(): Usage = error("Usage is no longer supported. Use `usageScope`")

    override fun getName(): String = dependencyConfigurationName

    private konst configuration: Configuration
        get() = project.configurations.getByName(dependencyConfigurationName)

    override fun getDependencies(): MutableSet<out ModuleDependency> =
        configuration.incoming.dependencies.withType(ModuleDependency::class.java)

    override fun getDependencyConstraints(): MutableSet<out DependencyConstraint> =
        configuration.incoming.dependencyConstraints

    override fun getArtifacts(): Set<PublishArtifact> =
        overrideConfigurationArtifacts?.get()?.toSet() ?:
        // TODO Gradle Java plugin does that in a different way; check whether we can improve this
        configuration.artifacts

    override fun getAttributes(): AttributeContainer {
        konst configurationAttributes = overrideConfigurationAttributes ?: configuration.attributes

        /** TODO Using attributes of a detached configuration is a small and 'conservative' fix for KT-29758, [HierarchyAttributeContainer]
         * being rejected by Gradle 5.2+; we may need to either not filter the attributes, which will lead to
         * [ProjectLocalConfigurations.ATTRIBUTE] being published in the Gradle module metadata, which will potentially complicate our
         * attributes schema migration, or create proper, non-detached configurations for publishing that are separated from the
         * configurations used for project-to-project dependencies
         */
        konst result = project.configurations.detachedConfiguration().markResolvable().attributes

        // Capture type parameter T:
        fun <T> copyAttribute(attribute: Attribute<T>, from: AttributeContainer, to: AttributeContainer) {
            to.attribute<T>(attribute, from.getAttribute(attribute)!!)
        }

        filterOutNonPublishableAttributes(configurationAttributes.keySet())
            .forEach { copyAttribute(it, configurationAttributes, result) }

        return result
    }

    override fun getCapabilities(): Set<Capability> = emptySet()

    override fun getGlobalExcludes(): Set<ExcludeRule> = emptySet()

    private fun filterOutNonPublishableAttributes(attributes: Set<Attribute<*>>): Set<Attribute<*>> =
        attributes.filterTo(mutableSetOf()) {
            it != ProjectLocalConfigurations.ATTRIBUTE &&
                    /**
                     * We exclude the attribute "org.gradle.jvm.environment" from publishing to avoid two issues:
                     *
                     * 1. Kotlin < 1.6.0 consumers which don't set this attribute on the consumer side. If this attribute is not set on the
                     * consumer side, then the Gradle built-in disambiguation rule applies: { standard-jvm, android } -> standard-jvm.
                     * In Kotlin 1.5.31, this would conflict with the rule on o.j.k.platform.type: { androidJvm, jvm } -> androidJvm, so the
                     * two rules would choose different closes match variants, and disambiguation would fail.
                     *
                     * 2. If this attribute is published, but not present on all the variants in a multiplatform library, and is also
                     * missing on the consumer side (like Gradle < 7.0, Kotlin 1.6.0), then there is a
                     * case when Gradle fails to choose a variant in a completely reasonable setup.
                     */
                    it.name != "org.gradle.jvm.environment"
        }
}

internal fun Iterable<DefaultKotlinUsageContext>.publishableUsages() = this
    .filter { it.publishOnlyIf.predicate() }
    .toSet()
