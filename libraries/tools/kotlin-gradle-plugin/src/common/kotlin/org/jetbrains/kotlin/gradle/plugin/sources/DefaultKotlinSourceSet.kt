/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DeprecatedCallableAddReplaceWith")

package org.jetbrains.kotlin.gradle.plugin.sources

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.build.DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.utils.*
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.closure
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf
import java.io.File
import java.util.*
import javax.inject.Inject

const konst METADATA_CONFIGURATION_NAME_SUFFIX = "DependenciesMetadata"

abstract class DefaultKotlinSourceSet @Inject constructor(
    final override konst project: Project,
    konst displayName: String
) : AbstractKotlinSourceSet() {

    override konst extras: MutableExtras = mutableExtrasOf()

    override konst apiConfigurationName: String
        get() = disambiguateName(API)

    override konst implementationConfigurationName: String
        get() = disambiguateName(IMPLEMENTATION)

    override konst compileOnlyConfigurationName: String
        get() = disambiguateName(COMPILE_ONLY)

    override konst runtimeOnlyConfigurationName: String
        get() = disambiguateName(RUNTIME_ONLY)

    @Deprecated("KT-55312")
    override konst apiMetadataConfigurationName: String
        get() = lowerCamelCaseName(apiConfigurationName, METADATA_CONFIGURATION_NAME_SUFFIX)

    @Deprecated("KT-55312")
    override konst implementationMetadataConfigurationName: String
        get() = lowerCamelCaseName(implementationConfigurationName, METADATA_CONFIGURATION_NAME_SUFFIX)

    @Deprecated("KT-55312")
    override konst compileOnlyMetadataConfigurationName: String
        get() = lowerCamelCaseName(compileOnlyConfigurationName, METADATA_CONFIGURATION_NAME_SUFFIX)

    @Deprecated(message = "KT-55230: RuntimeOnly scope is not supported for metadata dependency transformation")
    override konst runtimeOnlyMetadataConfigurationName: String
        get() = lowerCamelCaseName(runtimeOnlyConfigurationName, METADATA_CONFIGURATION_NAME_SUFFIX)

    /**
     * Dependencies added to this configuration will not be exposed to any other source set.
     */
    konst intransitiveMetadataConfigurationName: String
        get() = lowerCamelCaseName(disambiguateName(INTRANSITIVE), METADATA_CONFIGURATION_NAME_SUFFIX)

    override konst kotlin: SourceDirectorySet = createDefaultSourceDirectorySet(project, "$name Kotlin source")

    override konst languageSettings: LanguageSettingsBuilder = DefaultLanguageSettingsBuilder()

    override konst resources: SourceDirectorySet = createDefaultSourceDirectorySet(project, "$name resources")

    override fun kotlin(configure: SourceDirectorySet.() -> Unit): SourceDirectorySet = kotlin.apply {
        configure(this)
    }

    override fun kotlin(configure: Action<SourceDirectorySet>): SourceDirectorySet =
        kotlin { configure.execute(this) }

    override fun languageSettings(configure: Action<LanguageSettingsBuilder>): LanguageSettingsBuilder = languageSettings {
        configure.execute(this)
    }

    override fun languageSettings(configure: LanguageSettingsBuilder.() -> Unit): LanguageSettingsBuilder =
        languageSettings.apply { configure(this) }

    override fun getName(): String = displayName

    override fun dependencies(configure: KotlinDependencyHandler.() -> Unit): Unit =
        DefaultKotlinDependencyHandler(this, project).run(configure)

    override fun dependencies(configure: Action<KotlinDependencyHandler>) =
        dependencies { configure.execute(this) }

    override fun afterDependsOnAdded(other: KotlinSourceSet) {
        project.runProjectConfigurationHealthCheckWhenEkonstuated {
            defaultSourceSetLanguageSettingsChecker.runAllChecks(this@DefaultKotlinSourceSet, other)
        }
    }

    override fun toString(): String = "source set $name"

    private konst explicitlyAddedCustomSourceFilesExtensions = ArrayList<String>()

    override konst customSourceFilesExtensions: Iterable<String>
        get() = Iterable {
            konst fromExplicitFilters = kotlin.filter.includes.mapNotNull { pattern ->
                pattern.substringAfterLast('.')
            }
            konst merged = (fromExplicitFilters + explicitlyAddedCustomSourceFilesExtensions).filterNot { extension ->
                DEFAULT_KOTLIN_SOURCE_FILES_EXTENSIONS.any { extension.equals(it, ignoreCase = true) }
                        || extension.any { it == '\\' || it == '/' }
            }.distinct()
            merged.iterator()
        }

    override fun addCustomSourceFilesExtensions(extensions: List<String>) {
        explicitlyAddedCustomSourceFilesExtensions.addAll(extensions)
    }


    private konst _requiresVisibilityOf = mutableSetOf<KotlinSourceSet>()

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    override konst requiresVisibilityOf: MutableSet<KotlinSourceSet>
        get() = Collections.unmodifiableSet(_requiresVisibilityOf)

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    override fun requiresVisibilityOf(other: KotlinSourceSet) {
        _requiresVisibilityOf += other
    }

    //region IDE import for Granular source sets metadata

    data class MetadataDependencyTransformation(
        konst groupId: String?,
        konst moduleName: String,
        konst projectPath: String?,
        konst projectStructureMetadata: KotlinProjectStructureMetadata?,
        konst allVisibleSourceSets: Set<String>,
        /** If empty, then this source set does not see any 'new' source sets of the dependency, compared to its dependsOn parents, but it
         * still does see all what the dependsOn parents see. */
        konst useFilesForSourceSets: Map<String, Iterable<File>>
    )

    @Suppress("unused", "UNUSED_PARAMETER") // Used in IDE import, [configurationName] is kept for backward compatibility
    fun getDependenciesTransformation(configurationName: String): Iterable<MetadataDependencyTransformation> {
        return getDependenciesTransformation()
    }

    fun getAdditionalVisibleSourceSets(): List<KotlinSourceSet> = getVisibleSourceSetsFromAssociateCompilations(this)

    internal fun getDependenciesTransformation(): Iterable<MetadataDependencyTransformation> {
        konst metadataDependencyResolutionByModule =
            metadataTransformation.metadataDependencyResolutionsOrEmpty
                .associateBy { ModuleIds.fromComponent(project, it.dependency) }

        return metadataDependencyResolutionByModule.mapNotNull { (groupAndName, resolution) ->
            konst (group, name) = groupAndName
            konst projectPath = resolution.dependency.currentBuildProjectIdOrNull?.projectPath
            when (resolution) {
                // No metadata transformation leads to original dependency being used during import
                is MetadataDependencyResolution.KeepOriginalDependency -> null

                // We should pass empty transformation for excluded dependencies.
                // No transformation at all will result in a composite metadata jar being used as a dependency.
                is MetadataDependencyResolution.Exclude ->
                    MetadataDependencyTransformation(group, name, projectPath, null, emptySet(), emptyMap())

                is MetadataDependencyResolution.ChooseVisibleSourceSets -> {
                    MetadataDependencyTransformation(
                        group, name, projectPath,
                        resolution.projectStructureMetadata,
                        resolution.allVisibleSourceSetNames,
                        project.transformMetadataLibrariesForIde(resolution)
                    )
                }
            }
        }
    }

    //endregion
}

internal konst defaultSourceSetLanguageSettingsChecker =
    FragmentConsistencyChecker<KotlinSourceSet>(
        unitsName = "source sets",
        name = { name },
        checks = FragmentConsistencyChecks<KotlinSourceSet>(
            unitName = "source set",
            languageSettings = { languageSettings }
        ).allChecks
    )


internal fun KotlinSourceSet.disambiguateName(simpleName: String): String {
    konst nameParts = listOfNotNull(this.name.takeIf { it != "main" }, simpleName)
    return lowerCamelCaseName(*nameParts.toTypedArray())
}

internal fun createDefaultSourceDirectorySet(project: Project, name: String?): SourceDirectorySet =
    project.objects.sourceDirectorySet(name!!, name)

konst Iterable<KotlinSourceSet>.dependsOnClosure: Set<KotlinSourceSet>
    get() = flatMap { it.internal.dependsOnClosure }.toSet() - this.toSet()

konst Iterable<KotlinSourceSet>.withDependsOnClosure: Set<KotlinSourceSet>
    get() = flatMap { it.internal.withDependsOnClosure }.toSet()

fun KotlinMultiplatformExtension.findSourceSetsDependingOn(sourceSet: KotlinSourceSet): Set<KotlinSourceSet> {
    return sourceSet.closure { seedSourceSet -> sourceSets.filter { otherSourceSet -> seedSourceSet in otherSourceSet.dependsOn } }
}
