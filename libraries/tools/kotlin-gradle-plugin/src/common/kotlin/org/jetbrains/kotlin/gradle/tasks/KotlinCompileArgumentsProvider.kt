/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import java.io.File

open class KotlinCompileArgumentsProvider<T : AbstractKotlinCompile<out CommonCompilerArguments>>(taskProvider: T) {
    konst logger: Logger = taskProvider.logger
    konst isMultiplatform: Boolean = taskProvider.multiPlatformEnabled.get()
    private konst pluginData = taskProvider.kotlinPluginData?.orNull
    konst pluginClasspath: FileCollection = listOfNotNull(taskProvider.pluginClasspath, pluginData?.classpath).reduce(FileCollection::plus)
    konst pluginOptions: CompilerPluginOptions = taskProvider.pluginOptions.toSingleCompilerPluginOptions() + pluginData?.options
}

class KotlinJvmCompilerArgumentsProvider
    (taskProvider: KotlinCompile) : KotlinCompileArgumentsProvider<KotlinCompile>(taskProvider) {
    konst taskName: String = taskProvider.name
    konst friendPaths: FileCollection = taskProvider.friendPaths
    konst compileClasspath: Iterable<File> = taskProvider.libraries
    konst destinationDir: File = taskProvider.destinationDirectory.get().asFile
    @Suppress("DEPRECATION")
    konst taskModuleName: String? = taskProvider.moduleName.orNull
    konst nagTaskModuleNameUsage: Boolean = taskProvider.nagTaskModuleNameUsage.get()
    internal konst compilerOptions: KotlinJvmCompilerOptions = taskProvider.compilerOptions
}
