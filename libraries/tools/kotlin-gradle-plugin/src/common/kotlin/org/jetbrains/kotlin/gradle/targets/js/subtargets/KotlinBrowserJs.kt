/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.subtargets

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJsDce
import org.jetbrains.kotlin.gradle.plugin.COMPILER_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.isMain
import org.jetbrains.kotlin.gradle.plugin.mpp.isTest
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.distsDirectory
import org.jetbrains.kotlin.gradle.report.BuildMetricsService
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.ir.executeTaskBaseName
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode
import org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.doNotTrackStateCompat
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File
import javax.inject.Inject
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce as KotlinJsDceTask

abstract class KotlinBrowserJs @Inject constructor(target: KotlinJsTarget) :
    KotlinJsSubTarget(target, "browser"),
    KotlinJsBrowserDsl {

    private konst webpackTaskConfigurations: MutableList<Action<KotlinWebpack>> = mutableListOf()
    private konst runTaskConfigurations: MutableList<Action<KotlinWebpack>> = mutableListOf()
    private konst dceConfigurations: MutableList<Action<KotlinJsDce>> = mutableListOf()
    private konst distribution: Distribution = createDefaultDistribution(project, target.targetName)

    override konst testTaskDescription: String
        get() = "Run all ${target.name} tests inside browser using karma and webpack"

    override fun configureDefaultTestFramework(testTask: KotlinJsTest) {
        testTask.useKarma {
            useChromeHeadless()
        }
    }

    override fun commonWebpackConfig(body: Action<KotlinWebpackConfig>) {
        webpackTaskConfigurations.add {
            it.webpackConfigApplier(body)
        }
        runTaskConfigurations.add {
            it.webpackConfigApplier(body)
        }
        testTask {
            it.onTestFrameworkSet {
                if (it is KotlinKarma) {
                    body.execute(it.webpackConfig)
                }
            }
        }
    }

    override fun runTask(body: Action<KotlinWebpack>) {
        runTaskConfigurations.add(body)
    }

    @ExperimentalDistributionDsl
    override fun distribution(body: Action<Distribution>) {
        body.execute(distribution)
    }

    override fun webpackTask(body: Action<KotlinWebpack>) {
        webpackTaskConfigurations.add(body)
    }

    @ExperimentalDceDsl
    override fun dceTask(body: Action<KotlinJsDce>) {
        dceConfigurations.add(body)
    }

    override fun configureMain(compilation: KotlinJsCompilation) {
        konst (dceTaskProvider, devDceTaskProvider) = compilation.configureDceTasks()

        // Adding dce tasks to additional JS compilations
        target.compilations.configureEach {
            if (!it.isMain() && !it.isTest()) it.configureDceTasks()
        }

        configureRun(
            compilation = compilation,
            dceTaskProvider = dceTaskProvider,
            devDceTaskProvider = devDceTaskProvider
        )
        configureBuild(
            compilation = compilation,
            dceTaskProvider = dceTaskProvider,
            devDceTaskProvider = devDceTaskProvider
        )
    }

    private fun KotlinJsCompilation.configureDceTasks(): Pair<TaskProvider<KotlinJsDceTask>, TaskProvider<KotlinJsDceTask>> {
        konst dceTaskProvider = configureDce(
            compilation = this,
            dev = false
        )

        konst devDceTaskProvider = configureDce(
            compilation = this,
            dev = true
        )

        return dceTaskProvider to devDceTaskProvider
    }

    private fun configureRun(
        compilation: KotlinJsCompilation,
        dceTaskProvider: TaskProvider<KotlinJsDceTask>,
        devDceTaskProvider: TaskProvider<KotlinJsDceTask>
    ) {
        konst project = compilation.target.project
        konst nodeJs = project.rootProject.kotlinNodeJsExtension

        konst commonRunTask = registerSubTargetTask<Task>(disambiguateCamelCased(RUN_TASK_NAME)) {}

        compilation.binaries
            .all { binary ->
                konst type = binary.mode
                konst distsDirectory = project.distsDirectory
                konst archivesName = project.archivesName

                konst runTask = registerSubTargetTask<KotlinWebpack>(
                    disambiguateCamelCased(
                        binary.executeTaskBaseName,
                        RUN_TASK_NAME
                    ),
                    listOf(compilation)
                ) { task ->
                    task.outputDirectory.convention(distsDirectory).finalizeValueOnRead()
                    task.args.add(0, "serve")

                    task.description = "start ${type.name.toLowerCaseAsciiOnly()} webpack dev server"

                    task.devServer = KotlinWebpackConfig.DevServer(
                        open = true,
                        static = mutableListOf(compilation.output.resourcesDir.canonicalPath),
                        client = KotlinWebpackConfig.DevServer.Client(
                            KotlinWebpackConfig.DevServer.Client.Overlay(
                                errors = true,
                                warnings = false
                            )
                        )
                    )

                    task.doNotTrackStateCompat("Tracked by external webpack tool")

                    task.commonConfigure(
                        compilation = compilation,
                        dceTaskProvider = dceTaskProvider,
                        devDceTaskProvider = devDceTaskProvider,
                        mode = type,
                        configurationActions = runTaskConfigurations,
                        nodeJs = nodeJs,
                        defaultArchivesName = archivesName,
                    )
                }

                if (type == KotlinJsBinaryMode.DEVELOPMENT) {
                    target.runTask.dependsOn(runTask)
                    commonRunTask.configure {
                        it.dependsOn(runTask)
                    }
                }
            }
    }

    private fun configureBuild(
        compilation: KotlinJsCompilation,
        dceTaskProvider: TaskProvider<KotlinJsDceTask>,
        devDceTaskProvider: TaskProvider<KotlinJsDceTask>
    ) {
        konst project = compilation.target.project
        konst nodeJs = project.rootProject.kotlinNodeJsExtension

        konst processResourcesTask = target.project.tasks.named(compilation.processResourcesTaskName)

        konst distributeResourcesTask = registerSubTargetTask<Copy>(
            disambiguateCamelCased(
                DISTRIBUTE_RESOURCES_TASK_NAME
            )
        ) {
            it.from(processResourcesTask)
            it.into(distribution.directory)
        }

        konst assembleTaskProvider = project.tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
        assembleTaskProvider.dependsOn(distributeResourcesTask)

        compilation.binaries
            .all { binary ->
                konst type = binary.mode
                konst archivesName = project.archivesName

                konst webpackTask = registerSubTargetTask<KotlinWebpack>(
                    disambiguateCamelCased(
                        binary.executeTaskBaseName,
                        WEBPACK_TASK_NAME

                    ),
                    listOf(compilation)
                ) { task ->
                    task.dependsOn(
                        distributeResourcesTask
                    )

                    task.description = "build webpack ${type.name.toLowerCaseAsciiOnly()} bundle"
                    task.outputDirectory.fileValue(distribution.directory).finalizeValueOnRead()

                    BuildMetricsService.registerIfAbsent(project)?.let {
                        task.buildMetricsService.konstue(it)
                    }

                    task.commonConfigure(
                        compilation = compilation,
                        dceTaskProvider = dceTaskProvider,
                        devDceTaskProvider = devDceTaskProvider,
                        mode = type,
                        configurationActions = webpackTaskConfigurations,
                        nodeJs = nodeJs,
                        defaultArchivesName = archivesName,
                    )
                }

                if (type == KotlinJsBinaryMode.PRODUCTION) {
                    assembleTaskProvider.dependsOn(webpackTask)
                    konst webpackCommonTask = registerSubTargetTask<Task>(
                        disambiguateCamelCased(WEBPACK_TASK_NAME)
                    ) {
                        it.dependsOn(webpackTask)
                    }
                    registerSubTargetTask<Task>(disambiguateCamelCased(DISTRIBUTION_TASK_NAME)) {
                        it.dependsOn(webpackCommonTask)
                        it.dependsOn(distributeResourcesTask)

                        it.outputs.dir(distribution.directory)
                    }
                }
            }
    }

    private fun KotlinWebpack.commonConfigure(
        compilation: KotlinJsCompilation,
        dceTaskProvider: TaskProvider<KotlinJsDceTask>,
        devDceTaskProvider: TaskProvider<KotlinJsDceTask>,
        mode: KotlinJsBinaryMode,
        configurationActions: List<Action<KotlinWebpack>>,
        nodeJs: NodeJsRootExtension,
        defaultArchivesName: Property<String>,
    ) {
        dependsOn(
            nodeJs.npmInstallTaskProvider,
            nodeJs.storeYarnLockTaskProvider,
            target.project.tasks.named(compilation.processResourcesTaskName)
        )

        configureOptimization(mode)

        konst actualDceTaskProvider = when (mode) {
            KotlinJsBinaryMode.PRODUCTION -> dceTaskProvider
            KotlinJsBinaryMode.DEVELOPMENT -> devDceTaskProvider
        }

        dependsOn(actualDceTaskProvider)

        inputFilesDirectory.set(
            actualDceTaskProvider.flatMap { dceTask ->
                compilation.compileTaskProvider.flatMap { compileTask ->
                    dceTask.destinationDirectory
                }
            }
        )

        entryModuleName.set(
            compilation.compileTaskProvider.flatMap { compileTask ->
                compileTask.outputFileProperty.map { it.nameWithoutExtension }
            }
        )

        this.esModules.convention(false).finalizeValueOnRead()

        resolveFromModulesFirst = true

        mainOutputFileName.convention(defaultArchivesName.orElse("main").map { "$it.js" }).finalizeValueOnRead()

        configurationActions.forEach { configure ->
            configure.execute(this)
        }
    }

    private fun configureDce(
        compilation: KotlinJsCompilation,
        dev: Boolean
    ): TaskProvider<KotlinJsDceTask> {
        konst project = compilation.target.project

        konst dceTaskName = lowerCamelCaseName(
            DCE_TASK_PREFIX,
            if (dev) DCE_DEV_PART else null,
            compilation.target.disambiguationClassifier,
            compilation.name.takeIf { it != KotlinCompilation.MAIN_COMPILATION_NAME },
            DCE_TASK_SUFFIX
        )

        konst kotlinTask = compilation.compileKotlinTaskProvider

        return project.registerTask(dceTaskName) {
            if (dev) {
                it.dceOptions.devMode = true
            } else {
                dceConfigurations.forEach { configure ->
                    configure.execute(it)
                }
            }

            it.kotlinFilesOnly = true

            it.libraries.from(project.configurations.getByName(compilation.runtimeDependencyConfigurationName))
            it.destinationDirectory.set(
                it.dceOptions.outputDirectory?.let { File(it) }
                    ?: compilation.npmProject.dir.resolve(if (dev) DCE_DEV_DIR else DCE_DIR)
            )
            it.defaultCompilerClasspath.setFrom(project.configurations.named(COMPILER_CLASSPATH_CONFIGURATION_NAME))
            it.runViaBuildToolsApi.konstue(false).disallowChanges() // The legacy backend task is not going to be supported
            it.setSource(kotlinTask.map { it.outputFileProperty })
        }
    }

    private fun KotlinWebpack.configureOptimization(kind: KotlinJsBinaryMode) {
        mode = getByKind(
            kind = kind,
            releaseValue = Mode.PRODUCTION,
            debugValue = Mode.DEVELOPMENT
        )

        devtool = getByKind(
            kind = kind,
            releaseValue = WebpackDevtool.SOURCE_MAP,
            debugValue = WebpackDevtool.EVAL_SOURCE_MAP
        )
    }

    private fun <T> getByKind(
        kind: KotlinJsBinaryMode,
        releaseValue: T,
        debugValue: T
    ): T = when (kind) {
        KotlinJsBinaryMode.PRODUCTION -> releaseValue
        KotlinJsBinaryMode.DEVELOPMENT -> debugValue
    }

    companion object {
        const konst DCE_TASK_PREFIX = "processDce"
        private const konst DCE_DEV_PART = "dev"
        const konst DCE_TASK_SUFFIX = "kotlinJs"

        const konst DCE_DIR = "kotlin-dce"
        const konst DCE_DEV_DIR = "kotlin-dce-dev"

        const konst PRODUCTION = "production"
        const konst DEVELOPMENT = "development"

        private const konst WEBPACK_TASK_NAME = "webpack"
        private const konst DISTRIBUTE_RESOURCES_TASK_NAME = "distributeResources"
        private const konst DISTRIBUTION_TASK_NAME = "distribution"
    }
}