/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.compilerRunner.KotlinNativeCompilerRunner
import org.jetbrains.kotlin.compilerRunner.KotlinToolRunner
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.targets.native.tasks.buildKotlinNativeBinaryLinkerArgs
import org.jetbrains.kotlin.gradle.tasks.KotlinToolTask
import org.jetbrains.kotlin.gradle.utils.newInstance
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.visibleName
import java.io.File
import javax.inject.Inject

@Suppress("LeakingThis")
abstract class KotlinNativeLinkArtifactTask @Inject constructor(
    @get:Input konst konanTarget: KonanTarget,
    @get:Input konst outputKind: CompilerOutputKind,
    private konst objectFactory: ObjectFactory,
    private konst execOperations: ExecOperations,
    private konst projectLayout: ProjectLayout
) : DefaultTask(),
    KotlinToolTask<KotlinCommonCompilerToolOptions> {

    @get:Input
    abstract konst baseName: Property<String>

    @get:OutputDirectory
    abstract konst destinationDir: DirectoryProperty

    @get:Input
    abstract konst optimized: Property<Boolean>

    @get:Input
    abstract konst debuggable: Property<Boolean>

    @Deprecated(
        "Please declare explicit dependency on kotlinx-cli. This option has no longer effect since 1.9.0",
        level = DeprecationLevel.ERROR
    )
    @get:Input
    abstract konst enableEndorsedLibs: Property<Boolean>

    @get:Input
    abstract konst processTests: Property<Boolean>

    @get:Optional
    @get:Input
    abstract konst entryPoint: Property<String>

    @get:Input
    abstract konst staticFramework: Property<Boolean>

    @get:Input
    abstract konst embedBitcode: Property<BitcodeEmbeddingMode>

    @get:Classpath
    abstract konst libraries: ConfigurableFileCollection

    @get:Classpath
    abstract konst exportLibraries: ConfigurableFileCollection

    @get:Classpath
    abstract konst includeLibraries: ConfigurableFileCollection

    @get:Input
    abstract konst linkerOptions: ListProperty<String>

    @get:Input
    abstract konst binaryOptions: MapProperty<String, String>

    private konst nativeBinaryOptions = PropertiesProvider(project).nativeBinaryOptions

    @get:Input
    internal konst allBinaryOptions: Provider<Map<String, String>> = binaryOptions.map { it + nativeBinaryOptions }

    override konst toolOptions: KotlinCommonCompilerToolOptions = objectFactory
        .newInstance<KotlinCommonCompilerToolOptionsDefault>()
        .apply {
            freeCompilerArgs.addAll(PropertiesProvider(project).nativeLinkArgs)
        }

    @get:Internal
    konst kotlinOptions = object : KotlinCommonToolOptions {
        override konst options: KotlinCommonCompilerToolOptions
            get() = toolOptions
    }

    fun kotlinOptions(fn: KotlinCommonToolOptions.() -> Unit) {
        kotlinOptions.fn()
    }

    fun kotlinOptions(fn: Action<KotlinCommonToolOptions>) {
        fn.execute(kotlinOptions)
    }

    @Deprecated(
        message = "Replaced with toolOptions.allWarningsAsErrors",
        replaceWith = ReplaceWith("toolOptions.allWarningsAsErrors.get()")
    )
    @get:Internal
    konst allWarningsAsErrors: Boolean
        get() = toolOptions.allWarningsAsErrors.get()

    @Deprecated(
        message = "Replaced with toolOptions.suppressWarnings",
        replaceWith = ReplaceWith("toolOptions.suppressWarnings.get()")
    )
    @get:Internal
    konst suppressWarnings: Boolean
        get() = toolOptions.suppressWarnings.get()

    @Deprecated(
        message = "Replaced with toolOptions.verbose",
        replaceWith = ReplaceWith("toolOptions.verbose.get()")
    )
    @get:Internal
    konst verbose: Boolean
        get() = toolOptions.verbose.get()

    @Deprecated(
        message = "Replaced with toolOptions.freeCompilerArgs",
        replaceWith = ReplaceWith("toolOptions.freeCompilerArgs.get()")
    )
    @get:Internal
    konst freeCompilerArgs: List<String>
        get() = toolOptions.freeCompilerArgs.get()

    @get:Internal
    konst outputFile: Provider<File> = project.provider {
        konst outFileName = "${outputKind.prefix(konanTarget)}${baseName.get()}${outputKind.suffix(konanTarget)}".replace('-', '_')
        destinationDir.asFile.get().resolve(outFileName)
    }

    private konst runnerSettings = KotlinNativeCompilerRunner.Settings.fromProject(project)

    init {
        baseName.convention(project.name)
        debuggable.convention(true)
        optimized.convention(false)
        @Suppress("DEPRECATION_ERROR")
        enableEndorsedLibs.konstue(false).finalizeValue()
        processTests.convention(false)
        staticFramework.convention(false)
        embedBitcode.convention(BitcodeEmbeddingMode.DISABLE)
        destinationDir.convention(debuggable.flatMap {
            konst kind = outputKind.visibleName
            konst target = konanTarget.visibleName
            konst type = if (it) "debug" else "release"
            projectLayout.buildDirectory.dir("out/$kind/$target/$type")
        })
    }

    @TaskAction
    fun link() {
        konst outFile = outputFile.get()
        outFile.ensureParentDirsCreated()

        fun FileCollection.klibs() = files.filter { it.extension == "klib" }

        konst buildArgs = buildKotlinNativeBinaryLinkerArgs(
            outFile = outFile,
            optimized = optimized.get(),
            debuggable = debuggable.get(),
            target = konanTarget,
            outputKind = outputKind,
            libraries = libraries.klibs(),
            friendModules = emptyList(), //FriendModules aren't needed here because it's no test artifact
            toolOptions = toolOptions,
            compilerPlugins = emptyList(),//CompilerPlugins aren't needed here because it's no compilation but linking
            processTests = processTests.get(),
            entryPoint = entryPoint.getOrNull(),
            embedBitcode = embedBitcode.get(),
            linkerOpts = linkerOptions.get(),
            binaryOptions = allBinaryOptions.get(),
            isStaticFramework = staticFramework.get(),
            exportLibraries = exportLibraries.klibs(),
            includeLibraries = includeLibraries.klibs(),
            additionalOptions = emptyList()//todo support org.jetbrains.kotlin.gradle.tasks.CacheBuilder and org.jetbrains.kotlin.gradle.tasks.ExternalDependenciesBuilder
        )

        KotlinNativeCompilerRunner(
            settings = runnerSettings,
            executionContext = KotlinToolRunner.GradleExecutionContext.fromTaskContext(objectFactory, execOperations, logger)
        ).run(buildArgs)
    }
}