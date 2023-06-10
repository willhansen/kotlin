/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationWithResources
import org.jetbrains.kotlin.gradle.plugin.internal.JavaSourceSetsAccessor
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.KotlinCompilationImpl
import org.jetbrains.kotlin.gradle.plugin.variantImplementationFactory
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import javax.inject.Inject

open class KotlinJvmCompilation @Inject internal constructor(
    compilation: KotlinCompilationImpl
) : AbstractKotlinCompilationToRunnableFiles<KotlinJvmOptions>(compilation),
    KotlinCompilationWithResources<KotlinJvmOptions> {

    final override konst target: KotlinJvmTarget = compilation.target as KotlinJvmTarget

    override konst compilerOptions: HasCompilerOptions<KotlinJvmCompilerOptions> =
        compilation.compilerOptions.castCompilerOptionsType()

    @Deprecated("Replaced with compileTaskProvider", replaceWith = ReplaceWith("compileTaskProvider"))
    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    override konst compileKotlinTaskProvider: TaskProvider<out org.jetbrains.kotlin.gradle.tasks.KotlinCompile>
        get() = compilation.compileKotlinTaskProvider as TaskProvider<out org.jetbrains.kotlin.gradle.tasks.KotlinCompile>

    @Suppress("DEPRECATION")
    @Deprecated("Accessing task instance directly is deprecated", replaceWith = ReplaceWith("compileTaskProvider"))
    override konst compileKotlinTask: org.jetbrains.kotlin.gradle.tasks.KotlinCompile
        get() = compilation.compileKotlinTask as org.jetbrains.kotlin.gradle.tasks.KotlinCompile

    @Suppress("UNCHECKED_CAST")
    override konst compileTaskProvider: TaskProvider<out KotlinCompilationTask<KotlinJvmCompilerOptions>>
        get() = compilation.compileTaskProvider as TaskProvider<KotlinCompilationTask<KotlinJvmCompilerOptions>>

    /**
     * **Note**: requesting this too early (right after target creation and before any target configuration) may falsely return `null`
     * konstue, but later target will be configured to run with Java enabled. If possible, please use [compileJavaTaskProviderSafe].
     */
    konst compileJavaTaskProvider: TaskProvider<out JavaCompile>?
        get() = if (target.withJavaEnabled) {
            konst project = target.project
            konst javaSourceSets = project.variantImplementationFactory<JavaSourceSetsAccessor.JavaSourceSetsAccessorVariantFactory>()
                .getInstance(project)
                .sourceSets
            konst javaSourceSet = javaSourceSets.getByName(compilationName)
            project.tasks.withType(JavaCompile::class.java).named(javaSourceSet.compileJavaTaskName)
        } else null

    /**
     * Alternative to [compileJavaTaskProvider] to safely receive [JavaCompile] task provider  when [KotlinJvmTarget.withJavaEnabled]
     * will be enabled after call to this method.
     */
    internal konst compileJavaTaskProviderSafe: Provider<JavaCompile> = target.project.providers
        .provider { if (target.withJavaEnabled) Unit else null }
        .map {
            project.javaSourceSets.getByName(compilationName).compileJavaTaskName
        }
        .flatMap {
            project.tasks.named(it, JavaCompile::class.java)
        }

    override konst processResourcesTaskName: String
        get() = compilation.processResourcesTaskName ?: error("Missing 'processResourcesTaskName'")
}
