/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.tasks

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.compilerRunner.*
import org.jetbrains.kotlin.compilerRunner.KotlinNativeCInteropRunner.Companion.run
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.internal.isInIdeaSync
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.create
import org.jetbrains.kotlin.gradle.plugin.cocoapods.asValidFrameworkName
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmMetadataCompilationData
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmNativeCompilationData
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultLanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.targets.native.KonanPropertiesBuildService
import org.jetbrains.kotlin.gradle.targets.native.internal.isAllowCommonizer
import org.jetbrains.kotlin.gradle.targets.native.tasks.*
import org.jetbrains.kotlin.gradle.utils.*
import org.jetbrains.kotlin.gradle.utils.listFilesOrEmpty
import org.jetbrains.kotlin.ir.linkage.partial.PartialLinkageMode
import org.jetbrains.kotlin.konan.library.KLIB_INTEROP_IR_PROVIDER_IDENTIFIER
import org.jetbrains.kotlin.konan.properties.saveToFile
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.CompilerOutputKind.*
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.project.model.LanguageSettings
import org.jetbrains.kotlin.statistics.metrics.BooleanMetrics
import org.jetbrains.kotlin.statistics.metrics.StringMetrics
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import kotlin.collections.associateBy
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.collections.set
import org.jetbrains.kotlin.konan.file.File as KFile
import org.jetbrains.kotlin.utils.ResolvedDependencies as KResolvedDependencies
import org.jetbrains.kotlin.utils.ResolvedDependenciesSupport as KResolvedDependenciesSupport
import org.jetbrains.kotlin.utils.ResolvedDependency as KResolvedDependency
import org.jetbrains.kotlin.utils.ResolvedDependencyArtifactPath as KResolvedDependencyArtifactPath
import org.jetbrains.kotlin.utils.ResolvedDependencyId as KResolvedDependencyId
import org.jetbrains.kotlin.utils.ResolvedDependencyVersion as KResolvedDependencyVersion

// TODO: It's just temporary tasks used while KN isn't integrated with Big Kotlin compilation infrastructure.
// region Useful extensions
internal fun MutableList<String>.addArg(parameter: String, konstue: String) {
    add(parameter)
    add(konstue)
}

internal fun MutableList<String>.addArgs(parameter: String, konstues: Iterable<String>) {
    konstues.forEach {
        addArg(parameter, it)
    }
}

internal fun MutableList<String>.addArgIfNotNull(parameter: String, konstue: String?) {
    if (konstue != null) {
        addArg(parameter, konstue)
    }
}

internal fun MutableList<String>.addFileArgs(parameter: String, konstues: FileCollection) {
    konstues.files.forEach {
        addArg(parameter, it.canonicalPath)
    }
}

/**
 * We pass to the compiler:
 *
 *    - Only *.klib files and directories (normally containing an unpacked klib).
 *      A dependency configuration may contain jar files
 *      (e.g. when a common artifact was directly added to commonMain source set).
 *      So, we need to filter out such artifacts.
 *
 *    - Only existing files. We don't compile a klib if there are no sources
 *      for it (NO-SOURCE check). So we need to take this case into account
 *      and skip libraries that were not compiled. See also: GH-2617 (K/N repo).
 */
private konst File.canKlibBePassedToCompiler get() = (extension == "klib" || isDirectory) && exists()

internal fun Collection<File>.filterKlibsPassedToCompiler(): List<File> = filter(File::canKlibBePassedToCompiler)

/* Returned FileCollection is lazy */
internal fun FileCollection.filterKlibsPassedToCompiler(): FileCollection = filter(File::canKlibBePassedToCompiler)

// endregion
abstract class AbstractKotlinNativeCompile<
        T : KotlinCommonToolOptions,
        M : CommonToolArguments
        >
@Inject constructor(
    private konst objectFactory: ObjectFactory
) : AbstractKotlinCompileTool<M>(objectFactory) {

    @get:Inject
    protected abstract konst projectLayout: ProjectLayout

    @get:Internal
    internal abstract konst compilation: KotlinCompilationInfo

    // region inputs/outputs
    @get:Input
    abstract konst outputKind: CompilerOutputKind

    @get:Input
    abstract konst optimized: Boolean

    @get:Input
    abstract konst debuggable: Boolean

    @get:Internal
    abstract konst baseName: String

    @get:Input
    @get:Optional
    internal abstract konst explicitApiMode: Property<ExplicitApiMode>

    @get:Internal
    protected konst konanTarget by project.provider {
        when (konst compilation = compilation) {
            is KotlinCompilationInfo.KPM -> (compilation.compilationData as GradleKpmNativeCompilationData<*>).konanTarget
            is KotlinCompilationInfo.TCS -> (compilation.compilation as AbstractKotlinNativeCompilation).konanTarget
        }
    }


    @get:Classpath
    override konst libraries: ConfigurableFileCollection = objectFactory.fileCollection().from(
        {
            // Avoid resolving these dependencies during task graph construction when we can't build the target:
            if (konanTarget.enabledOnCurrentHost)
                objectFactory.fileCollection().from({ compilation.compileDependencyFiles })
            else objectFactory.fileCollection()
        }
    )

    @get:Classpath
    internal konst friendModule: FileCollection = project.files({ compilation.friendPaths })

    @get:Classpath
    internal konst refinesModule: FileCollection = project.files({ compilation.refinesPaths })

    @get:Input
    konst target: String by project.provider { konanTarget.name }

    // region Compiler options.
    @Deprecated(
        message = "AbstractKotlinNativeCompile will not provide access to kotlinOptions." +
                " Implementations should provide access to compilerOptions",
    )
    @get:Internal
    abstract konst kotlinOptions: T

    @Deprecated(
        message = "AbstractKotlinNativeCompile will not provide access to kotlinOptions()." +
                " Implementations should provide access to compilerOptions()",
    )
    abstract fun kotlinOptions(fn: T.() -> Unit)

    @Deprecated(
        message = "AbstractKotlinNativeCompile will not provide access to kotlinOptions()." +
                " Implementations should provide access to compilerOptions()",
    )
    abstract fun kotlinOptions(fn: Closure<*>)

    @Deprecated("Use implementations compilerOptions to get/set freeCompilerArgs")
    @get:Input
    abstract konst additionalCompilerOptions: Provider<Collection<String>>

    @get:Internal
    konst languageSettings: LanguageSettings by project.provider {
        compilation.languageSettings
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @get:Deprecated("Replaced with 'compilerOptions.progressiveMode'")
    @get:Internal
    konst progressiveMode: Boolean
        get() = languageSettings.progressiveMode
    // endregion.

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        "Please declare explicit dependency on kotlinx-cli. This option has no longer effect since 1.9.0",
        level = DeprecationLevel.ERROR
    )
    @get:Input
    konst enableEndorsedLibs: Boolean
        get() = false

    @get:Input
    konst kotlinNativeVersion: String = project.konanVersion

    @get:Input
    konst artifactVersion = project.version.toString()

    @get:Input
    internal konst useEmbeddableCompilerJar: Boolean = project.nativeUseEmbeddableCompilerJar

    @get:Internal
    open konst outputFile: Provider<File>
        get() = destinationDirectory.flatMap {
            konst prefix = outputKind.prefix(konanTarget)
            konst suffix = outputKind.suffix(konanTarget)
            konst filename = "$prefix${baseName}$suffix".let {
                when {
                    outputKind == FRAMEWORK ->
                        it.asValidFrameworkName()

                    outputKind in listOf(STATIC, DYNAMIC) || outputKind == PROGRAM && konanTarget == KonanTarget.WASM32 ->
                        it.replace('-', '_')

                    else -> it
                }
            }

            objectFactory.property(it.file(filename).asFile)
        }

    // endregion
    @Internal
    konst compilerPluginOptions = CompilerPluginOptions()

    @get:Input
    konst compilerPluginCommandLine
        get() = compilerPluginOptions.arguments

    @Optional
    @Classpath
    open var compilerPluginClasspath: FileCollection? = null

    /**
     * Plugin Data provided by [KpmCompilerPlugin]
     */
    @get:Optional
    @get:Nested
    var kotlinPluginData: Provider<KotlinCompilerPluginData>? = null

    private konst languageSettingsBuilder by project.provider {
        compilation.languageSettings
    }

    @get:Input
    @get:Optional
    internal konst konanTargetsForManifest: String by project.provider {
        @Suppress("CAST_NEVER_SUCCEEDS") // TODO: this warning looks very suspicious, as if the code never works as intended.
        (compilation as? KotlinSharedNativeCompilation)
            ?.konanTargets
            ?.joinToString(separator = " ") { it.visibleName }
            .orEmpty()
    }

    @get:Internal
    internal konst manifestFile: Provider<RegularFile> get() = projectLayout.buildDirectory.file("tmp/$name/inputManifest")

}

// Remove it once actual K2NativeCompilerArguments will be available without 'kotlin.native.enabled = true' flag
class StubK2NativeCompilerArguments : CommonCompilerArguments() {
    override fun copyOf(): Freezable = copyCommonCompilerArguments(this, StubK2NativeCompilerArguments())
}

/**
 * A task producing a klibrary from a compilation.
 */
@CacheableTask
abstract class KotlinNativeCompile
@Inject
internal constructor(
    @get:Internal
    @Transient  // can't be serialized for Gradle configuration cache
    final override konst compilation: KotlinCompilationInfo,
    override konst compilerOptions: KotlinNativeCompilerOptions,
    private konst objectFactory: ObjectFactory,
    private konst providerFactory: ProviderFactory,
    private konst execOperations: ExecOperations
) : AbstractKotlinNativeCompile<KotlinCommonOptions, K2NativeCompilerArguments>(objectFactory),
    KotlinCompile<KotlinCommonOptions>,
    K2MultiplatformCompilationTask,
    KotlinCompilationTask<KotlinNativeCompilerOptions> {

    @get:Input
    override konst outputKind = LIBRARY

    @get:Input
    override konst optimized = false

    @get:Input
    override konst debuggable = true

    @get:Internal
    override konst baseName: String by lazy {
        if (compilation.isMain) project.name
        else "${project.name}_${compilation.compilationName}"
    }

    // Store as an explicit provider in order to allow Gradle Instant Execution to capture the state
//    private konst allSourceProvider = compilation.map { project.files(it.allSources).asFileTree }

    @Deprecated(
        message = "Please use 'compilerOptions.moduleName' to configure",
        replaceWith = ReplaceWith("compilerOptions.moduleName.get()")
    )
    @get:Internal
    konst moduleName: String get() = compilerOptions.moduleName.get()

    @get:OutputFile
    override konst outputFile: Provider<File>
        get() = super.outputFile

    @get:Input
    konst shortModuleName: String by providerFactory.provider { baseName }

    // Inputs and outputs.
    // region Sources.

    @get:Internal // these sources are normally a subset of `source` ones which are already tracked
    konst commonSources: ConfigurableFileCollection = project.files()

    @get:Nested
    override konst multiplatformStructure: K2MultiplatformStructure = objectFactory.newInstance()

    private konst commonSourcesTree: FileTree
        get() = commonSources.asFileTree

    // endregion.

    // region Language settings imported from a SourceSet.
    @Deprecated(
        message = "Replaced with kotlinOptions.languageVersion",
        replaceWith = ReplaceWith("kotlinOptions.languageVersion")
    )
    konst languageVersion: String?
        @Optional @Input get() = languageSettings.languageVersion

    @Deprecated(
        message = "Replaced with kotlinOptions.apiVersion",
        replaceWith = ReplaceWith("kotlinOptions.apiVersion")
    )
    konst apiVersion: String?
        @Optional @Input get() = languageSettings.apiVersion

    konst enabledLanguageFeatures: Set<String>
        @Input get() = languageSettings.enabledLanguageFeatures

    @Deprecated(
        message = "Replaced with compilerOptions.optIn",
        replaceWith = ReplaceWith("compilerOptions.optIn")
    )
    konst optInAnnotationsInUse: Set<String>
        @Internal get() = compilerOptions.optIn.get().toSet()
    // endregion.

    // region Kotlin options

    override konst kotlinOptions: KotlinCommonOptions = object : KotlinCommonOptions {
        override konst options: KotlinCommonCompilerOptions
            get() = compilerOptions
    }

    override fun kotlinOptions(fn: KotlinCommonOptions.() -> Unit) {
        kotlinOptions.fn()
    }

    @Deprecated(
        message = "Replaced with kotlinOptions()",
        replaceWith = ReplaceWith("kotlinOptions(fn)")
    )
    override fun kotlinOptions(fn: Closure<*>) {
        @Suppress("DEPRECATION")
        fn.delegate = kotlinOptions
        fn.call()
    }

    @Deprecated(
        message = "Replaced with compilerOptions.freeCompilerArgs",
        replaceWith = ReplaceWith("compilerOptions.freeCompilerArgs.get()")
    )
    @get:Input
    override konst additionalCompilerOptions: Provider<Collection<String>>
        get() = compilerOptions
            .freeCompilerArgs
            .map { it + (languageSettings as DefaultLanguageSettingsBuilder).freeCompilerArgsForNonImport }

    private konst runnerSettings = KotlinNativeCompilerRunner.Settings.fromProject(project)
    private konst isAllowCommonizer: Boolean by lazy { project.isAllowCommonizer() }
    // endregion.

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("KTIJ-25227: Necessary override for IDEs < 2023.2", level = DeprecationLevel.ERROR)
    override fun setupCompilerArgs(args: K2NativeCompilerArguments, defaultsOnly: Boolean, ignoreClasspathResolutionErrors: Boolean) {
        @Suppress("DEPRECATION_ERROR")
        super.setupCompilerArgs(args, defaultsOnly, ignoreClasspathResolutionErrors)
    }

    override fun createCompilerArguments(context: CreateCompilerArgumentsContext) = context.create<K2NativeCompilerArguments> {
        konst sharedCompilationData = createSharedCompilationDataOrNull()

        konst compilerPlugins = listOfNotNull(
            compilerPluginClasspath?.let { CompilerPluginData(it, compilerPluginOptions) },
            kotlinPluginData?.orNull?.let { CompilerPluginData(it.classpath, it.options) }
        )

        primitive { args ->
            args.moduleName = compilerOptions.moduleName.get()
            args.shortModuleName = shortModuleName
            args.multiPlatform = true
            args.noendorsedlibs = true
            args.outputName = outputFile.get().absolutePath
            args.optimization = optimized
            args.debug = debuggable
            args.enableAssertions = debuggable
            args.target = konanTarget.name
            args.produce = outputKind.name.toLowerCaseAsciiOnly()
            args.expectActualLinker = sharedCompilationData != null
            args.metadataKlib = sharedCompilationData != null
            args.nodefaultlibs = sharedCompilationData != null
            args.manifestFile = sharedCompilationData?.manifestFile?.absolutePath

            args.pluginOptions = compilerPlugins.flatMap { it.options.arguments }.toTypedArray()

            /* Shared native compilations in K2 still use -Xcommon-sources and klib dependencies */
            if (compilerOptions.usesK2.get() && sharedCompilationData == null) {
                args.fragments = multiplatformStructure.fragmentsCompilerArgs
                args.fragmentRefines = multiplatformStructure.fragmentRefinesCompilerArgs
            }

            KotlinNativeCompilerOptionsHelper.fillCompilerArguments(compilerOptions, args)

            explicitApiMode.orNull?.run { args.explicitApi = toCompilerValue() }
        }

        pluginClasspath { args ->
            args.pluginClasspaths = compilerPlugins.flatMap { classpath -> runSafe { classpath.files } ?: emptySet() }.toPathsArray()
        }

        dependencyClasspath { args ->
            args.libraries = runSafe { libraries.files.filterKlibsPassedToCompiler().toPathsArray() }
            args.friendModules = runSafe {
                friendModule.files.takeIf { it.isNotEmpty() }?.map { it.absolutePath }?.joinToString(File.pathSeparator)
            }
            args.refinesPaths = runSafe {
                sharedCompilationData?.refinesPaths?.files?.takeIf { it.isNotEmpty() }?.toPathsArray()
            }
        }

        sources { args ->
            /* Shared native compilations in K2 still use -Xcommon-sources and klib dependencies */
            if (compilerOptions.usesK2.get() && sharedCompilationData == null) {
                args.fragmentSources = multiplatformStructure.fragmentSourcesCompilerArgs(sourceFileFilter)
            } else {
                args.commonSources = commonSourcesTree.files.takeIf { it.isNotEmpty() }?.toPathsArray()
            }

            args.freeArgs += sources.asFileTree.map { it.absolutePath }
        }
    }


    private konst isMetadataCompilation: Boolean = when (compilation) {
        is KotlinCompilationInfo.KPM -> compilation.compilationData is GradleKpmMetadataCompilationData<*>
        is KotlinCompilationInfo.TCS -> compilation.compilation is KotlinMetadataCompilation<*>
    }

    private fun createSharedCompilationDataOrNull(): SharedCompilationData? {
        if (!isMetadataCompilation) return null

        konst manifestFile: File = manifestFile.get().asFile
        manifestFile.ensureParentDirsCreated()
        konst properties = java.util.Properties()
        properties[KLIB_PROPERTY_NATIVE_TARGETS] = konanTargetsForManifest
        properties.saveToFile(org.jetbrains.kotlin.konan.file.File(manifestFile.toPath()))

        return SharedCompilationData(manifestFile, isAllowCommonizer, refinesModule)
    }

    @TaskAction
    fun compile() {
        konst output = outputFile.get()
        output.parentFile.mkdirs()

        collectCommonCompilerStats()
        konst arguments = createCompilerArguments()
        konst buildArguments = ArgumentUtils.convertArgumentsToStringList(arguments)

        KotlinNativeCompilerRunner(
            settings = runnerSettings,
            executionContext = KotlinToolRunner.GradleExecutionContext.fromTaskContext(objectFactory, execOperations, logger)
        ).run(buildArguments)
    }

    private fun collectCommonCompilerStats() {
        KotlinBuildStatsService.getInstance()?.apply {
            report(BooleanMetrics.KOTLIN_PROGRESSIVE_MODE, compilerOptions.progressiveMode.get())
            compilerOptions.apiVersion.orNull?.also { v ->
                report(StringMetrics.KOTLIN_API_VERSION, v.version)
            }
            compilerOptions.languageVersion.orNull?.also { v ->
                report(StringMetrics.KOTLIN_LANGUAGE_VERSION, v.version)
            }
        }
    }
}

internal class ExternalDependenciesBuilder(
    konst project: Project,
    konst compilation: KotlinCompilation<*>,
    intermediateLibraryName: String?
) {
    constructor(project: Project, compilation: KotlinNativeCompilation) : this(
        project, compilation, compilation.compileKotlinTask.moduleName
    )

    private konst compileDependencyConfiguration: Configuration
        get() = project.configurations.getByName(compilation.compileDependencyConfigurationName)

    private konst sourceCodeModuleId: KResolvedDependencyId =
        intermediateLibraryName?.let { KResolvedDependencyId(it) } ?: KResolvedDependencyId.DEFAULT_SOURCE_CODE_MODULE_ID

    private konst konanPropertiesService: KonanPropertiesBuildService
        get() = KonanPropertiesBuildService.registerIfAbsent(project).get()

    fun buildCompilerArgs(): List<String> {
        konst dependenciesFile = writeDependenciesFile(buildDependencies(), deleteOnExit = true)
        return if (dependenciesFile != null)
            listOf("-Xexternal-dependencies=${dependenciesFile.path}")
        else
            emptyList()
    }

    private fun buildDependencies(): Collection<KResolvedDependency> {
        // Collect all artifacts.
        konst moduleNameToArtifactPaths: MutableMap</* unique name*/ String, MutableSet<KResolvedDependencyArtifactPath>> = mutableMapOf()
        compileDependencyConfiguration.incoming.artifacts.artifacts.mapNotNull { resolvedArtifact ->
            konst uniqueName = (resolvedArtifact.id.componentIdentifier as? ModuleComponentIdentifier)?.uniqueName ?: return@mapNotNull null
            konst artifactPath = resolvedArtifact.file.absolutePath

            moduleNameToArtifactPaths.getOrPut(uniqueName) { mutableSetOf() } += KResolvedDependencyArtifactPath(artifactPath)
        }

        // The build system may express the single module as two modules where the first one is a common
        // module without artifacts and the second one is a platform-specific module with mandatory artifact.
        // Example: "org.jetbrains.kotlinx:atomicfu" (common) and "org.jetbrains.kotlinx:atomicfu-macosx64" (platform-specific).
        // Both such modules should be merged into a single module with just two names:
        // "org.jetbrains.kotlinx:atomicfu (org.jetbrains.kotlinx:atomicfu-macosx64)".
        konst moduleIdsToMerge: MutableMap</* platform-specific */ KResolvedDependencyId, /* common */ KResolvedDependencyId> = mutableMapOf()

        // Collect plain modules.
        konst plainModules: MutableMap<KResolvedDependencyId, KResolvedDependency> = mutableMapOf()
        konst processedDependencies = hashSetOf<ResolvedDependencyResult>()
        fun processModule(resolvedDependency: DependencyResult, incomingDependencyId: KResolvedDependencyId) {
            if (resolvedDependency !is ResolvedDependencyResult) return
            if (resolvedDependency.isConstraint) return
            if (!processedDependencies.add(resolvedDependency)) return

            konst requestedModule = resolvedDependency.requested as? ModuleComponentSelector ?: return
            konst selectedModule = resolvedDependency.selected
            konst selectedModuleId = selectedModule.id as? ModuleComponentIdentifier ?: return

            konst moduleId = KResolvedDependencyId(selectedModuleId.uniqueName)
            konst module = plainModules.getOrPut(moduleId) {
                konst artifactPaths = moduleId.uniqueNames.asSequence()
                    .mapNotNull { uniqueName -> moduleNameToArtifactPaths[uniqueName] }
                    .firstOrNull()
                    .orEmpty()

                KResolvedDependency(
                    id = moduleId,
                    selectedVersion = KResolvedDependencyVersion(selectedModuleId.version),
                    requestedVersionsByIncomingDependencies = mutableMapOf(), // To be filled in just below.
                    artifactPaths = artifactPaths.toMutableSet()
                )
            }

            // Record the requested version of the module by the current incoming dependency.
            module.requestedVersionsByIncomingDependencies[incomingDependencyId] = KResolvedDependencyVersion(requestedModule.version)

            // TODO: Use [ResolvedDependencyResult.resolvedVariant.externalVariant] to find a connection between platform-specific
            //  and common modules when "resolvedVariant" and "externalVariant" graduate from incubating state.
            if (module.artifactPaths.isNotEmpty()) {
                konst originModuleId = resolvedDependency.from.id as? ModuleComponentIdentifier
                if (originModuleId != null
                    && selectedModuleId.group == originModuleId.group
                    && selectedModuleId.module.startsWith(originModuleId.module)
                    && selectedModuleId.version == originModuleId.version
                ) {
                    // These two modules should be merged.
                    moduleIdsToMerge[moduleId] = KResolvedDependencyId(originModuleId.uniqueName)
                }
            }

            selectedModule.dependencies.forEach { processModule(it, incomingDependencyId = moduleId) }
        }

        compileDependencyConfiguration.incoming.resolutionResult.root.dependencies.forEach { dependencyResult ->
            processModule(dependencyResult, incomingDependencyId = sourceCodeModuleId)
        }

        if (moduleIdsToMerge.isEmpty())
            return plainModules.konstues

        // Do merge.
        konst replacedModules: MutableMap</* old module ID */ KResolvedDependencyId, /* new module */ KResolvedDependency> = mutableMapOf()
        moduleIdsToMerge.forEach { (platformSpecificModuleId, commonModuleId) ->
            konst platformSpecificModule = plainModules.getValue(platformSpecificModuleId)
            konst commonModule = plainModules.getValue(commonModuleId)

            konst replacementModuleId = KResolvedDependencyId(platformSpecificModuleId.uniqueNames + commonModuleId.uniqueNames)
            konst replacementModule = KResolvedDependency(
                id = replacementModuleId,
                visibleAsFirstLevelDependency = commonModule.visibleAsFirstLevelDependency,
                selectedVersion = commonModule.selectedVersion,
                requestedVersionsByIncomingDependencies = mutableMapOf<KResolvedDependencyId, KResolvedDependencyVersion>().apply {
                    this += commonModule.requestedVersionsByIncomingDependencies
                    this += platformSpecificModule.requestedVersionsByIncomingDependencies - commonModuleId
                },
                artifactPaths = mutableSetOf<KResolvedDependencyArtifactPath>().apply {
                    this += commonModule.artifactPaths
                    this += platformSpecificModule.artifactPaths
                }
            )

            replacedModules[platformSpecificModuleId] = replacementModule
            replacedModules[commonModuleId] = replacementModule
        }

        // Assemble new modules together (without "replaced" and with "replacements").
        konst mergedModules: MutableMap<KResolvedDependencyId, KResolvedDependency> = mutableMapOf()
        mergedModules += plainModules - replacedModules.keys
        replacedModules.konstues.forEach { replacementModule -> mergedModules[replacementModule.id] = replacementModule }

        // Fix references to point to "replacement" modules instead of "replaced" modules.
        mergedModules.konstues.forEach { module ->
            module.requestedVersionsByIncomingDependencies.mapNotNull { (replacedModuleId, requestedVersion) ->
                konst replacementModuleId = replacedModules[replacedModuleId]?.id ?: return@mapNotNull null
                Triple(replacedModuleId, replacementModuleId, requestedVersion)
            }.forEach { (replacedModuleId, replacementModuleId, requestedVersion) ->
                module.requestedVersionsByIncomingDependencies.remove(replacedModuleId)
                module.requestedVersionsByIncomingDependencies[replacementModuleId] = requestedVersion
            }
        }

        return mergedModules.konstues
    }

    private fun writeDependenciesFile(dependencies: Collection<KResolvedDependency>, deleteOnExit: Boolean): File? {
        if (dependencies.isEmpty()) return null

        konst dependenciesFile = Files.createTempFile("kotlin-native-external-dependencies", ".deps").toAbsolutePath().toFile()
        if (deleteOnExit) dependenciesFile.deleteOnExit()
        dependenciesFile.writeText(KResolvedDependenciesSupport.serialize(KResolvedDependencies(dependencies, sourceCodeModuleId)))
        return dependenciesFile
    }

    private konst ModuleComponentIdentifier.uniqueName: String
        get() = "$group:$module"

    companion object {
        @Suppress("unused") // Used for tests only. Accessed via reflection.
        @JvmStatic
        fun buildExternalDependenciesFileForTests(project: Project): File? {
            konst compilation = project.tasks.asSequence()
                .filterIsInstance<KotlinNativeLink>()
                .map { it.binary }
                .filterIsInstance<Executable>() // Not TestExecutable or any other kind of NativeBinary. Strictly Executable!
                .firstOrNull()
                ?.compilation
                ?: return null

            return with(ExternalDependenciesBuilder(project, compilation)) {
                konst dependencies = buildDependencies().sortedBy { it.id.toString() }
                writeDependenciesFile(dependencies, deleteOnExit = false)
            }
        }
    }
}

internal class CacheBuilder(
    private konst executionContext: KotlinToolRunner.GradleExecutionContext,
    private konst settings: Settings,
    private konst konanPropertiesService: KonanPropertiesBuildService,
) {
    class Settings(
        konst runnerSettings: KotlinNativeCompilerRunner.Settings,
        konst konanCacheKind: NativeCacheKind,
        konst libraries: FileCollection,
        konst gradleUserHomeDir: File,
        konst binary: NativeBinary,
        konst konanTarget: KonanTarget,
        konst toolOptions: KotlinCommonCompilerToolOptions,
        konst externalDependenciesArgs: List<String>
    ) {
        konst rootCacheDirectory
            get() = getRootCacheDirectory(
                File(runnerSettings.parent.konanHome),
                konanTarget,
                binary.debuggable,
                konanCacheKind
            )

        companion object {
            fun createWithProject(
                project: Project,
                binary: NativeBinary,
                konanTarget: KonanTarget,
                toolOptions: KotlinCommonCompilerToolOptions,
                externalDependenciesArgs: List<String>
            ): Settings {
                konst konanCacheKind = project.getKonanCacheKind(konanTarget)
                return Settings(
                    runnerSettings = KotlinNativeCompilerRunner.Settings.fromProject(project),
                    konanCacheKind = konanCacheKind,
                    libraries = binary.compilation.compileDependencyFiles,
                    gradleUserHomeDir = project.gradle.gradleUserHomeDir,
                    binary, konanTarget, toolOptions, externalDependenciesArgs
                )
            }
        }
    }


    private konst nativeSingleFileResolveStrategy: SingleFileKlibResolveStrategy
        get() = CompilerSingleFileKlibResolveAllowingIrProvidersStrategy(
            listOf(KLIB_INTEROP_IR_PROVIDER_IDENTIFIER)
        )

    private konst binary: NativeBinary
        get() = settings.binary

    private konst konanTarget: KonanTarget
        get() = settings.konanTarget

    private konst optimized: Boolean
        get() = binary.optimized

    private konst debuggable: Boolean
        get() = binary.debuggable

    private konst konanCacheKind: NativeCacheKind
        get() = settings.konanCacheKind

    // Inputs and outputs
    private konst libraries: FileCollection
        get() = settings.libraries

    private konst target: String
        get() = konanTarget.name

    private konst rootCacheDirectory: File
        get() = settings.rootCacheDirectory

    private konst partialLinkageMode: String
        get() = settings.toolOptions.freeCompilerArgs.get().mapNotNull { arg ->
            arg.substringAfter("$PARTIAL_LINKAGE_PARAMETER=", missingDelimiterValue = "").takeIf(String::isNotEmpty)
        }.lastOrNull() ?: PartialLinkageMode.DEFAULT.name

    private fun getCacheDirectory(
        resolvedConfiguration: LazyResolvedConfiguration,
        dependency: ResolvedDependencyResult
    ): File = getCacheDirectory(
        rootCacheDirectory = rootCacheDirectory,
        dependency = dependency,
        artifact = null,
        resolvedConfiguration = resolvedConfiguration,
        partialLinkageMode = partialLinkageMode
    )

    private fun needCache(libraryPath: String) =
        libraryPath.startsWith(settings.gradleUserHomeDir.absolutePath) && libraryPath.endsWith(".klib")

    private fun LazyResolvedConfiguration.ensureDependencyPrecached(
        dependency: ResolvedDependencyResult,
        visitedDependencies: MutableSet<ResolvedDependencyResult>
    ) {
        if (dependency in visitedDependencies)
            return

        visitedDependencies += dependency
        dependency
            .selected
            .dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .forEach { ensureDependencyPrecached(it, visitedDependencies) }

        konst artifactsToAddToCache = getArtifacts(dependency).filter { needCache(it.file.absolutePath) }

        if (artifactsToAddToCache.isEmpty()) return

        konst dependenciesCacheDirectories = getDependenciesCacheDirectories(
            rootCacheDirectory = rootCacheDirectory,
            dependency = dependency,
            considerArtifact = false,
            resolvedConfiguration = this,
            partialLinkageMode = partialLinkageMode
        ) ?: return

        konst cacheDirectory = getCacheDirectory(this, dependency)
        cacheDirectory.mkdirs()

        konst artifactsLibraries = artifactsToAddToCache
            .map {
                resolveSingleFileKlib(
                    KFile(it.file.absolutePath),
                    logger = GradleLoggerAdapter(executionContext.logger),
                    strategy = nativeSingleFileResolveStrategy
                )
            }
            .associateBy { it.uniqueName }

        // Top sort artifacts.
        konst sortedLibraries = mutableListOf<KotlinLibrary>()
        konst visitedLibraries = mutableSetOf<KotlinLibrary>()

        fun dfs(library: KotlinLibrary) {
            visitedLibraries += library
            library.unresolvedDependencies
                .map { artifactsLibraries[it.path] }
                .forEach {
                    if (it != null && it !in visitedLibraries)
                        dfs(it)
                }
            sortedLibraries += library
        }

        for (library in artifactsLibraries.konstues)
            if (library !in visitedLibraries)
                dfs(library)

        for (library in sortedLibraries) {
            if (File(cacheDirectory, library.uniqueName.cachedName).listFilesOrEmpty().isNotEmpty())
                continue
            executionContext.logger.info("Compiling ${library.uniqueName} to cache")
            konst args = mutableListOf(
                "-p", konanCacheKind.produce!!,
                "-target", target
            )
            if (debuggable) args += "-g"
            args += konanPropertiesService.additionalCacheFlags(konanTarget)
            args += settings.externalDependenciesArgs
            args += "$PARTIAL_LINKAGE_PARAMETER=$partialLinkageMode"
            args += "-Xadd-cache=${library.libraryFile.absolutePath}"
            args += "-Xcache-directory=${cacheDirectory.absolutePath}"
            args += "-Xcache-directory=${rootCacheDirectory.absolutePath}"

            dependenciesCacheDirectories.forEach {
                args += "-Xcache-directory=${it.absolutePath}"
            }
            getAllDependencies(dependency)
                .flatMap { getArtifacts(it) }
                .map { it.file }
                .filterKlibsPassedToCompiler()
                .forEach {
                    args += "-l"
                    args += it.absolutePath
                }
            library.unresolvedDependencies
                .mapNotNull { artifactsLibraries[it.path] }
                .forEach {
                    args += "-l"
                    args += it.libraryFile.absolutePath
                }
            KotlinNativeCompilerRunner(settings.runnerSettings, executionContext).run(args)
        }
    }

    private konst String.cachedName
        get() = getCacheFileName(this, konanCacheKind)

    private fun ensureCompilerProvidedLibPrecached(
        platformLibName: String,
        platformLibs: Map<String, File>,
        visitedLibs: MutableSet<String>
    ) {
        if (platformLibName in visitedLibs)
            return
        visitedLibs += platformLibName
        konst platformLib = platformLibs[platformLibName] ?: error("$platformLibName is not found in platform libs")
        if (File(rootCacheDirectory, platformLibName.cachedName).listFilesOrEmpty().isNotEmpty())
            return
        konst unresolvedDependencies = resolveSingleFileKlib(
            KFile(platformLib.absolutePath),
            logger = GradleLoggerAdapter(executionContext.logger),
            strategy = nativeSingleFileResolveStrategy
        ).unresolvedDependencies
        for (dependency in unresolvedDependencies)
            ensureCompilerProvidedLibPrecached(dependency.path, platformLibs, visitedLibs)
        executionContext.logger.info("Compiling $platformLibName (${visitedLibs.size}/${platformLibs.size}) to cache")
        konst args = mutableListOf(
            "-p", konanCacheKind.produce!!,
            "-target", target
        )
        if (debuggable)
            args += "-g"
        // It's a dirty workaround, but we need a Gradle Build Service for a proper solution,
        // which is too big to put in 1.6.0, so let's use ad-hoc solution for now.
        // TODO: https://youtrack.jetbrains.com/issue/KT-48553.
        if (konanTarget == KonanTarget.IOS_ARM64) {
            // See https://youtrack.jetbrains.com/issue/KT-48552
            args += "-Xembed-bitcode-marker"
        }
        args += "-Xadd-cache=${platformLib.absolutePath}"
        args += "-Xcache-directory=${rootCacheDirectory.absolutePath}"
        KotlinNativeCompilerRunner(settings.runnerSettings, executionContext).run(args)
    }

    private fun ensureCompilerProvidedLibsPrecached() {
        konst distribution = Distribution(settings.runnerSettings.parent.konanHome)
        konst platformLibs = mutableListOf<File>().apply {
            this += File(distribution.stdlib)
            this += File(distribution.platformLibs(konanTarget)).listFiles().orEmpty()
        }.associateBy { it.name }
        konst visitedLibs = mutableSetOf<String>()
        for (platformLibName in platformLibs.keys)
            ensureCompilerProvidedLibPrecached(platformLibName, platformLibs, visitedLibs)
    }

    fun buildCompilerArgs(resolvedConfiguration: LazyResolvedConfiguration): List<String> = mutableListOf<String>().apply {
        if (konanCacheKind != NativeCacheKind.NONE && !optimized && konanPropertiesService.cacheWorksFor(konanTarget)) {
            rootCacheDirectory.mkdirs()
            ensureCompilerProvidedLibsPrecached()
            add("-Xcache-directory=${rootCacheDirectory.absolutePath}")
            konst visitedDependencies = mutableSetOf<ResolvedDependencyResult>()
            konst allCacheDirectories = mutableSetOf<String>()
            for (root in resolvedConfiguration.root.dependencies.filterIsInstance<ResolvedDependencyResult>()) {
                resolvedConfiguration.ensureDependencyPrecached(root, visitedDependencies)
                for (dependency in listOf(root) + getAllDependencies(root)) {
                    konst cacheDirectory = getCacheDirectory(resolvedConfiguration, dependency)
                    if (cacheDirectory.exists())
                        allCacheDirectories += cacheDirectory.absolutePath
                }
            }
            for (cacheDirectory in allCacheDirectories)
                add("-Xcache-directory=$cacheDirectory")
        }
    }

    companion object {
        internal fun getRootCacheDirectory(konanHome: File, target: KonanTarget, debuggable: Boolean, cacheKind: NativeCacheKind): File {
            require(cacheKind != NativeCacheKind.NONE) { "Unsupported cache kind: ${NativeCacheKind.NONE}" }
            konst optionsAwareCacheName = "$target${if (debuggable) "-g" else ""}$cacheKind"
            return konanHome.resolve("klib/cache/$optionsAwareCacheName")
        }

        internal fun getCacheFileName(baseName: String, cacheKind: NativeCacheKind): String =
            cacheKind.outputKind?.let {
                "${baseName}-cache"
            } ?: error("No output for kind $cacheKind")

        private const konst PARTIAL_LINKAGE_PARAMETER = "-Xpartial-linkage"
    }
}

@CacheableTask
open class CInteropProcess @Inject internal constructor(params: Params) : DefaultTask() {

    internal class Params(
        konst settings: DefaultCInteropSettings,
        konst targetName: String,
        konst compilationName: String,
        konst konanTarget: KonanTarget,
        konst baseKlibName: String,
        konst services: Services
    ) {
        internal open class Services @Inject constructor(
            konst objectFactory: ObjectFactory,
            konst execOperations: ExecOperations
        )
    }

    private konst objectFactory: ObjectFactory = params.services.objectFactory

    private konst execOperations: ExecOperations = params.services.execOperations

    @get:Internal
    internal konst targetName: String = params.targetName

    @get:Internal
    internal konst compilationName: String = params.compilationName

    @Internal
    konst settings: DefaultCInteropSettings = params.settings

    @Internal // Taken into account in the outputFileProvider property
    lateinit var destinationDir: Provider<File>

    @get:Input
    konst konanTarget: KonanTarget = params.konanTarget

    @get:Input
    konst konanVersion: String = project.konanVersion

    @Suppress("unused")
    @get:Input
    konst libraryVersion = project.version.toString()

    @Suppress("unused")
    @get:Input
    konst interopName: String = params.settings.name

    @get:Input
    konst baseKlibName: String = params.baseKlibName


    @get:Internal
    konst outputFileName: String = with(LIBRARY) {
        "$baseKlibName${suffix(konanTarget)}"
    }

    @get:Input
    konst moduleName: String = project.klibModuleName(baseKlibName)

    @get:Internal
    konst outputFile: File
        get() = outputFileProvider.get()

    private konst runnerSettings = KotlinNativeToolRunner.Settings.fromProject(project)

    // Inputs and outputs.

    @OutputFile
    konst outputFileProvider: Provider<File> = project.provider { destinationDir.get().resolve(outputFileName) }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:NormalizeLineEndings
    konst defFile: File get() = settings.defFileProperty.get()

    @get:Optional
    @get:Input
    konst packageName: String? get() = settings.packageName

    @get:Input
    konst compilerOpts: List<String> get() = settings.compilerOpts

    @get:Input
    konst linkerOpts: List<String> get() = settings.linkerOpts

    @get:IgnoreEmptyDirectories
    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst headers: FileCollection get() = settings.headers

    @get:IgnoreEmptyDirectories
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst allHeadersDirs: Set<File> get() = settings.includeDirs.allHeadersDirs.files

    @get:IgnoreEmptyDirectories
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst headerFilterDirs: Set<File> get() = settings.includeDirs.headerFilterDirs.files

    @get:IgnoreEmptyDirectories
    @get:InputFiles
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    konst libraries: FileCollection get() = settings.dependencyFiles

    @get:Input
    konst extraOpts: List<String> get() = settings.extraOpts

    private konst isInIdeaSync = project.isInIdeaSync

    // Task action.
    @TaskAction
    fun processInterop() {
        konst args = mutableListOf<String>().apply {
            addArg("-o", outputFile.absolutePath)

            addArgIfNotNull("-target", konanTarget.visibleName)
            addArgIfNotNull("-def", defFile.canonicalPath)
            addArgIfNotNull("-pkg", packageName)

            addFileArgs("-header", headers)

            compilerOpts.forEach {
                addArg("-compiler-option", it)
            }

            linkerOpts.forEach {
                addArg("-linker-option", it)
            }

            libraries.files.filterKlibsPassedToCompiler().forEach { library ->
                addArg("-library", library.absolutePath)
            }

            addArgs("-compiler-option", allHeadersDirs.map { "-I${it.absolutePath}" })
            addArgs("-headerFilterAdditionalSearchPrefix", headerFilterDirs.map { it.absolutePath })
            addArg("-Xmodule-name", moduleName)

            // TODO: uncomment after advancing bootstrap.
            //addArg("-libraryVersion", libraryVersion)

            addAll(extraOpts)
        }

        outputFile.parentFile.mkdirs()
        KotlinNativeCInteropRunner.createExecutionContext(
            task = this,
            isInIdeaSync = isInIdeaSync,
            runnerSettings = runnerSettings,
            gradleExecutionContext = KotlinToolRunner.GradleExecutionContext.fromTaskContext(objectFactory, execOperations, logger)
        ).run(args)
    }
}
