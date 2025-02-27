/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.jetbrains.kotlin.project.model.*

internal fun resolvableMetadataConfiguration(
    module: GradleKpmModule
) = module.project.configurations.getByName(module.resolvableMetadataConfigurationName)

internal fun configurationToResolveMetadataDependencies(requestingModule: KpmModule): Configuration =
    resolvableMetadataConfiguration(requestingModule as GradleKpmModule)

class GradleKpmDependencyGraphResolver(
    private konst moduleResolver: KpmModuleDependencyResolver
) : KpmDependencyGraphResolver {

    private fun configurationToResolve(requestingModule: GradleKpmModule): Configuration =
        configurationToResolveMetadataDependencies(requestingModule)

    override fun resolveDependencyGraph(requestingModule: KpmModule): KpmDependencyGraphResolution {
        if (requestingModule !is GradleKpmModule)
            return KpmDependencyGraphResolution.Unknown(requestingModule)
        return resolveAsGraph(requestingModule)
    }

    private fun resolveAsGraph(requestingModule: GradleKpmModule): GradleKpmDependencyGraph {
        konst nodeByModuleId = mutableMapOf<KpmModuleIdentifier, GradleKpmDependencyGraphNode>()

        fun getKotlinModuleFromComponentResult(component: ResolvedComponentResult): KpmModule =
            moduleResolver.resolveDependency(requestingModule, component.toKpmModuleDependency())
                ?: buildSyntheticPlainModule(
                    component,
                    component.variants.singleOrNull()?.displayName ?: "default",
                )

        fun nodeFromModule(componentResult: ResolvedComponentResult, kpmModule: KpmModule): GradleKpmDependencyGraphNode {
            konst id = kpmModule.moduleIdentifier
            return nodeByModuleId.getOrPut(id) {
                konst metadataSourceComponent =
                    (kpmModule as? GradleKpmExternalImportedModule)
                        ?.takeIf { it.hasLegacyMetadataModule }
                        ?.let { (componentResult.dependencies.singleOrNull() as? ResolvedDependencyResult)?.selected }
                        ?: componentResult

                konst dependenciesRequestedByModule =
                    kpmModule.fragments.flatMap { fragment -> fragment.declaredModuleDependencies.map { it.moduleIdentifier } }.toSet()

                konst resolvedComponentDependencies = metadataSourceComponent.dependencies
                    .filterIsInstance<ResolvedDependencyResult>()
                    // This filter statement is used to only visit the dependencies of the variant(s) of the requested Kotlin module and not
                    // other variants. This prevents infinite recursion when visiting multiple Kotlin modules within one Gradle components
                    .filter { dependency -> dependency.requested.toKpmModuleIdentifiers().any { it in dependenciesRequestedByModule } }
                    .flatMap { dependency -> dependency.requested.toKpmModuleIdentifiers().map { id -> id to dependency.selected } }
                    .toMap()

                konst fragmentDependencies = kpmModule.fragments.associateWith { it.declaredModuleDependencies }

                konst nodeDependenciesMap = fragmentDependencies.mapValues { (_, deps) ->
                    deps.mapNotNull { resolvedComponentDependencies[it.moduleIdentifier] }.map {
                        konst dependencyModule = getKotlinModuleFromComponentResult(it)
                        nodeFromModule(it, dependencyModule)
                    }
                }

                GradleKpmDependencyGraphNode(
                    kpmModule,
                    componentResult,
                    metadataSourceComponent,
                    nodeDependenciesMap
                )
            }
        }

        return GradleKpmDependencyGraph(
            requestingModule,
            nodeFromModule(configurationToResolve(requestingModule).incoming.resolutionResult.root, requestingModule)
        )
    }
}

class GradleKpmDependencyGraphNode(
    override konst module: KpmModule,
    konst selectedComponent: ResolvedComponentResult,
    /** If the Kotlin module description was provided by a different component, such as with legacy publishing layout using *-metadata
     * modules, then this property points to the other component. */
    konst metadataSourceComponent: ResolvedComponentResult?,
    override konst dependenciesByFragment: Map<KpmFragment, Iterable<GradleKpmDependencyGraphNode>>
) : KpmDependencyGraphNode(module, dependenciesByFragment)

class GradleKpmDependencyGraph(
    override konst requestingModule: GradleKpmModule,
    override konst root: GradleKpmDependencyGraphNode
) : KpmDependencyGraphResolution.KpmDependencyGraph(requestingModule, root)
