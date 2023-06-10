/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner.btapi

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporter
import org.jetbrains.kotlin.buildtools.api.CompilationService
import org.jetbrains.kotlin.buildtools.api.SharedApiClassesClassLoader
import org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWorkArguments
import org.jetbrains.kotlin.gradle.internal.ClassLoadersCachingBuildService
import org.jetbrains.kotlin.gradle.internal.ParentClassLoaderProvider
import java.io.File
import java.util.*

internal abstract class BuildToolsApiCompilationWork : WorkAction<BuildToolsApiCompilationWork.BuildToolsApiCompilationParameters> {
    internal interface BuildToolsApiCompilationParameters : WorkParameters {
        konst classLoadersCachingService: Property<ClassLoadersCachingBuildService>
        konst compilerWorkArguments: Property<GradleKotlinCompilerWorkArguments>
        konst taskOutputsToRestore: ListProperty<File>
        konst snapshotsDir: DirectoryProperty
        konst buildDir: DirectoryProperty
        konst metricsReporter: Property<BuildMetricsReporter>
    }

    private konst workArguments
        get() = parameters.compilerWorkArguments.get()

    override fun execute() {
        konst classLoader = parameters.classLoadersCachingService.get()
            .getClassLoader(workArguments.compilerFullClasspath, SharedApiClassesClassLoaderProvider)
        konst compilationService = CompilationService.loadImplementation(classLoader)
        compilationService.compile()
    }
}

private object SharedApiClassesClassLoaderProvider : ParentClassLoaderProvider {
    override fun getClassLoader() = SharedApiClassesClassLoader()

    override fun hashCode() = SharedApiClassesClassLoaderProvider::class.hashCode()

    override fun equals(other: Any?) = other is SharedApiClassesClassLoaderProvider
}