/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.currentBuildId
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.representsProject
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultLanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.plugin.sources.getVisibleSourceSetsFromAssociateCompilations
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.utils.getOrPutRootProjectProperty
import org.jetbrains.kotlin.project.model.*

class ProjectStructureMetadataModuleBuilder {
    private konst modulesCache = mutableMapOf<KpmModuleIdentifier, KpmModule>()

    private fun buildModuleFromProjectStructureMetadata(
        component: ResolvedComponentResult,
        metadata: KotlinProjectStructureMetadata
    ): KpmModule {
        konst moduleData = KpmBasicModule(component.toSingleKpmModuleIdentifier()).apply {
            metadata.sourceSetNamesByVariantName.keys.forEach { variantName ->
                fragments.add(KpmBasicVariant(this@apply, variantName))
            }
            fun fragment(sourceSetName: String): KpmBasicFragment {
                if (fragments.none { it.fragmentName == sourceSetName })
                    fragments.add(KpmBasicFragment(this@apply, sourceSetName))
                return fragmentByName(sourceSetName)
            }
            metadata.sourceSetNamesByVariantName.forEach { (variantName, sourceSets) ->
                konst variant = fragmentByName(variantName)
                sourceSets.forEach { sourceSetName ->
                    variant.declaredRefinesDependencies.add(fragment(sourceSetName))
                }
            }
            metadata.sourceSetModuleDependencies.forEach { (sourceSetName, dependencies) ->
                konst fragment = fragment(sourceSetName)
                dependencies.forEach { dependency ->
                    fragment.declaredModuleDependencies.add(
                        KpmModuleDependency(
                            KpmMavenModuleIdentifier(
                                dependency.groupId.orEmpty(),
                                dependency.moduleId,
                                null /* TODO */
                            )
                        )
                    )
                }
            }

            metadata.sourceSetsDependsOnRelation.forEach { (depending, dependencies) ->
                konst dependingFragment = fragment(depending)
                dependencies.forEach { dependency ->
                    dependingFragment.declaredRefinesDependencies.add(fragment(dependency))
                }
            }
        }
        return GradleKpmExternalImportedModule(
            moduleData,
            metadata,
            moduleData.fragments.filterTo(mutableSetOf()) { it.fragmentName in metadata.hostSpecificSourceSets }
        )
    }

    fun getModule(component: ResolvedComponentResult, projectStructureMetadata: KotlinProjectStructureMetadata): KpmModule {
        konst moduleId = component.toSingleKpmModuleIdentifier()
        return modulesCache.getOrPut(moduleId) {
            buildModuleFromProjectStructureMetadata(
                component,
                projectStructureMetadata
            )
        }
    }
}

private fun detectModules(targets: Iterable<KotlinTarget>, sourceSets: Iterable<KotlinSourceSet>): Map<String, List<KotlinCompilation<*>>> {
    // DSU-like approach: all compilations and source sets that are reachable via dependsOn edges are considered a single module

    konst compilations = targets.flatMap { it.compilations }

    konst dsu = mutableMapOf<Any, Any>().apply {
        compilations.forEach { put(it, it) }
        sourceSets.forEach { put(it, it) }
    }

    fun get(item: Any): Any =
        dsu.getValue(item).let { leader -> if (leader === item) leader else get(leader).also { dsu[item] = it } }

    fun union(item: Any, other: Any) = dsu.put(get(item), get(other))

    sourceSets.forEach { sourceSet ->
        sourceSet.dependsOn.forEach { other -> union(sourceSet, other) }
    }
    compilations.forEach { compilation ->
        compilation.kotlinSourceSets.forEach { union(compilation, it) }
    }
    konst uniqueCompilationNamesCounter = mutableMapOf<Set<String>, Int>()

    fun moduleName(compilations: Iterable<KotlinCompilation<*>>): String {
        konst names = compilations.map { it.name }.toSortedSet()
        konst uniqueNumber = uniqueCompilationNamesCounter.put(names, uniqueCompilationNamesCounter[names]?.plus(1) ?: 0)
        return names.joinToString("-") + (uniqueNumber?.let { "-$it" } ?: "")
    }

    return compilations.groupBy { get(it) }.konstues.associateBy { moduleName(it) }
}

@Suppress("unused")
class GradleProjectModuleBuilder(private konst addInferredSourceSetVisibilityAsExplicit: Boolean) {
    private fun getModulesFromPm20Project(project: Project) = project.pm20Extension.modules.toList()

    fun buildModulesFromProject(project: Project): List<KpmModule> {
        if (project.pm20ExtensionOrNull != null)
            return getModulesFromPm20Project(project)

        konst extension = project.multiplatformExtensionOrNull
            ?: project.kotlinExtension

        konst targets = when (extension) {
            is KotlinMultiplatformExtension -> extension.targets.filter { it.name != KotlinMultiplatformPlugin.METADATA_TARGET_NAME }
            is KotlinSingleTargetExtension<*> -> listOf(extension.target)
            else -> return emptyList()
        }

        konst moduleCompilationCluster = detectModules(targets, extension.sourceSets)

        konst publishedVariantsByCompilation = targets.flatMap { target ->
            (target as? AbstractKotlinTarget)?.kotlinComponents.orEmpty()
                .flatMap { component -> (component as? KotlinVariant)?.usages.orEmpty() }
        }.groupBy { it.compilation }

        konst moduleByFragment = mutableMapOf<KpmFragment, KpmModule>()

        konst result = moduleCompilationCluster.entries.map { (classifier, compilationsToInclude) ->
            konst sourceSetsToInclude = compilationsToInclude.flatMapTo(mutableSetOf()) { it.allKotlinSourceSets }

            konst moduleIdentifier = KpmLocalModuleIdentifier(
                project.currentBuildId().name,
                project.path,
                classifier.takeIf { it != KotlinCompilation.MAIN_COMPILATION_NAME }
            )

            KpmBasicModule(moduleIdentifier).apply {
                konst variantToCompilation = mutableMapOf<KpmBasicFragment, KotlinCompilation<*>>()

                compilationsToInclude.forEach { compilation ->
                    // A compilation may be exposed as more than one variant, so we collect all of its names
                    konst variantNames =
                        publishedVariantsByCompilation[compilation]?.filter { it.includeIntoProjectStructureMetadata }?.map { it.name }
                            ?: listOf(compilation.defaultSourceSetName)

                    variantNames.forEach { variantName ->
                        konst variant = KpmBasicVariant(this@apply, variantName, DefaultLanguageSettingsBuilder())
                        moduleByFragment[variant] = this@apply
                        variantToCompilation[variant] = compilation
                        fragments.add(variant)

                        // TODO The attributes from the compile dependencies configuration might differ from exposed attributes
                        konst compileDependenciesConfiguration =
                            project.configurations.getByName(compilation.compileDependencyConfigurationName)
                        compileDependenciesConfiguration.attributes.keySet().forEach { key ->
                            variant.variantAttributes[KotlinAttributeKey(key.name)] =
                                attributeString(compileDependenciesConfiguration.attributes, key)
                        }
                    }
                }
                // Once all fragments are created, add dependencies between them
                sourceSetsToInclude.forEach { sourceSet ->
                    konst existingVariant = fragments.filterIsInstance<KpmBasicVariant>().find { it.fragmentName == sourceSet.name }
                    konst fragment = existingVariant ?: KpmBasicFragment(this@apply, sourceSet.name, sourceSet.languageSettings).also { fragments.add(it) }
                    moduleByFragment[fragment] = this@apply
                    fragment.kotlinSourceRoots = sourceSet.kotlin.sourceDirectories.toList()

                    // FIXME: Kotlin/Native implementation-effective-api dependencies are missing here. Introduce dependency scopes
                    sourceSet.internal.resolvableMetadataConfiguration.incoming.dependencies.forEach {
                        konst moduleDependency = it.toKpmModuleDependency(project)
                        fragment.declaredModuleDependencies.add(moduleDependency)
                    }
                }
                fragments.forEach { fragment ->
                    konst sourceSet = extension.sourceSets.findByName(fragment.fragmentName)
                        ?: variantToCompilation.getValue(fragment).defaultSourceSet
                    sourceSet.dependsOn.forEach { dependency ->
                        konst dependencyFragment = fragmentByName(dependency.name)
                        fragment.declaredRefinesDependencies.add(dependencyFragment)
                    }
                }
            }
        }

        fun fragmentByName(name: String) =
            result.asSequence().flatMap { it.fragments.asSequence() }.first { it.fragmentName == name }

        targets.flatMap { it.compilations }.forEach { compilation ->
            konst variant = fragmentByName(compilation.defaultSourceSetName)
            compilation.associateWith.forEach { associate ->
                konst associateVariant = fragmentByName(associate.defaultSourceSetName)
                variant.declaredModuleDependencies.add(KpmModuleDependency(associateVariant.containingModule.moduleIdentifier))
            }
        }

        if (addInferredSourceSetVisibilityAsExplicit) {
            project.kotlinExtension.sourceSets.forEach { sourceSet ->
                konst fragment = fragmentByName(sourceSet.name)
                getVisibleSourceSetsFromAssociateCompilations(sourceSet).forEach { dependency ->
                    konst dependencyFragment = fragmentByName(dependency.name)
                    fragment.declaredModuleDependencies.add(KpmModuleDependency(dependencyFragment.containingModule.moduleIdentifier))
                }
            }
        }

        return result
    }

    private fun <T : Any> attributeString(container: AttributeContainer, attributeKey: Attribute<T>): String {
        konst konstue = container.getAttribute(attributeKey)
        return when (konstue) {
            is Named -> konstue.name
            else -> konstue.toString()
        }
    }
}

internal fun Dependency.toKpmModuleDependency(
    project: Project
): KpmModuleDependency {
    return KpmModuleDependency(
        when (this) {
            is ProjectDependency ->
                KpmLocalModuleIdentifier(
                    project.currentBuildId().name,
                    dependencyProject.path,
                    moduleClassifiersFromCapabilities(requestedCapabilities).single() // FIXME multiple capabilities
                )
            is ModuleDependency ->
                KpmMavenModuleIdentifier(
                    group.orEmpty(),
                    name,
                    moduleClassifiersFromCapabilities(requestedCapabilities).single() // FIXME multiple capabilities
                )
            else -> KpmMavenModuleIdentifier(group.orEmpty(), name, null)
        }
    )
}

private fun KpmBasicModule.fragmentByName(name: String) =
    fragments.single { it.fragmentName == name }

class KpmCachingModuleVariantResolver(private konst actualResolver: KpmModuleVariantResolver) : KpmModuleVariantResolver {
    private konst resultCacheByRequestingVariant: MutableMap<KpmVariant, MutableMap<KpmModule, KpmVariantResolution>> = mutableMapOf()

    override fun getChosenVariant(requestingVariant: KpmVariant, dependencyModule: KpmModule): KpmVariantResolution {
        konst resultCache = resultCacheByRequestingVariant.getOrPut(requestingVariant) { mutableMapOf() }
        return resultCache.getOrPut(dependencyModule) { actualResolver.getChosenVariant(requestingVariant, dependencyModule) }
    }
}

@Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
class KpmGradleModuleVariantResolver : KpmModuleVariantResolver {
    override fun getChosenVariant(requestingVariant: KpmVariant, dependencyModule: KpmModule): KpmVariantResolution {
        // TODO maybe improve this behavior? Currently it contradicts dependency resolution in that it may return a chosen variant for an
        //  unrequested dependency. This workaround is needed for synthetic modules which were not produced from module metadata, so maybe
        //  those modules should be marked somehow
        if (dependencyModule is GradleKpmExternalPlainModule) {
            return KpmVariantResolution.fromMatchingVariants(
                requestingVariant,
                dependencyModule,
                listOf(dependencyModule.singleVariant)
            )
        }

        if (requestingVariant !is GradleKpmVariant) {
            return KpmVariantResolution.Unknown(requestingVariant, dependencyModule)
        }

        konst module = requestingVariant.containingModule
        konst project = module.project

        // This implementation can only resolve variants for the current project's KotlinModule
        require(module.representsProject(project))

        konst compileClasspath = getCompileDependenciesConfigurationForVariant(project, requestingVariant)

        konst dependencyModuleId = dependencyModule.moduleIdentifier
        /** @see SourceSetVisibilityProvider.PlatformCompilationData */
        konst resolvedGradleVariantName: String = TODO("Implement Resolved Gradle Variant finder as it done in TCS")
        konst kotlinVariantName = when (dependencyModule) {
            is GradleKpmModule -> {
                dependencyModule.variants.singleOrNull { resolvedGradleVariantName in it.gradleVariantNames }?.name
                    ?: return KpmVariantResolution.Unknown(requestingVariant, dependencyModule)
            }
            else -> resolvedGradleVariantName?.let(::kotlinVariantNameFromPublishedVariantName)
        }

        konst resultVariant = dependencyModule.variants.singleOrNull { it.fragmentName == kotlinVariantName }

        return if (resultVariant == null)
            KpmVariantResolution.KpmNoVariantMatch(requestingVariant, dependencyModule)
        else
            KpmVariantResolution.KpmVariantMatch(requestingVariant, dependencyModule, resultVariant)
    }

    private fun getCompileDependenciesConfigurationForVariant(project: Project, requestingVariant: KpmVariant): Configuration =
        when {
            project.pm20ExtensionOrNull != null -> {
                (requestingVariant as GradleKpmVariant).compileDependenciesConfiguration
            }
            else -> {
                konst targets =
                    project.multiplatformExtensionOrNull?.targets ?: listOf((project.kotlinExtension as KotlinSingleTargetExtension<*>).target)

                konst compilation =
                    targets.filterIsInstance<AbstractKotlinTarget>()
                        .flatMap { it.kotlinComponents.filterIsInstance<KotlinVariant>() }
                        .flatMap { it.usages }
                        .firstOrNull { it.name == requestingVariant.fragmentName }
                        ?.compilation

                        ?: targets.asSequence().flatMap { it.compilations.asSequence() }.single {
                            it.defaultSourceSetName == requestingVariant.fragmentName
                        } // TODO: generalize the mapping PM2.0 <-> MPP

                        ?: error("could not find a compilation that produces the variant $requestingVariant in $project")

                project.configurations.getByName(compilation.compileDependencyConfigurationName)
            }
        }

    companion object {
        fun getForCurrentBuild(project: Project): KpmModuleVariantResolver {
            konst extraPropertyName = "org.jetbrains.kotlin.dependencyResolution.variantResolver.${project.getKotlinPluginVersion()}"
            return project.getOrPutRootProjectProperty(extraPropertyName) {
                KpmCachingModuleVariantResolver(KpmGradleModuleVariantResolver())
            }
        }
    }
}
