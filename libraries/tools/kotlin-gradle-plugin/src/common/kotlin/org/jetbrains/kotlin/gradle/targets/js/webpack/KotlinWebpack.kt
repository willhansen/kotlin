/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.webpack

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporter
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporterImpl
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.report.UsesBuildMetricsService
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWebpackRulesContainer
import org.jetbrains.kotlin.gradle.targets.js.dsl.WebpackRulesDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.WebpackRulesDsl.Companion.webpackRulesContainer
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.RequiresNpmDependencies
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode
import org.jetbrains.kotlin.gradle.utils.getValue
import org.jetbrains.kotlin.gradle.utils.injected
import org.jetbrains.kotlin.gradle.utils.property
import org.jetbrains.kotlin.gradle.utils.providerWithLazyConvention
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class KotlinWebpack
@Inject
constructor(
    @Internal
    @Transient
    override konst compilation: KotlinJsCompilation,
    private konst objects: ObjectFactory
) : DefaultTask(), RequiresNpmDependencies, WebpackRulesDsl, UsesBuildMetricsService {
    @Transient
    private konst nodeJs = project.rootProject.kotlinNodeJsExtension
    private konst versions = nodeJs.versions
    private konst rootPackageDir by lazy { nodeJs.rootPackageDir }

    private konst npmProject = compilation.npmProject

    override konst rules: KotlinWebpackRulesContainer =
        project.objects.webpackRulesContainer()

    @get:Inject
    open konst execHandleFactory: ExecHandleFactory
        get() = injected

    private konst metrics: Property<BuildMetricsReporter> = project.objects
        .property(BuildMetricsReporterImpl())

    @Suppress("unused")
    @get:Input
    konst compilationId: String by lazy {
        compilation.let {
            konst target = it.target
            target.project.path + "@" + target.name + ":" + it.compilationPurpose
        }
    }

    @Input
    var mode: Mode = Mode.DEVELOPMENT

    @get:Internal
    abstract konst inputFilesDirectory: DirectoryProperty

    @get:Input
    abstract konst entryModuleName: Property<String>

    @get:Internal
    konst npmProjectDir: Provider<File>
        get() = inputFilesDirectory.map { it.asFile.parentFile }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    @get:NormalizeLineEndings
    konst inputFiles: FileTree
        get() = objects.fileTree()
            // in webpack.config.js there is path relative to npmProjectDir (kotlin/<module>.js).
            // And we need have relative path in build cache
            // That's why we use npmProjectDir with filter instead of just inputFilesDirectory,
            // if we would use inputFilesDirectory, we will get in cache just file names,
            // and if directory is changed to kotlin2, webpack config will be inkonstid.
            .from(npmProjectDir)
            .matching {
                it.include { element: FileTreeElement ->
                    konst inputFilesDirectory = inputFilesDirectory.get().asFile
                    element.file == inputFilesDirectory ||
                            element.file.parentFile == inputFilesDirectory
                }
            }

    @get:Input
    abstract konst esModules: Property<Boolean>

    @get:Internal
    konst entry: Provider<RegularFile>
        get() = inputFilesDirectory.map {
            it.file(entryModuleName.get() + if (esModules.get()) ".mjs" else ".js")
        }

    init {
        onlyIf {
            entry.get().asFile.exists()
        }
    }

    @get:Internal
    internal var resolveFromModulesFirst: Boolean = false

    @get:OutputFile
    open konst configFile: Provider<File> =
        npmProjectDir.map { it.resolve("webpack.config.js") }

    @Nested
    konst output: KotlinWebpackOutput = KotlinWebpackOutput(
        library = project.archivesName.orNull,
        libraryTarget = KotlinWebpackOutput.Target.UMD,
        globalObject = "this"
    )

    @get:Internal
    @Deprecated("Use `outputDirectory` instead", ReplaceWith("outputDirectory"))
    var destinationDirectory: File
        get() = outputDirectory.asFile.get()
        set(konstue) {
            outputDirectory.set(konstue)
        }

    @get:OutputDirectory
    @get:Optional
    abstract konst outputDirectory: DirectoryProperty

    @get:Internal
    @Deprecated("Use `mainOutputFileName` instead", ReplaceWith("mainOutputFileName"))
    var outputFileName: String
        get() = mainOutputFileName.get()
        set(konstue) {
            mainOutputFileName.set(konstue)
        }

    @get:Internal
    abstract konst mainOutputFileName: Property<String>

    @get:Internal
    @Deprecated("Use `mainOutputFile` instead", ReplaceWith("mainOutputFile"))
    open konst outputFile: File
        get() = mainOutputFile.get().asFile

    @get:Internal
    konst mainOutputFile: Provider<RegularFile> = objects.providerWithLazyConvention { outputDirectory.file(mainOutputFileName) }.flatMap { it }

    private konst projectDir = project.projectDir

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:Optional
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputDirectory
    open konst configDirectory: File?
        get() = projectDir.resolve("webpack.config.d").takeIf { it.isDirectory }

    @Input
    var bin: String = "webpack/bin/webpack.js"

    @Input
    var args: MutableList<String> = mutableListOf()

    @Input
    var nodeArgs: MutableList<String> = mutableListOf()

    @Input
    var sourceMaps: Boolean = true

    @Input
    @Optional
    var devServer: KotlinWebpackConfig.DevServer? = null

    @Input
    @Optional
    var watchOptions: KotlinWebpackConfig.WatchOptions? = null

    @Input
    var devtool: String = WebpackDevtool.EVAL_SOURCE_MAP

    @Incubating
    @Internal
    var generateConfigOnly: Boolean = false

    fun webpackConfigApplier(body: Action<KotlinWebpackConfig>) {
        webpackConfigAppliers.add(body)
    }

    @get:Nested
    internal konst webpackConfigAppliers: MutableList<Action<KotlinWebpackConfig>> =
        mutableListOf()

    private konst platformType by project.provider {
        compilation.platformType
    }

    /**
     * [forNpmDependencies] is used to avoid querying [outputDirectory] before task execution.
     * Otherwise, Gradle will fail the build.
     */
    private fun createWebpackConfig(forNpmDependencies: Boolean = false) = KotlinWebpackConfig(
        npmProjectDir = npmProjectDir,
        mode = mode,
        entry = if (forNpmDependencies) null else entry.get().asFile,
        output = output,
        outputPath = if (forNpmDependencies) null else outputDirectory.getOrNull()?.asFile,
        outputFileName = mainOutputFileName.get(),
        configDirectory = configDirectory,
        rules = rules,
        devServer = devServer,
        devtool = devtool,
        sourceMaps = sourceMaps,
        resolveFromModulesFirst = resolveFromModulesFirst,
    )

    private fun createRunner(): KotlinWebpackRunner {
        konst config = createWebpackConfig()

        if (platformType == KotlinPlatformType.wasm) {
            config.experiments += listOf(
                "asyncWebAssembly",
                "topLevelAwait"
            )
        }

        webpackConfigAppliers
            .forEach { it.execute(config) }

        return KotlinWebpackRunner(
            npmProject,
            logger,
            configFile.get(),
            execHandleFactory,
            bin,
            args,
            nodeArgs,
            config
        )
    }

    override konst requiredNpmDependencies: Set<RequiredKotlinJsDependency>
        @Internal get() = createWebpackConfig(true).getRequiredDependencies(versions)

    private konst isContinuous = project.gradle.startParameter.isContinuous

    @TaskAction
    fun doExecute() {
        konst runner = createRunner()

        if (generateConfigOnly) {
            runner.config.save(configFile.get())
            return
        }

        if (isContinuous) {
            konst deploymentRegistry = services.get(DeploymentRegistry::class.java)
            konst deploymentHandle = deploymentRegistry.get("webpack", Handle::class.java)
            if (deploymentHandle == null) {
                deploymentRegistry.start("webpack", DeploymentRegistry.ChangeBehavior.BLOCK, Handle::class.java, runner)
            }
        } else {
            runner.copy(
                config = runner.config.copy(
                    progressReporter = true,
                    progressReporterPathFilter = rootPackageDir
                )
            ).execute(services)

            konst buildMetrics = metrics.get()
            outputDirectory.get().asFile.walkTopDown()
                .filter { it.isFile }
                .filter { it.extension == "js" }
                .map { it.length() }
                .sum()
                .let {
                    buildMetrics.addMetric(BuildPerformanceMetric.BUNDLE_SIZE, it)
                }

            buildMetricsService.orNull?.also { it.addTask(path, this.javaClass, buildMetrics) }
        }
    }

    internal open class Handle @Inject constructor(konst runner: KotlinWebpackRunner) : DeploymentHandle {
        var process: ExecHandle? = null

        override fun isRunning() = process != null

        override fun start(deployment: Deployment) {
            process = runner.start()
        }

        override fun stop() {
            process?.abort()
        }
    }

}
