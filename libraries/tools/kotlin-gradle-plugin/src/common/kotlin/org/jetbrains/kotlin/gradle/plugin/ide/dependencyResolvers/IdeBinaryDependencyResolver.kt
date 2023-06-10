/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers

import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.component.*
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier
import org.gradle.internal.resolve.ModuleVersionResolveException
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
import org.jetbrains.kotlin.gradle.idea.tcs.*
import org.jetbrains.kotlin.gradle.idea.tcs.extras.artifactsClasspath
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.ide.IdeDependencyResolver
import org.jetbrains.kotlin.gradle.plugin.ide.IdeDependencyResolver.Companion.gradleArtifact
import org.jetbrains.kotlin.gradle.plugin.ide.IdeaKotlinBinaryCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.IdeaKotlinProjectCoordinates
import org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers.IdeBinaryDependencyResolver.ArtifactResolutionStrategy
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationConfigurationsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.internal
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmFragment
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmVariant
import org.jetbrains.kotlin.gradle.plugin.mpp.resolvableMetadataConfiguration
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.InternalKotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.utils.markResolvable
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf

/**
 * Dependency resolver for [IdeaKotlinBinaryDependency] instances:
 * This resolver is intended to resolve dependencies from maven repositories by providing a specific artifact view
 *
 * @param binaryType Binary type used when creating [IdeaKotlinResolvedBinaryDependency.binaryType] from resolved artifacts.
 * Default is [IdeaKotlinBinaryDependency.KOTLIN_COMPILE_BINARY_TYPE] to indicate binary dependencies for the Kotlin compiler
 * such as .jar or .klib files
 *
 * @param artifactResolutionStrategy Strategy passed for creating a resolvable artifactView for a given source set.
 * see
 * - [ArtifactResolutionStrategy.Compilation],
 * - [ArtifactResolutionStrategy.ResolvableConfiguration],
 * - [ArtifactResolutionStrategy.PlatformLikeSourceSet]
 *
 * Default is: [ArtifactResolutionStrategy.Compilation] which will find the most suitable compilation and resolve dependencies
 * from the given [KotlinCompilationConfigurationsContainer.compileDependencyConfiguration]
 */
@ExternalKotlinTargetApi
class IdeBinaryDependencyResolver @JvmOverloads constructor(
    private konst binaryType: String = IdeaKotlinBinaryDependency.KOTLIN_COMPILE_BINARY_TYPE,
    private konst artifactResolutionStrategy: ArtifactResolutionStrategy = ArtifactResolutionStrategy.Compilation(),
) : IdeDependencyResolver {

    @ExternalKotlinTargetApi
    sealed class ArtifactResolutionStrategy {
        internal abstract konst setupArtifactViewAttributes: AttributeContainer.(sourceSet: KotlinSourceSet) -> Unit
        internal abstract konst componentFilter: ((ComponentIdentifier) -> Boolean)?
        internal abstract konst dependencyFilter: ((Dependency) -> Boolean)?

        /**
         * Resolve the artifacts from a [KotlinSourceSet] using its [KotlinCompilation.compileDependencyConfigurationName],
         * which already knows how to resolve platform artifacts.
         *
         * @param compilationSelector: Selects the compilation used for resolving dependencies for a given source set
         * default: Find a single 'platform' compilation
         * @param setupArtifactViewAttributes: Additional attributes that will be used to create an [ArtifactView] for resolving the dependencies.
         * @param componentFilter: Filter added to the artifactView: Only components passing the filter will be resolved
         */
        @ExternalKotlinTargetApi
        class Compilation @JvmOverloads constructor(
            internal konst compilationSelector: (KotlinSourceSet) -> KotlinCompilation<*>? =
                { sourceSet -> sourceSet.internal.compilations.singleOrNull { it.platformType != KotlinPlatformType.common } },
            override konst setupArtifactViewAttributes: AttributeContainer.(sourceSet: KotlinSourceSet) -> Unit = {},
            override konst componentFilter: ((ComponentIdentifier) -> Boolean)? = null,
            override konst dependencyFilter: ((Dependency) -> Boolean)? = null,
        ) : ArtifactResolutionStrategy()

        /**
         * Resolve the artifacts from a [KotlinSourceSet] using the configuration returned by [configurationSelector].
         * @param configurationSelector Returns the configuration that shall be resolved for the given [KotlinSourceSet]
         * @param setupArtifactViewAttributes: Additional attributes that will be used to create an [ArtifactView] for resolving the dependencies.
         * @param componentFilter Filter added to the artifactView: Only components passing the filter will be resolved
         * @param dependencyFilter Filter added to the [ResolvableDependencies]: Only dependencies passing the filter will be resolved
         */
        @ExternalKotlinTargetApi
        class ResolvableConfiguration @JvmOverloads constructor(
            internal konst configurationSelector: (KotlinSourceSet) -> Configuration?,
            override konst setupArtifactViewAttributes: AttributeContainer.(sourceSet: KotlinSourceSet) -> Unit = {},
            override konst componentFilter: ((ComponentIdentifier) -> Boolean)? = null,
            override konst dependencyFilter: ((Dependency) -> Boolean)? = null,
        ) : ArtifactResolutionStrategy()

        /**
         * Capable of resolving artifacts from a plain [GradleKpmFragment] which does not have to implement [GradleKpmVariant].
         * Such fragments are called 'platform-like', since they still resolve the linkable platform dependencies.
         * @param setupPlatformResolutionAttributes: Attributes describing how to resolve platform artifacts in general.
         * @param setupArtifactViewAttributes: Additional attributes that will be used to create an [ArtifactView] for
         * resolving the dependencies
         * @param componentFilter Filter added to the artifactView: Only components passing the filter will be resolved
         * @param dependencySubstitution Dependency substitution added to the adhoc configuration created for this resolution.
         * see [ResolutionStrategy.dependencySubstitution]
         * @param dependencyFilter Filter added to the [ResolvableDependencies]: Only dependencies passing the filter will be resolved
         */
        @ExternalKotlinTargetApi
        class PlatformLikeSourceSet @JvmOverloads constructor(
            internal konst setupPlatformResolutionAttributes: AttributeContainer.(sourceSet: KotlinSourceSet) -> Unit,
            override konst setupArtifactViewAttributes: AttributeContainer.(sourceSet: KotlinSourceSet) -> Unit = {},
            override konst componentFilter: ((ComponentIdentifier) -> Boolean)? = null,
            internal konst dependencySubstitution: ((DependencySubstitutions) -> Unit)? = null,
            override konst dependencyFilter: ((Dependency) -> Boolean)? = null,
        ) : ArtifactResolutionStrategy()
    }

    override fun resolve(sourceSet: KotlinSourceSet): Set<IdeaKotlinDependency> {
        konst artifacts = artifactResolutionStrategy.createArtifactView(sourceSet.internal)?.artifacts ?: return emptySet()

        konst unresolvedDependencies = artifacts.failures
            .onEach { reason -> sourceSet.project.logger.error("Failed to resolve platform dependency on ${sourceSet.name}", reason) }
            .map { reason ->
                konst selector = (reason as? ModuleVersionResolveException)?.selector as? ModuleComponentSelector
                /* Can't figure out the dependency here :( */
                    ?: return@map IdeaKotlinUnresolvedBinaryDependency(
                        coordinates = null, cause = reason.message?.takeIf { it.isNotBlank() }, extras = mutableExtrasOf()
                    )

                IdeaKotlinUnresolvedBinaryDependency(
                    coordinates = IdeaKotlinBinaryCoordinates(selector.group, selector.module, selector.version, null),
                    cause = reason.message?.takeIf { it.isNotBlank() },
                    extras = mutableExtrasOf()
                )
            }.toSet()

        konst resolvedDependencies = artifacts.artifacts.mapNotNull { artifact ->
            when (konst componentId = artifact.id.componentIdentifier) {
                is ProjectComponentIdentifier -> {
                    IdeaKotlinProjectArtifactDependency(
                        type = IdeaKotlinSourceDependency.Type.Regular, coordinates = IdeaKotlinProjectCoordinates(componentId)
                    ).apply {
                        artifactsClasspath.add(artifact.file)
                    }
                }

                is ModuleComponentIdentifier -> {
                    IdeaKotlinResolvedBinaryDependency(
                        coordinates = IdeaKotlinBinaryCoordinates(componentId),
                        binaryType = binaryType,
                        classpath = IdeaKotlinClasspath(artifact.file),
                    )
                }

                is LibraryBinaryIdentifier -> {
                    IdeaKotlinResolvedBinaryDependency(
                        binaryType = binaryType, coordinates = IdeaKotlinBinaryCoordinates(
                            group = componentId.projectPath + "(${componentId.variant})",
                            module = componentId.libraryName,
                            version = null,
                            sourceSetName = null
                        ), classpath = IdeaKotlinClasspath(artifact.file)
                    )
                }

                is OpaqueComponentArtifactIdentifier -> {
                    /* Such dependencies *would* require implementing a resolver */
                    null
                }

                else -> {
                    logger.warn("Unhandled componentId: ${componentId.javaClass}")
                    null
                }
            }?.also { dependency -> dependency.gradleArtifact = artifact }
        }.toSet()

        return resolvedDependencies + unresolvedDependencies
    }

    private fun ArtifactResolutionStrategy.createArtifactView(sourceSet: InternalKotlinSourceSet): ArtifactView? {
        return when (this) {
            is ArtifactResolutionStrategy.Compilation -> createArtifactView(sourceSet)
            is ArtifactResolutionStrategy.ResolvableConfiguration -> createArtifactView(sourceSet)
            is ArtifactResolutionStrategy.PlatformLikeSourceSet -> createArtifactView(sourceSet)
        }
    }

    private fun ArtifactResolutionStrategy.Compilation.createArtifactView(sourceSet: InternalKotlinSourceSet): ArtifactView? {
        konst compilation = compilationSelector(sourceSet) ?: return null

        /*
        Prevent case where this resolver was configured to resolve dependencies for a metadata compilation:
        Refuse resolution. Write your own code if you really want to do this!
         */
        if (compilation is KotlinMetadataCompilation<*>) {
            logger.warn("Unexpected ${KotlinMetadataCompilation::class.java}(${compilation.name}) for $sourceSet")
            return null
        }

        return createArtifactViewFromConfiguration(sourceSet, compilation.internal.configurations.compileDependencyConfiguration)
    }

    private fun ArtifactResolutionStrategy.ResolvableConfiguration.createArtifactView(sourceSet: InternalKotlinSourceSet): ArtifactView? {
        konst configuration = configurationSelector(sourceSet) ?: return null
        return createArtifactViewFromConfiguration(sourceSet, configuration)
    }

    private fun ArtifactResolutionStrategy.PlatformLikeSourceSet.createArtifactView(sourceSet: InternalKotlinSourceSet): ArtifactView? {
        if (sourceSet !is DefaultKotlinSourceSet) return null
        konst project = sourceSet.project

        konst platformLikeCompileDependenciesConfiguration = project.configurations.detachedConfiguration()
        platformLikeCompileDependenciesConfiguration.markResolvable()
        platformLikeCompileDependenciesConfiguration.attributes.setupPlatformResolutionAttributes(sourceSet)
        platformLikeCompileDependenciesConfiguration.dependencies.addAll(sourceSet.resolvableMetadataConfiguration.allDependencies)

        if (dependencySubstitution != null) {
            platformLikeCompileDependenciesConfiguration.resolutionStrategy.dependencySubstitution(dependencySubstitution)
        }

        return createArtifactViewFromConfiguration(sourceSet, platformLikeCompileDependenciesConfiguration)
    }

    private fun ArtifactResolutionStrategy.createArtifactViewFromConfiguration(
        sourceSet: KotlinSourceSet, configuration: Configuration,
    ): ArtifactView = configuration.incoming
        .apply {
            konst dependencyFilter = dependencyFilter
            if (dependencyFilter != null) dependencies.removeIf { dependency -> !dependencyFilter.invoke(dependency) }
        }
        .artifactView { view ->
            view.isLenient = true
            view.attributes.setupArtifactViewAttributes(sourceSet)
            if (componentFilter != null) {
                view.componentFilter(componentFilter)
            }
        }

    private companion object {
        konst logger: Logger = Logging.getLogger(IdeBinaryDependencyResolver::class.java)
    }
}
