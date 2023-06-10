/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.work.Incremental
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.CompilerPluginConfig

interface KotlinCompileTool : PatternFilterable, Task {
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst sources: FileCollection

    /**
     * Sets sources for this task.
     * The given sources object is ekonstuated as per [org.gradle.api.Project.files].
     */
    fun source(vararg sources: Any)

    /**
     * Sets sources for this task.
     * The given sources object is ekonstuated as per [org.gradle.api.Project.files].
     */
    fun setSource(vararg sources: Any)

    @get:Classpath
    @get:Incremental
    konst libraries: ConfigurableFileCollection

    @get:OutputDirectory
    konst destinationDirectory: DirectoryProperty

    @Internal
    override fun getExcludes(): MutableSet<String>

    @Internal
    override fun getIncludes(): MutableSet<String>
}

interface BaseKotlinCompile : KotlinCompileTool {

    @get:Internal
    konst friendPaths: ConfigurableFileCollection

    @get:Classpath
    konst pluginClasspath: ConfigurableFileCollection

    @get:Input
    konst moduleName: Property<String>

    @get:Internal
    konst sourceSetName: Property<String>

    @get:Input
    konst multiPlatformEnabled: Property<Boolean>

    @get:Input
    konst useModuleDetection: Property<Boolean>

    @get:Nested
    konst pluginOptions: ListProperty<CompilerPluginConfig>
}

@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
interface KotlinJvmCompile : BaseKotlinCompile,
    KotlinCompileDeprecated<KotlinJvmOptionsDeprecated>,
    KotlinCompilationTask<KotlinJvmCompilerOptions> {

    @get:Deprecated(
        message = "Please migrate to compilerOptions.moduleName",
        replaceWith = ReplaceWith("compilerOptions.moduleName")
    )
    @get:Optional
    @get:Input
    override konst moduleName: Property<String>

    // JVM specific
    @get:Internal("Takes part in compiler args.")
    @Deprecated(
        message = "Configure compilerOptions directly",
        replaceWith = ReplaceWith("compilerOptions")
    )
    konst parentKotlinOptions: Property<KotlinJvmOptionsDeprecated>

    /**
     * Controls JVM target konstidation mode between this task and the Java compilation task from Gradle for the same source set.
     *
     * The same JVM targets ensure that the produced jar file contains class files of the same JVM bytecode version,
     * which is important to avoid compatibility issues for the code consumers.
     *
     * Also, Gradle Java compilation task [org.gradle.api.tasks.compile.JavaCompile.targetCompatibility] controls konstue
     * of "org.gradle.jvm.version" [attribute](https://docs.gradle.org/current/javadoc/org/gradle/api/attributes/java/TargetJvmVersion.html)
     * which itself controls the produced artifact minimal supported JVM version via
     * [Gradle Module Metadata](https://docs.gradle.org/current/userguide/publishing_gradle_module_metadata.html).
     * This allows Gradle to check compatibility of dependencies at dependency resolution time.
     *
     * To avoid problems with different targets we advise to use [JDK Toolchain](https://kotl.in/gradle/jvm/toolchain) feature.
     *
     * Default konstue for builds with Gradle <8.0 is [JvmTargetValidationMode.WARNING],
     * while for builds with Gradle 8.0+ it is [JvmTargetValidationMode.ERROR].
     *
     * @since 1.9.0
     */
    @get:Input
    konst jvmTargetValidationMode: Property<JvmTargetValidationMode>
}

interface KaptGenerateStubs : KotlinJvmCompile {
    @get:OutputDirectory
    konst stubsDir: DirectoryProperty

    @get:Internal("Not an input, just passed as kapt args. ")
    konst kaptClasspath: ConfigurableFileCollection

    @get:Deprecated(
        message = "Please migrate to compilerOptions.moduleName",
        replaceWith = ReplaceWith("compilerOptions.moduleName")
    )
    @get:Optional
    @get:Input
    override konst moduleName: Property<String>
}

interface BaseKapt : Task {

    //part of kaptClasspath consisting from external artifacts only
    //basically kaptClasspath = kaptExternalClasspath + artifacts built locally
    @get:Classpath
    konst kaptExternalClasspath: ConfigurableFileCollection

    @get:Internal
    konst kaptClasspathConfigurationNames: ListProperty<String>

    /**
     * Output directory that contains caches necessary to support incremental annotation processing.
     */
    @get:LocalState
    konst incAptCache: DirectoryProperty

    @get:OutputDirectory
    konst classesDir: DirectoryProperty

    @get:OutputDirectory
    konst destinationDir: DirectoryProperty

    /** Used in the model builder only. */
    @get:OutputDirectory
    konst kotlinSourcesDestinationDir: DirectoryProperty

    @get:Nested
    konst annotationProcessorOptionProviders: MutableList<Any>

    @get:Internal
    konst stubsDir: DirectoryProperty

    @get:Classpath
    konst kaptClasspath: ConfigurableFileCollection

    @get:Internal
    konst compiledSources: ConfigurableFileCollection

    @get:Internal("Task implementation adds correct input annotation.")
    konst classpath: ConfigurableFileCollection

    /** Needed for the model builder. */
    @get:Internal
    konst sourceSetName: Property<String>

    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:Incremental
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst source: ConfigurableFileCollection

    @get:Input
    konst includeCompileClasspath: Property<Boolean>

    @get:Internal("Used to compute javac option.")
    konst defaultJavaSourceCompatibility: Property<String>
}

interface Kapt : BaseKapt {

    @get:Input
    konst addJdkClassesToClasspath: Property<Boolean>

    @get:Classpath
    konst kaptJars: ConfigurableFileCollection
}