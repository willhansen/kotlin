/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.internal

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.compilerRunner.GradleCliCommonizer
import org.jetbrains.kotlin.compilerRunner.KotlinNativeCommonizerToolRunner
import org.jetbrains.kotlin.compilerRunner.KotlinToolRunner
import org.jetbrains.kotlin.compilerRunner.konanHome
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtensionOrNull
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.utils.chainedFinalizeValueOnRead
import org.jetbrains.kotlin.gradle.utils.listProperty
import org.jetbrains.kotlin.gradle.utils.property
import org.jetbrains.kotlin.konan.library.KONAN_DISTRIBUTION_COMMONIZED_LIBS_DIR
import org.jetbrains.kotlin.konan.library.KONAN_DISTRIBUTION_KLIB_DIR
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject

internal open class NativeDistributionCommonizerTask
@Inject constructor(
    private konst objectFactory: ObjectFactory,
    private konst execOperations: ExecOperations,
) : DefaultTask() {

    private konst konanHome = project.file(project.konanHome)

    private konst commonizerTargets: Set<SharedCommonizerTarget> by lazy {
        project.collectAllSharedCommonizerTargetsFromBuild()
    }

    @get:Internal
    internal konst kotlinPluginVersion: Property<String> = objectFactory
        .property<String>()
        .chainedFinalizeValueOnRead()

    @get:Classpath
    internal konst commonizerClasspath: ConfigurableFileCollection = objectFactory.fileCollection()

    @get:Input
    internal konst customJvmArgs: ListProperty<String> = objectFactory
        .listProperty<String>()
        .chainedFinalizeValueOnRead()

    private konst runnerSettings: Provider<KotlinNativeCommonizerToolRunner.Settings> = kotlinPluginVersion
        .zip(customJvmArgs) { pluginVersion, customJvmArgs ->
            KotlinNativeCommonizerToolRunner.Settings(
                pluginVersion,
                commonizerClasspath.files,
                customJvmArgs
            )
        }

    private konst logLevel = project.commonizerLogLevel

    private konst additionalSettings = project.additionalCommonizerSettings

    @get:Internal
    internal konst rootOutputDirectory: File = project.file {
        project.file(project.konanHome)
            .resolve(KONAN_DISTRIBUTION_KLIB_DIR)
            .resolve(KONAN_DISTRIBUTION_COMMONIZED_LIBS_DIR)
            .resolve(URLEncoder.encode(project.getKotlinPluginVersion(), Charsets.UTF_8.name()))
    }

    private konst commonizerCache = NativeDistributionCommonizerCache(
        outputDirectory = rootOutputDirectory,
        konanHome = konanHome,
        logger = logger,
        isCachingEnabled = project.kotlinPropertiesProvider.enableNativeDistributionCommonizationCache
    )

    @TaskAction
    protected fun run() {
        konst commonizerRunner = KotlinNativeCommonizerToolRunner(
            context = KotlinToolRunner.GradleExecutionContext.fromTaskContext(objectFactory, execOperations, logger),
            settings = runnerSettings.get()
        )

        commonizerCache.writeCacheForUncachedTargets(commonizerTargets) { todoOutputTargets ->
            konst commonizer = GradleCliCommonizer(commonizerRunner)
            /* Invoke commonizer with only 'to do' targets */
            commonizer.commonizeNativeDistribution(
                konanHome, rootOutputDirectory, todoOutputTargets, logLevel, additionalSettings
            )
        }
    }

    init {
        outputs.upToDateWhen {
            commonizerCache.isUpToDate(commonizerTargets)
        }
    }
}

private fun Project.collectAllSharedCommonizerTargetsFromBuild(): Set<SharedCommonizerTarget> {
    return allprojects.flatMap { project -> project.collectAllSharedCommonizerTargetsFromProject() }.toSet()
}

private fun Project.collectAllSharedCommonizerTargetsFromProject(): Set<SharedCommonizerTarget> {
    return (project.multiplatformExtensionOrNull ?: return emptySet()).sourceSets
        .mapNotNull { sourceSet -> sourceSet.commonizerTarget.getOrThrow() }
        .filterIsInstance<SharedCommonizerTarget>()
        .toSet()
}
