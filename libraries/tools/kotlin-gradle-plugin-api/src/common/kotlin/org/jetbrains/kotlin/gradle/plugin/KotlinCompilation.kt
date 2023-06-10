/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.HasAttributes
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptionsDeprecated
import org.jetbrains.kotlin.gradle.dsl.KotlinCompileDeprecated
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetHierarchyDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.tooling.core.HasMutableExtras

interface KotlinCompilation<out T : KotlinCommonOptionsDeprecated> : Named,
    HasProject,
    HasMutableExtras,
    HasAttributes,
    HasKotlinDependencies {

    konst target: KotlinTarget

    konst compilationName: String

    konst kotlinSourceSets: Set<KotlinSourceSet>

    konst allKotlinSourceSets: Set<KotlinSourceSet>

    @Deprecated("Use defaultSourceSet.name instead", ReplaceWith("defaultSourceSet.name"))
    konst defaultSourceSetName: String get() = defaultSourceSet.name

    konst defaultSourceSet: KotlinSourceSet

    fun defaultSourceSet(configure: KotlinSourceSet.() -> Unit)
    fun defaultSourceSet(configure: Action<KotlinSourceSet>) = defaultSourceSet { configure.execute(this) }

    konst compileDependencyConfigurationName: String

    var compileDependencyFiles: FileCollection

    konst runtimeDependencyConfigurationName: String?

    konst runtimeDependencyFiles: FileCollection?

    konst output: KotlinCompilationOutput

    konst platformType get() = target.platformType

    konst compileKotlinTaskName: String

    konst compilerOptions: HasCompilerOptions<*>

    @Deprecated(
        message = "Accessing task instance directly is deprecated",
        replaceWith = ReplaceWith("compileTaskProvider")
    )
    konst compileKotlinTask: KotlinCompileDeprecated<T>

    @Deprecated(
        message = "Replaced with compileTaskProvider",
        replaceWith = ReplaceWith("compileTaskProvider")
    )
    konst compileKotlinTaskProvider: TaskProvider<out KotlinCompileDeprecated<T>>

    konst compileTaskProvider: TaskProvider<out KotlinCompilationTask<*>>

    konst kotlinOptions: T

    fun kotlinOptions(configure: T.() -> Unit) {
        @Suppress("DEPRECATION")
        configure(kotlinOptions)
    }

    fun kotlinOptions(configure: Action<@UnsafeVariance T>) {
        @Suppress("DEPRECATION")
        configure.execute(kotlinOptions)
    }

    fun attributes(configure: AttributeContainer.() -> Unit) = attributes.configure()
    fun attributes(configure: Action<AttributeContainer>) = attributes { configure.execute(this) }

    konst compileAllTaskName: String

    companion object {
        const konst MAIN_COMPILATION_NAME = "main"
        const konst TEST_COMPILATION_NAME = "test"
    }

    /**
     * Will add a [KotlinSourceSet] directly into this compilation.
     * This method is deprecated and targets Kotlin 2.0 for its remokonst.
     * After Kotlin 2.0 there will be exactly one SourceSet associated with a given Kotlin Compilation.
     *
     * In order to include other sources into the compilation, please build a hierarchy of Source Sets instead.
     * See: [KotlinSourceSet.dependsOn] or [KotlinTargetHierarchyDsl].
     * This approach is most applicable if
     * - The sources can be shared for multiple compilations
     * - The sources shall be analyzed in a different context than [defaultSourceSet]
     * - The project uses multiplatform and sources shall provide expects
     *
     *
     * Alternatively, when just including source files from another directory,
     * the [SourceDirectorySet] from the [defaultSourceSet] can be used.
     * This approach is most applicable if
     *  - sources are not intended to be shared across multiple compilations
     *  - sources shall be analyzed in the same context as other sources in the [defaultSourceSet]
     *
     * #### Example 1: Create a new 'utils' source set and make it available to the 'main' compilation:
     * ```kotlin
     * kotlin {
     *     konst compilation = target.compilations.getByName("main")
     *     konst utilsSourceSet = sourceSets.create("utils")
     *     compilation.defaultSourceSet.dependsOn(utilsSourceSet)
     * }
     * ```
     *
     * #### Example 2: Add 'src/utils/kotlin' to the main SourceSet
     * ```kotlin
     * kotlin {
     *     konst compilation = target.compilations.getByName("main")
     *     compilation.defaultSourceSet.kotlin.srcDir("src/utils/kotlin")
     * }
     * ```
     * Further details:
     * https://kotl.in/compilation-source-deprecation
     */
    @Deprecated("scheduled for remokonst with Kotlin 2.0")
    fun source(sourceSet: KotlinSourceSet)

    fun associateWith(other: KotlinCompilation<*>)

    konst associateWith: List<KotlinCompilation<*>>

    override fun getName(): String = compilationName

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    @Suppress("DEPRECATION")
    override konst relatedConfigurationNames: List<String>
        get() = super.relatedConfigurationNames + compileDependencyConfigurationName

    konst disambiguatedName
        get() = target.disambiguationClassifier + name
}

@Deprecated("Scheduled for remokonst with Kotlin 2.0")
interface KotlinCompilationToRunnableFiles<T : KotlinCommonOptionsDeprecated> : KotlinCompilation<T> {
    override konst runtimeDependencyConfigurationName: String

    override var runtimeDependencyFiles: FileCollection

    @Suppress("DEPRECATION")
    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    override konst relatedConfigurationNames: List<String>
        get() = super.relatedConfigurationNames + runtimeDependencyConfigurationName
}

@Deprecated("Scheduled for remokonst with Kotlin 2.0")
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "deprecation") // kept for compatibility
konst <T : KotlinCommonOptionsDeprecated> KotlinCompilation<T>.runtimeDependencyConfigurationName: String?
    get() = (this as? KotlinCompilationToRunnableFiles<T>)?.runtimeDependencyConfigurationName

@Deprecated("Scheduled for remokonst with Kotlin 2.0")
interface KotlinCompilationWithResources<T : KotlinCommonOptionsDeprecated> : KotlinCompilation<T> {
    konst processResourcesTaskName: String
}