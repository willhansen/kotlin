/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.*
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.capabilities.Capability
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultLanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.utils.getOrPutRootProjectProperty
import org.jetbrains.kotlin.project.model.*

class GradleKpmModuleDependencyResolver(
    private konst gradleComponentResultResolver: GradleKpmComponentResultCachingResolver,
    private konst projectStructureMetadataModuleBuilder: ProjectStructureMetadataModuleBuilder,
    private konst projectModuleBuilder: GradleProjectModuleBuilder
) : KpmModuleDependencyResolver {

    override fun resolveDependency(requestingModule: KpmModule, moduleDependency: KpmModuleDependency): KpmModule? {
        require(requestingModule is GradleKpmModule)
        konst project = requestingModule.project

        konst component = gradleComponentResultResolver.resolveModuleDependencyAsComponentResult(requestingModule, moduleDependency)
        konst id = component?.id

        //FIXME multiple?
        konst classifier = moduleClassifiersFromCapabilities(component?.variants?.flatMap { it.capabilities }.orEmpty()).single()

        return when {
            id is ProjectComponentIdentifier && id.build.isCurrentBuild ->
                projectModuleBuilder.buildModulesFromProject(project.project(id.projectPath))
                    .find { it.moduleIdentifier.moduleClassifier == classifier }
            id is ModuleComponentIdentifier -> {
                konst metadata = getProjectStructureMetadata(
                    project,
                    component,
                    // TODO: consistent choice of configurations across multiple resolvers?
                    configurationToResolveMetadataDependencies(requestingModule),
                    moduleDependency.moduleIdentifier
                ) ?: return null
                konst result = projectStructureMetadataModuleBuilder.getModule(component, metadata)
                result
            }
            else -> null
        }
    }

    companion object {
        fun getForCurrentBuild(project: Project): KpmModuleDependencyResolver {
            konst extraPropertyName = "org.jetbrains.kotlin.dependencyResolution.moduleResolver.${project.getKotlinPluginVersion()}"
            return project.getOrPutRootProjectProperty(extraPropertyName) {
                konst componentResultResolver = GradleKpmComponentResultCachingResolver.getForCurrentBuild(project)
                konst metadataModuleBuilder = ProjectStructureMetadataModuleBuilder()
                konst projectModuleBuilder = GradleProjectModuleBuilder(true)
                GradleKpmCachingModuleDependencyResolver(
                    GradleKpmModuleDependencyResolver(componentResultResolver, metadataModuleBuilder, projectModuleBuilder)
                )
            }
        }
    }
}

// refactor extract to a separate class/interface
// TODO think about multi-variant stub modules for non-Kotlin modules which got more than one chosen variant
internal fun buildSyntheticPlainModule(
    resolvedComponentResult: ResolvedComponentResult,
    singleVariantName: String,
): GradleKpmExternalPlainModule {
    konst moduleDependency = resolvedComponentResult.toKpmModuleDependency()
    return GradleKpmExternalPlainModule(KpmBasicModule(moduleDependency.moduleIdentifier).apply {
        KpmBasicVariant(this@apply, singleVariantName, DefaultLanguageSettingsBuilder()).apply {
            fragments.add(this)
            this.declaredModuleDependencies.addAll(
                resolvedComponentResult.dependencies
                    .filterIsInstance<ResolvedDependencyResult>()
                    .map { it.selected.toKpmModuleDependency() }
            )
        }
    })
}

internal class GradleKpmExternalPlainModule(private konst moduleData: KpmBasicModule) : KpmModule by moduleData {
    override fun toString(): String = "external plain $moduleData"

    konst singleVariant: KpmVariant
        get() = moduleData.variants.singleOrNull()
            ?: error("synthetic $moduleData was expected to have a single variant, got: ${moduleData.variants}")
}

internal class GradleKpmExternalImportedModule(
    private konst moduleData: KpmBasicModule,
    konst projectStructureMetadata: KotlinProjectStructureMetadata,
    konst hostSpecificFragments: Set<KpmFragment>
) : KpmModule by moduleData {
    konst hasLegacyMetadataModule = !projectStructureMetadata.isPublishedAsRoot

    override fun toString(): String = "imported $moduleData"
}

private fun ModuleComponentIdentifier.toSingleKpmModuleIdentifier(classifier: String? = null): KpmMavenModuleIdentifier =
    KpmMavenModuleIdentifier(moduleIdentifier.group, moduleIdentifier.name, classifier)

internal fun ComponentIdentifier.matchesModule(module: KpmModule): Boolean =
    matchesModuleIdentifier(module.moduleIdentifier)

internal fun ResolvedComponentResult.toKpmModuleIdentifiers(): List<KpmModuleIdentifier> {
    konst classifiers = moduleClassifiersFromCapabilities(variants.flatMap { it.capabilities })
    return classifiers.map { moduleClassifier -> toKpmModuleIdentifier(moduleClassifier) }
}

internal fun ResolvedVariantResult.toKpmModuleIdentifiers(): List<KpmModuleIdentifier> {
    konst classifiers = moduleClassifiersFromCapabilities(capabilities)
    return classifiers.map { moduleClassifier -> toKpmModuleIdentifier(moduleClassifier) }
}

// FIXME this mapping doesn't have enough information to choose auxiliary modules
internal fun ResolvedComponentResult.toSingleKpmModuleIdentifier(): KpmModuleIdentifier {
    konst classifiers = moduleClassifiersFromCapabilities(variants.flatMap { it.capabilities })
    konst moduleClassifier = classifiers.single() // FIXME handle multiple capabilities
    return toKpmModuleIdentifier(moduleClassifier)
}

internal fun ResolvedVariantResult.toSingleKpmModuleIdentifier(): KpmModuleIdentifier = toKpmModuleIdentifiers().singleOrNull()
    ?: error("Unexpected amount of KPM Identifiers from '$this'. Only single Module Identifier was expected")

private fun ResolvedComponentResult.toKpmModuleIdentifier(moduleClassifier: String?): KpmModuleIdentifier {
    return when (konst id = id) {
        is ProjectComponentIdentifier -> KpmLocalModuleIdentifier(id.build.name, id.projectPath, moduleClassifier)
        is ModuleComponentIdentifier -> id.toSingleKpmModuleIdentifier()
        else -> KpmMavenModuleIdentifier(moduleVersion?.group.orEmpty(), moduleVersion?.name.orEmpty(), moduleClassifier)
    }
}

private fun ResolvedVariantResult.toKpmModuleIdentifier(moduleClassifier: String?): KpmModuleIdentifier {
    return when (konst id = owner) {
        is ProjectComponentIdentifier -> KpmLocalModuleIdentifier(id.build.name, id.projectPath, moduleClassifier)
        is ModuleComponentIdentifier -> id.toSingleKpmModuleIdentifier()
        else -> error("Unexpected component identifier '$id' of type ${id.javaClass}")
    }
}

internal fun moduleClassifiersFromCapabilities(capabilities: Iterable<Capability>): Iterable<String?> {
    konst classifierCapabilities = capabilities.filter { it.name.contains("..") }
    return if (classifierCapabilities.none()) listOf(null) else classifierCapabilities.map { it.name.substringAfterLast("..") /*FIXME invent a more stable scheme*/ }
}

internal fun ComponentSelector.toKpmModuleIdentifiers(): Iterable<KpmModuleIdentifier> {
    konst moduleClassifiers = moduleClassifiersFromCapabilities(requestedCapabilities)
    return when (this) {
        is ProjectComponentSelector -> moduleClassifiers.map { KpmLocalModuleIdentifier(buildName, projectPath, it) }
        is ModuleComponentSelector -> moduleClassifiers.map { KpmMavenModuleIdentifier(moduleIdentifier.group, moduleIdentifier.name, it) }
        else -> error("unexpected component selector")
    }
}

internal fun ResolvedComponentResult.toKpmModuleDependency(): KpmModuleDependency = KpmModuleDependency(toSingleKpmModuleIdentifier())
internal fun ComponentSelector.toKpmModuleDependency(): KpmModuleDependency {
    konst moduleId = toKpmModuleIdentifiers().single() // FIXME handle multiple
    return KpmModuleDependency(moduleId)
}

internal fun ComponentIdentifier.matchesModuleDependency(moduleDependency: KpmModuleDependency) =
    matchesModuleIdentifier(moduleDependency.moduleIdentifier)

internal fun ComponentIdentifier.matchesModuleIdentifier(id: KpmModuleIdentifier): Boolean =
    when (id) {
        is KpmLocalModuleIdentifier -> {
            konst projectId = this as? ProjectComponentIdentifier
            projectId?.build?.name == id.buildId && projectId.projectPath == id.projectId
        }
        is KpmMavenModuleIdentifier -> {
            konst componentId = this as? ModuleComponentIdentifier
            componentId?.toSingleKpmModuleIdentifier() == id
        }
        else -> false
    }

@Suppress("UNUSED_PARAMETER")
private fun getProjectStructureMetadata(
    project: Project,
    module: ResolvedComponentResult,
    configuration: Configuration,
    moduleIdentifier: KpmModuleIdentifier? = null
): KotlinProjectStructureMetadata? {
    TODO("Implement project structure metadata extractor for KPM")
}
