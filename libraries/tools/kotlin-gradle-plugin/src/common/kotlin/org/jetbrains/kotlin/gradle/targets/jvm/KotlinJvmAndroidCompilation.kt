/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.getJavaTaskProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import javax.inject.Inject

open class KotlinJvmAndroidCompilation @Inject internal constructor(
    compilation: KotlinCompilationImpl,
    konst androidVariant: BaseVariant
) : AbstractKotlinCompilationToRunnableFiles<KotlinJvmOptions>(compilation) {

    override konst target: KotlinAndroidTarget = compilation.target as KotlinAndroidTarget

    override konst compilerOptions: HasCompilerOptions<KotlinJvmCompilerOptions> =
        compilation.compilerOptions.castCompilerOptionsType()

    internal konst testedVariantArtifacts: Property<FileCollection> =
        compilation.project.objects.property(FileCollection::class.java)

    @Suppress("DEPRECATION")
    @Deprecated("Accessing task instance directly is deprecated", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTask: org.jetbrains.kotlin.gradle.tasks.KotlinCompile
        get() = compilation.compileKotlinTask as org.jetbrains.kotlin.gradle.tasks.KotlinCompile

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    @Deprecated("Replaced with compileTaskProvider", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTaskProvider: TaskProvider<out org.jetbrains.kotlin.gradle.tasks.KotlinCompile>
        get() = compilation.compileKotlinTaskProvider as TaskProvider<out org.jetbrains.kotlin.gradle.tasks.KotlinCompile>

    @Suppress("UNCHECKED_CAST")
    override konst compileTaskProvider: TaskProvider<out KotlinCompilationTask<KotlinJvmCompilerOptions>>
        get() = compilation.compileTaskProvider as TaskProvider<KotlinCompilationTask<KotlinJvmCompilerOptions>>

    konst compileJavaTaskProvider: TaskProvider<out JavaCompile>
        get() = androidVariant.getJavaTaskProvider()

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    @Suppress("DEPRECATION")
    override konst relatedConfigurationNames: List<String>
        get() = compilation.relatedConfigurationNames + listOf(
            "${androidVariant.name}ApiElements",
            "${androidVariant.name}RuntimeElements",
            androidVariant.compileConfiguration.name,
            androidVariant.runtimeConfiguration.name
        )
}
