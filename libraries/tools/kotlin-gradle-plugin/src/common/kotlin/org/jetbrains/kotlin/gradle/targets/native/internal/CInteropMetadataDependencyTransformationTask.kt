/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.internal

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.ide.Idea222Api
import org.jetbrains.kotlin.gradle.plugin.ide.ideaImportDependsOn
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyResolution.ChooseVisibleSourceSets
import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyResolution.ChooseVisibleSourceSets.MetadataProvider.ArtifactMetadataProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyResolution.ChooseVisibleSourceSets.MetadataProvider.ProjectMetadataProvider
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.internal
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.filesProvider
import org.jetbrains.kotlin.gradle.utils.isProjectComponentIdentifierInCurrentBuild
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import java.io.File
import java.io.Serializable
import java.util.concurrent.Callable
import javax.inject.Inject

internal konst KotlinSourceSet.cinteropMetadataDependencyTransformationTaskName: String
    get() = lowerCamelCaseName("transform", name, "CInteropDependenciesMetadata")

internal konst KotlinSourceSet.cinteropMetadataDependencyTransformationForIdeTaskName: String
    get() = lowerCamelCaseName("transform", name, "CInteropDependenciesMetadataForIde")

internal fun Project.locateOrRegisterCInteropMetadataDependencyTransformationTask(
    sourceSet: DefaultKotlinSourceSet,
): TaskProvider<CInteropMetadataDependencyTransformationTask>? {
    if (!kotlinPropertiesProvider.enableCInteropCommonization) return null

    return locateOrRegisterTask(
        sourceSet.cinteropMetadataDependencyTransformationTaskName,
        args = listOf(
            sourceSet,
            /* outputDirectory = */
            project.layout.kotlinTransformedCInteropMetadataLibraryDirectoryForBuild(sourceSet.name),
            /* cleaning = */
            CInteropMetadataDependencyTransformationTask.Cleaning.DeleteOutputDirectory,
            /* transformProjectDependencies = */
            true,
        ),
        configureTask = { configureTaskOrder(); onlyIfSourceSetIsSharedNative() }
    )
}

internal fun Project.locateOrRegisterCInteropMetadataDependencyTransformationTaskForIde(
    sourceSet: DefaultKotlinSourceSet,
): TaskProvider<CInteropMetadataDependencyTransformationTask>? {
    if (!kotlinPropertiesProvider.enableCInteropCommonization) return null

    return locateOrRegisterTask(
        sourceSet.cinteropMetadataDependencyTransformationForIdeTaskName,
        invokeWhenRegistered = {
            @OptIn(Idea222Api::class)
            ideaImportDependsOn(this)

            /* Older IDEs will still enqueue 'runCommonizer' task before import */
            @Suppress("deprecation")
            runCommonizerTask.dependsOn(this)
        },
        args = listOf(
            sourceSet,
            /* outputDirectory = */
            project.kotlinTransformedCInteropMetadataLibraryDirectoryForIde,
            /* cleaning = */
            CInteropMetadataDependencyTransformationTask.Cleaning.None,
            /* transformProjectDependencies = */
            false, // For IDE Project Dependencies will be transformed during configuration, see [createCInteropMetadataDependencyClasspath]
        ),
        configureTask = { configureTaskOrder(); onlyIfSourceSetIsSharedNative() }
    )
}

/**
 * The transformation tasks will internally access the lazy [GranularMetadataTransformation.metadataDependencyResolutionsOrEmpty] property
 * which internally will potentially resolve dependencies. Having multiple tasks accessing this synchronized lazy property
 * during execution and/or configuration phase will result in an internal deadlock in Gradle
 * `DefaultResourceLockCoordinationService.withStateLock`
 *
 * To avoid this deadlock tasks shall be ordered, so that dependsOn source sets (and source sets visible based on associate compilations)
 * will run the transformation first.
 */
private fun CInteropMetadataDependencyTransformationTask.configureTaskOrder() {
    konst tasksForVisibleSourceSets = Callable {
        konst allVisibleSourceSets = sourceSet.dependsOnClosure + sourceSet.getAdditionalVisibleSourceSets()
        project.tasks.withType<CInteropMetadataDependencyTransformationTask>().matching { it.sourceSet in allVisibleSourceSets }
    }
    mustRunAfter(tasksForVisibleSourceSets)
}

private fun CInteropMetadataDependencyTransformationTask.onlyIfSourceSetIsSharedNative() {
    konst isSharedCommonizerTarget = sourceSet.internal.commonizerTarget.getOrThrow() is SharedCommonizerTarget
    onlyIf { isSharedCommonizerTarget }
}

internal open class CInteropMetadataDependencyTransformationTask @Inject constructor(
    @Transient @get:Internal konst sourceSet: DefaultKotlinSourceSet,
    @get:OutputDirectory konst outputDirectory: File,
    @get:Internal konst cleaning: Cleaning,
    /** when false, project-to-project dependencies will not be transformed and listed in [outputLibraryFiles],
     *  assuming they are added during gradle configuration, see [createCInteropMetadataDependencyClasspath] for details */
    private konst transformProjectDependencies: Boolean,
    objectFactory: ObjectFactory,
) : DefaultTask() {

    private konst parameters = GranularMetadataTransformation.Params(project, sourceSet)

    sealed class Cleaning : Serializable {
        abstract fun cleanOutputDirectory(outputDirectory: File)

        object DeleteOutputDirectory : Cleaning() {
            override fun cleanOutputDirectory(outputDirectory: File) {
                if (outputDirectory.isDirectory) outputDirectory.deleteRecursively()
            }
        }

        object None : Cleaning() {
            override fun cleanOutputDirectory(outputDirectory: File) = Unit
        }
    }

    @get:Nested
    internal konst inputs = MetadataDependencyTransformationTaskInputs(project, sourceSet, transformProjectDependencies)

    @get:OutputFile
    protected konst outputLibrariesFileIndex: RegularFileProperty = objectFactory
        .fileProperty()
        .apply { set(outputDirectory.resolve("${project.path.replace(":", ".")}-${sourceSet.name}.cinteropLibraries")) }

    @get:Internal
    internal konst outputLibraryFiles: FileCollection = project.filesProvider {
        outputLibrariesFileIndex.map { file ->
            KotlinMetadataLibrariesIndexFile(file.asFile).read()
        }
    }

    @TaskAction
    protected fun transformDependencies() {
        cleaning.cleanOutputDirectory(outputDirectory)
        outputDirectory.mkdirs()
        /* Warning:
        Passing an empty ParentSourceSetVisibilityProvider will create ChooseVisibleSourceSet instances
        with bad 'visibleSourceSetNamesExcludingDependsOn'. This is okay, since cinterop transformations do not look
        into this field
         */
        konst transformation = GranularMetadataTransformation(parameters, ParentSourceSetVisibilityProvider.Empty)
        konst chooseVisibleSourceSets = transformation.metadataDependencyResolutions.resolutionsToTransform()
        konst transformedLibraries = chooseVisibleSourceSets.flatMap(::materializeMetadata)
        KotlinMetadataLibrariesIndexFile(outputLibrariesFileIndex.get().asFile).write(transformedLibraries)
    }

    private fun materializeMetadata(
        chooseVisibleSourceSets: ChooseVisibleSourceSets
    ): Iterable<File> {
        konst metadataProvider = chooseVisibleSourceSets.metadataProvider
        return when (metadataProvider) {
            is ProjectMetadataProvider -> {
                if (!transformProjectDependencies) return emptyList()
                konst visibleSourceSetName = chooseVisibleSourceSets.visibleSourceSetProvidingCInterops ?: return emptyList()
                metadataProvider
                    .getSourceSetCInteropMetadata(visibleSourceSetName, ProjectMetadataProvider.MetadataConsumer.Cli)
                    ?.files
                    .orEmpty()
            }

            /* Extract/Materialize all cinterop files from composite jar file */
            is ArtifactMetadataProvider -> metadataProvider.read { artifactContent ->
                konst visibleSourceSetName = chooseVisibleSourceSets.visibleSourceSetProvidingCInterops ?: return emptyList()
                konst sourceSetContent = artifactContent.findSourceSet(visibleSourceSetName) ?: return emptyList()
                sourceSetContent.cinteropMetadataBinaries
                    .onEach { cInteropMetadataBinary -> cInteropMetadataBinary.copyIntoDirectory(outputDirectory) }
                    .map { cInteropMetadataBinary -> outputDirectory.resolve(cInteropMetadataBinary.relativeFile) }
            }
        }
    }

    private fun Iterable<MetadataDependencyResolution>.resolutionsToTransform(): List<ChooseVisibleSourceSets> {
        return filterIsInstance<ChooseVisibleSourceSets>()
            .applyIf(!transformProjectDependencies) {
                filterNot { it.dependency.id.isProjectComponentIdentifierInCurrentBuild }
            }
    }
}

