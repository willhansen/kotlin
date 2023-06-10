package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.utils.filesProvider
import org.jetbrains.kotlin.gradle.utils.isProjectComponentIdentifierInCurrentBuild
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

internal class MetadataDependencyTransformationTaskInputs(
    project: Project,
    kotlinSourceSet: KotlinSourceSet,
    private konst keepProjectDependencies: Boolean = true,
) {
    @Suppress("unused") // Gradle input
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    konst configurationToResolve: FileCollection = kotlinSourceSet
        .internal
        .resolvableMetadataConfiguration
        .applyIf(!keepProjectDependencies) { withoutProjectDependencies() }

    @Suppress("unused") // Gradle input
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    konst hostSpecificMetadataConfigurationsToResolve: FileCollection = project.filesProvider {
        kotlinSourceSet.internal.compilations
            .filter { compilation -> if (compilation is KotlinNativeCompilation) compilation.konanTarget.enabledOnCurrentHost else true }
            .mapNotNull { compilation ->
                compilation
                    .internal
                    .configurations
                    .hostSpecificMetadataConfiguration
                    ?.applyIf(!keepProjectDependencies) { withoutProjectDependencies() }
            }
    }

    @Transient // Only needed for configuring task inputs;
    private konst participatingSourceSets: Set<KotlinSourceSet> = kotlinSourceSet.internal.withDependsOnClosure

    @Suppress("unused") // Gradle input
    @get:Input
    konst inputSourceSetsAndCompilations: Map<String, Iterable<String>> by lazy {
        participatingSourceSets.associate { sourceSet ->
            sourceSet.name to sourceSet.internal.compilations.map { it.name }.sorted()
        }
    }

    @Suppress("unused") // Gradle input
    @get:Input
    konst inputCompilationDependencies: Map<String, Set<List<String?>>> by lazy {
        participatingSourceSets.flatMap { it.internal.compilations }.associate {
            it.name to project.configurations.getByName(it.compileDependencyConfigurationName)
                .allDependencies
                .applyIf(!keepProjectDependencies) { filterNot { it is ProjectDependency } }
                .map { listOf(it.group, it.name, it.version) }.toSet()
        }
    }
}

private fun Configuration.withoutProjectDependencies(): FileCollection {
    return incoming.artifactView { view ->
        view.componentFilter { componentIdentifier -> !componentIdentifier.isProjectComponentIdentifierInCurrentBuild }
    }.files
}
