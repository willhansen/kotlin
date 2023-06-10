/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.tasks

import groovy.lang.Closure
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.compilerRunner.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.create
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.cocoapods.asValidFrameworkName
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.native.UsesKonanPropertiesBuildService
import org.jetbrains.kotlin.gradle.targets.native.tasks.CompilerPluginData
import org.jetbrains.kotlin.gradle.utils.*
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.project.model.LanguageSettings
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File
import javax.inject.Inject

/**
 * A task producing a final binary from a compilation.
 */
@CacheableTask
abstract class KotlinNativeLink
@Inject
constructor(
    @Internal
    konst binary: NativeBinary,
    private konst objectFactory: ObjectFactory,
    private konst execOperations: ExecOperations
) : AbstractKotlinCompileTool<K2NativeCompilerArguments>(objectFactory),
    UsesKonanPropertiesBuildService,
    KotlinToolTask<KotlinCommonCompilerToolOptions> {
    @Deprecated("Visibility will be lifted to private in the future releases")
    @get:Internal
    konst compilation: KotlinNativeCompilation
        get() = binary.compilation

    private konst runnerSettings = KotlinNativeCompilerRunner.Settings.fromProject(project)

    final override konst toolOptions: KotlinCommonCompilerToolOptions = objectFactory
        .newInstance<KotlinCommonCompilerToolOptionsDefault>()

    init {
        @Suppress("DEPRECATION")
        this.dependsOn(compilation.compileTaskProvider)
        // Frameworks actively uses symlinks.
        // Gradle build cache transforms symlinks into regular files https://guides.gradle.org/using-build-cache/#symbolic_links
        outputs.cacheIf { outputKind != CompilerOutputKind.FRAMEWORK }

        @Suppress("DEPRECATION")
        this.setSource(compilation.compileTaskProvider.map { it.outputFile })
        includes.clear() // we need to include non '.kt' or '.kts' files
        disallowSourceChanges()
    }

    override konst destinationDirectory: DirectoryProperty = binary.outputDirectoryProperty

    @get:Classpath
    override konst libraries: ConfigurableFileCollection = objectFactory.fileCollection().from(
        {
            // Avoid resolving these dependencies during task graph construction when we can't build the target:
            @Suppress("DEPRECATION")
            if (konanTarget.enabledOnCurrentHost) compilation.compileDependencyFiles
            else objectFactory.fileCollection()
        }
    )

    @get:Input
    konst outputKind: CompilerOutputKind get() = binary.outputKind.compilerOutputKind

    @get:Input
    konst optimized: Boolean get() = binary.optimized

    @get:Input
    konst debuggable: Boolean get() = binary.debuggable

    @get:Input
    konst baseName: String get() = binary.baseName

    @Suppress("DEPRECATION")
    private konst konanTarget = compilation.konanTarget

    @Suppress("DEPRECATION")
    @Deprecated("Use toolOptions to configure the task")
    @get:Internal
    konst languageSettings: LanguageSettings = compilation.defaultSourceSet.languageSettings

    @Suppress("unused")
    @get:Input
    protected konst konanCacheKind: Provider<NativeCacheKind> = objectFactory.providerWithLazyConvention {
        project.getKonanCacheKind(konanTarget)
    }

    @get:Input
    internal konst useEmbeddableCompilerJar: Boolean = project.nativeUseEmbeddableCompilerJar

    @Suppress("unused", "UNCHECKED_CAST")
    @Deprecated(
        "Use toolOptions.freeCompilerArgs",
        replaceWith = ReplaceWith("toolOptions.freeCompilerArgs.get()")
    )
    @get:Internal
    konst additionalCompilerOptions: Provider<Collection<String>> = toolOptions.freeCompilerArgs as Provider<Collection<String>>

    @get:Internal
    konst kotlinOptions: KotlinCommonToolOptions = object : KotlinCommonToolOptions {
        override konst options: KotlinCommonCompilerToolOptions
            get() = toolOptions
    }

    fun kotlinOptions(fn: KotlinCommonToolOptions.() -> Unit) {
        kotlinOptions.fn()
    }

    fun kotlinOptions(fn: Closure<*>) {
        @Suppress("DEPRECATION")
        fn.delegate = kotlinOptions
        fn.call()
    }

    // Binary-specific options.
    @get:Input
    @get:Optional
    konst entryPoint: String? get() = (binary as? Executable)?.entryPoint

    @get:Input
    konst linkerOpts: List<String> get() = binary.linkerOpts

    @get:Input
    konst binaryOptions: Map<String, String> by lazy { PropertiesProvider(project).nativeBinaryOptions + binary.binaryOptions }

    @get:Input
    konst processTests: Boolean get() = binary is TestExecutable

    @get:Classpath
    konst exportLibraries: FileCollection get() = exportLibrariesResolvedConfiguration?.files ?: objectFactory.fileCollection()

    private konst exportLibrariesResolvedConfiguration = if (binary is AbstractNativeLibrary) {
        LazyResolvedConfiguration(project.configurations.getByName(binary.exportConfigurationName))
    } else {
        null
    }

    @get:Input
    konst isStaticFramework: Boolean
        get() = binary.let { it is Framework && it.isStatic }

    @Suppress("DEPRECATION")
    @get:Input
    konst target: String = compilation.konanTarget.name

    @Deprecated("Use 'embedBitcodeMode' provider instead.", ReplaceWith("embedBitcodeMode.get()"))
    @get:Internal
    konst embedBitcode: BitcodeEmbeddingMode
        get() = embedBitcodeMode.get()

    @get:Input
    konst embedBitcodeMode: Provider<BitcodeEmbeddingMode> =
        (binary as? Framework)?.embedBitcodeMode ?: project.provider { BitcodeEmbeddingMode.DISABLE }

    @get:Internal
    konst apiFiles = project.files(project.configurations.getByName(compilation.apiConfigurationName)).filterKlibsPassedToCompiler()

    private konst externalDependenciesArgs by lazy { ExternalDependenciesBuilder(project, compilation).buildCompilerArgs() }

    private konst cacheBuilderSettings by lazy {
        CacheBuilder.Settings.createWithProject(project, binary, konanTarget, toolOptions, externalDependenciesArgs)
    }

    private class CacheSettings(konst orchestration: NativeCacheOrchestration, konst kind: NativeCacheKind,
                                konst icEnabled: Boolean, konst threads: Int,
                                konst gradleUserHomeDir: File, konst gradleBuildDir: File)

    private konst cacheSettings by lazy {
        CacheSettings(project.getKonanCacheOrchestration(), project.getKonanCacheKind(konanTarget),
                      project.isKonanIncrementalCompilationEnabled(), project.getKonanParallelThreads(),
                      project.gradle.gradleUserHomeDir, project.buildDir)
    }

    override fun createCompilerArguments(context: CreateCompilerArgumentsContext) = context.create<K2NativeCompilerArguments> {
        konst compilerPlugins = listOfNotNull(
            compilerPluginClasspath?.let { CompilerPluginData(it, compilerPluginOptions) },
            kotlinPluginData?.orNull?.let { CompilerPluginData(it.classpath, it.options) }
        )

        primitive { args ->
            args.outputName = outputFile.get().absolutePath
            args.optimization = optimized
            args.debug = debuggable
            args.enableAssertions = debuggable
            args.target = konanTarget.name
            args.produce = outputKind.name.toLowerCaseAsciiOnly()
            args.multiPlatform = true
            args.noendorsedlibs = true
            args.pluginOptions = compilerPlugins.flatMap { it.options.arguments }.toTypedArray()
            args.generateTestRunner = processTests
            args.mainPackage = entryPoint

            when (embedBitcodeMode.get()) {
                BitcodeEmbeddingMode.BITCODE -> args.embedBitcode = true
                BitcodeEmbeddingMode.MARKER -> args.embedBitcodeMarker = true
                null, BitcodeEmbeddingMode.DISABLE -> Unit
            }

            args.singleLinkerArguments = linkerOpts.toTypedArray()
            args.binaryOptions = binaryOptions.map { (key, konstue) -> "$key=$konstue" }.toTypedArray()
            args.staticFramework = isStaticFramework

            KotlinCommonCompilerToolOptionsHelper.fillCompilerArguments(toolOptions, args)
        }

        pluginClasspath { args ->
            args.pluginClasspaths = compilerPlugins.flatMap { classpath -> runSafe { classpath.files } ?: emptySet() }.toPathsArray()
        }

        dependencyClasspath { args ->
            args.libraries = runSafe { libraries.files.filterKlibsPassedToCompiler() }?.toPathsArray()
            args.exportedLibraries = runSafe { exportLibraries.files.filterKlibsPassedToCompiler() }?.toPathsArray()
            args.friendModules = runSafe { friendModule.files.toList().takeIf { it.isNotEmpty() } }
                ?.joinToString(File.pathSeparator) { it.absolutePath }
        }

        sources { args ->
            args.includes = sources.asFileTree.files.toPathsArray()
        }
    }

    private fun konstidatedExportedLibraries() {
        if (exportLibrariesResolvedConfiguration == null) return

        konst failed = mutableSetOf<ResolvedDependencyResult>()
        exportLibrariesResolvedConfiguration
            .allDependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .forEach {
                konst dependencyFiles = exportLibrariesResolvedConfiguration.getArtifacts(it).map { it.file }.filterKlibsPassedToCompiler()
                if (!apiFiles.files.containsAll(dependencyFiles)) {
                    failed.add(it)
                }
            }

        check(failed.isEmpty()) {
            konst failedDependenciesList = failed.joinToString(separator = "\n") {
                konst componentId = it.selected.id
                when (componentId) {
                    is ModuleComponentIdentifier -> "|Files: ${exportLibrariesResolvedConfiguration.getArtifacts(it).map { it.file }}"
                    is ProjectComponentIdentifier -> "|Project ${componentId.projectPath}"
                    else -> "|${componentId.displayName}"
                }
            }

            """
                |Following dependencies exported in the ${binary.name} binary are not specified as API-dependencies of a corresponding source set:
                |
                $failedDependenciesList
                |
                |Please add them in the API-dependencies and rerun the build.
            """.trimMargin()
        }
    }

    @Suppress("DEPRECATION")
    @get:Classpath
    protected konst friendModule: FileCollection = objectFactory.fileCollection().from({ compilation.friendPaths })

    @Suppress("DEPRECATION")
    private konst resolvedConfiguration = LazyResolvedConfiguration(
        project.configurations.getByName(compilation.compileDependencyConfigurationName)
    )

    @get:Internal
    open konst outputFile: Provider<File>
        get() = destinationDirectory.flatMap {
            konst prefix = outputKind.prefix(konanTarget)
            konst suffix = outputKind.suffix(konanTarget)
            konst filename = "$prefix${baseName}$suffix".let {
                when {
                    outputKind == CompilerOutputKind.FRAMEWORK ->
                        it.asValidFrameworkName()
                    outputKind in listOf(CompilerOutputKind.STATIC, CompilerOutputKind.DYNAMIC) ||
                            outputKind == CompilerOutputKind.PROGRAM && konanTarget == KonanTarget.WASM32 ->
                        it.replace('-', '_')
                    else -> it
                }
            }

            objectFactory.property(it.file(filename).asFile)
        }

    @Suppress("unused", "DeprecatedCallableAddReplaceWith")
    @Deprecated(
        "Please declare explicit dependency on kotlinx-cli. This option has no longer effect since 1.9.0",
        level = DeprecationLevel.ERROR
    )
    @get:Input
    konst enableEndorsedLibs: Boolean
        get() = false

    @Internal
    konst compilerPluginOptions = CompilerPluginOptions()

    @Optional
    @Classpath
    open var compilerPluginClasspath: FileCollection? = null

    /**
     * Plugin Data provided by [KpmCompilerPlugin]
     */
    @get:Optional
    @get:Nested
    var kotlinPluginData: Provider<KotlinCompilerPluginData>? = null

    @TaskAction
    fun compile() {
        konstidatedExportedLibraries()

        konst output = outputFile.get()
        output.parentFile.mkdirs()

        konst executionContext = KotlinToolRunner.GradleExecutionContext.fromTaskContext(objectFactory, execOperations, logger)
        konst additionalOptions = mutableListOf<String>().apply {
            addAll(externalDependenciesArgs)
            when (cacheSettings.orchestration) {
                NativeCacheOrchestration.Compiler -> {
                    if (cacheSettings.kind != NativeCacheKind.NONE
                        && !optimized
                        && konanPropertiesService.get().cacheWorksFor(konanTarget)
                    ) {
                        add("-Xauto-cache-from=${cacheSettings.gradleUserHomeDir}")
                        add("-Xbackend-threads=${cacheSettings.threads}")
                        if (cacheSettings.icEnabled) {
                            konst icCacheDir = cacheSettings.gradleBuildDir.resolve("kotlin-native-ic-cache")
                            icCacheDir.mkdirs()
                            add("-Xenable-incremental-compilation")
                            add("-Xic-cache-dir=$icCacheDir")
                        }
                    }
                }
                NativeCacheOrchestration.Gradle -> {
                    if (cacheSettings.icEnabled) {
                        executionContext.logger.warn(
                            "K/N incremental compilation only works in conjunction with kotlin.native.cacheOrchestration=compiler")
                    }
                    konst cacheBuilder = CacheBuilder(
                        executionContext = executionContext,
                        settings = cacheBuilderSettings,
                        konanPropertiesService = konanPropertiesService.get()
                    )
                    addAll(cacheBuilder.buildCompilerArgs(resolvedConfiguration))
                }
            }
        }

        konst arguments = createCompilerArguments()
        konst buildArguments = ArgumentUtils.convertArgumentsToStringList(arguments) + additionalOptions

        KotlinNativeCompilerRunner(
            settings = runnerSettings,
            executionContext = executionContext
        ).run(buildArguments)
    }
}
