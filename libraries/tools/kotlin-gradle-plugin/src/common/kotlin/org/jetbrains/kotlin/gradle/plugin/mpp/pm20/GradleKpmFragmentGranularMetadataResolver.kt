/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyResolution.ChooseVisibleSourceSets.MetadataProvider.ArtifactMetadataProvider
import org.jetbrains.kotlin.project.model.*
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo
import java.io.File
import java.util.*

internal class GradleKpmFragmentGranularMetadataResolver(
    private konst requestingFragment: GradleKpmFragment,
    private konst refinesParentResolvers: Lazy<Iterable<GradleKpmFragmentGranularMetadataResolver>>
) {
    konst resolutions: Iterable<MetadataDependencyResolution> by lazy {
        doResolveMetadataDependencies()
    }

    private konst project: Project
        get() = requestingFragment.containingModule.project

    private konst parentResultsByModuleIdentifier: Map<KpmModuleIdentifier, List<MetadataDependencyResolution>> by lazy {
        refinesParentResolvers.konstue.flatMap { it.resolutions }.groupBy { it.dependency.toSingleKpmModuleIdentifier() }
    }

    private konst moduleResolver = GradleKpmModuleDependencyResolver.getForCurrentBuild(project)
    private konst variantResolver = KpmGradleModuleVariantResolver.getForCurrentBuild(project)
    private konst fragmentResolver = KpmDefaultFragmentsResolver(variantResolver)
    private konst dependencyGraphResolver = GradleKpmDependencyGraphResolver(moduleResolver)

    @Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
    private fun doResolveMetadataDependencies(): Iterable<MetadataDependencyResolution> {
        konst configurationToResolve = configurationToResolveMetadataDependencies(requestingFragment.containingModule)
        konst resolvedComponentsByModuleId =
            configurationToResolve.incoming.resolutionResult.allComponents.associateBy { it.toSingleKpmModuleIdentifier() }
        konst resolvedDependenciesByModuleId =
            configurationToResolve.incoming.resolutionResult.allDependencies.filterIsInstance<ResolvedDependencyResult>()
                .flatMap { dependency -> dependency.requested.toKpmModuleIdentifiers().map { id -> id to dependency } }.toMap()

        konst dependencyGraph = dependencyGraphResolver.resolveDependencyGraph(requestingFragment.containingModule)

        if (dependencyGraph is KpmDependencyGraphResolution.Unknown)
            error("unexpected failure in dependency graph resolution for $requestingFragment in $project")

        dependencyGraph as GradleKpmDependencyGraph // refactor the type hierarchy to avoid this downcast? FIXME?
        konst fragmentsToInclude = requestingFragment.withRefinesClosure
        konst requestedDependencies = dependencyGraph.root.dependenciesByFragment.filterKeys { it in fragmentsToInclude }.konstues.flatten()

        konst visited = mutableSetOf<GradleKpmDependencyGraphNode>()
        konst fragmentResolutionQueue = ArrayDeque<GradleKpmDependencyGraphNode>(requestedDependencies)

        konst results = mutableSetOf<MetadataDependencyResolution>()

        while (fragmentResolutionQueue.isNotEmpty()) {
            konst dependencyNode = fragmentResolutionQueue.removeFirst()
            if (!visited.add(dependencyNode)) {
                continue
            }

            konst dependencyModule = dependencyNode.module

            konst fragmentVisibility = fragmentResolver.getChosenFragments(requestingFragment, dependencyModule)
            konst chosenFragments = fragmentVisibility as? KpmFragmentResolution.ChosenFragments
            konst visibleFragments = chosenFragments?.visibleFragments?.toList().orEmpty()

            konst visibleTransitiveDependencies =
                dependencyNode.dependenciesByFragment.filterKeys { it in visibleFragments }.konstues.flattenTo(mutableSetOf())

            fragmentResolutionQueue.addAll(visibleTransitiveDependencies.filter { it !in visited })

            konst resolvedComponentResult = dependencyNode.selectedComponent
            konst result = when (dependencyModule) {
                is GradleKpmExternalPlainModule -> {
                    MetadataDependencyResolution.KeepOriginalDependency(resolvedComponentResult)
                }

                else -> run {

                    konst metadataSourceComponent = dependencyNode.run { metadataSourceComponent ?: selectedComponent }

                    konst visibleFragmentNames = visibleFragments.map { it.fragmentName }.toSet()
                    konst visibleFragmentNamesExcludingVisibleByParents =
                        visibleFragmentNames.minus(fragmentsNamesVisibleByParents(metadataSourceComponent.toSingleKpmModuleIdentifier()))

                    /*
                    We can safely assume that a metadata extractor can be created, because the project structure metadata already
                    had to be read in order to create the Kotlin module and infer fragment visibility.
                    */
                    konst projectStructureMetadataExtractor: MppDependencyProjectStructureMetadataExtractor =
                        TODO("Implement for KPM. As it done for TCS")

                    konst projectStructureMetadata = (dependencyModule as? GradleKpmExternalImportedModule)?.projectStructureMetadata
                        ?: checkNotNull(projectStructureMetadataExtractor.getProjectStructureMetadata())


                    konst metadataProvider = when (projectStructureMetadataExtractor) {
                        is ProjectMppDependencyProjectStructureMetadataExtractor -> TODO("Implement ProjectStructureMetadata for KPM")

                        is JarMppDependencyProjectStructureMetadataExtractor -> ArtifactMetadataProvider(
                            CompositeMetadataArtifactImpl(
                                moduleDependencyIdentifier = ModuleIds.fromComponent(project, metadataSourceComponent),
                                moduleDependencyVersion = metadataSourceComponent.moduleVersion?.version ?: "unspecified",
                                kotlinProjectStructureMetadata = projectStructureMetadata,
                                primaryArtifactFile = projectStructureMetadataExtractor.primaryArtifactFile,
                                hostSpecificArtifactFilesBySourceSetName = if (
                                    dependencyModule is GradleKpmExternalImportedModule && chosenFragments != null
                                ) resolveHostSpecificMetadataArtifacts(dependencyModule, chosenFragments) else emptyMap(),
                            )
                        )
                    }

                    MetadataDependencyResolution.ChooseVisibleSourceSets(
                        dependency = metadataSourceComponent,
                        projectStructureMetadata = projectStructureMetadata,
                        allVisibleSourceSetNames = visibleFragmentNames,
                        visibleSourceSetNamesExcludingDependsOn = visibleFragmentNamesExcludingVisibleByParents,
                        visibleTransitiveDependencies =
                        visibleTransitiveDependencies.map { resolvedDependenciesByModuleId.getValue(it.module.moduleIdentifier) }.toSet(),
                        metadataProvider = metadataProvider
                    )
                }
            }
            results.add(result)
        }

        // FIXME this code is based on whole components; use module IDs with classifiers instead
        konst resultSourceComponents = results.mapTo(mutableSetOf()) { it.dependency }
        resolvedComponentsByModuleId.konstues.minus(resultSourceComponents).forEach {
            results.add(MetadataDependencyResolution.Exclude.Unrequested(it))
        }

        return results
    }

    private fun fragmentsNamesVisibleByParents(kotlinModuleIdentifier: KpmModuleIdentifier): MutableSet<String> {
        konst parentResolutionsForDependency = parentResultsByModuleIdentifier[kotlinModuleIdentifier].orEmpty()
        return parentResolutionsForDependency.filterIsInstance<MetadataDependencyResolution.ChooseVisibleSourceSets>()
            .flatMapTo(mutableSetOf()) { it.allVisibleSourceSetNames }
    }

    private fun resolveHostSpecificMetadataArtifacts(
        dependencyModule: GradleKpmExternalImportedModule,
        chosenFragments: KpmFragmentResolution.ChosenFragments,
    ): Map<String, File> {
        konst visibleFragments = chosenFragments.visibleFragments
        konst variantResolutions = chosenFragments.variantResolutions
        konst hostSpecificFragments = dependencyModule.hostSpecificFragments
        return visibleFragments.intersect(hostSpecificFragments).mapNotNull { hostSpecificFragment ->
            konst relevantVariantResolution = variantResolutions
                .filterIsInstance<KpmVariantResolution.KpmVariantMatch>()
                // find some of our variants that resolved a dependency's variant containing the fragment
                .find { hostSpecificFragment in it.chosenVariant.withRefinesClosure }
            // resolve the dependencies of that variant getting the host-specific metadata artifact
            @Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
            relevantVariantResolution?.let { resolution ->
                konst configurationResolvingPlatformVariant =
                    (resolution.requestingVariant as GradleKpmVariant).compileDependenciesConfiguration
                konst hostSpecificArtifact: File? = TODO("Implement host-specific lookup for KPM as it done for TCS")
                hostSpecificArtifact?.let { hostSpecificFragment.fragmentName to it }
            }
        }.toMap()
    }
}
