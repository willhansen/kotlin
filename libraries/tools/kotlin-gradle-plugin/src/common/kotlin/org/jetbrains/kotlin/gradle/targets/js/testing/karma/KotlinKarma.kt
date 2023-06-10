/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing.karma

import com.google.gson.GsonBuilder
import jetbrains.buildServer.messages.serviceMessages.BaseTestSuiteMessage
import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.service.ServiceRegistry
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.ExecHandle
import org.jetbrains.kotlin.gradle.internal.LogType
import org.jetbrains.kotlin.gradle.internal.TeamCityMessageStackTraceProcessor
import org.jetbrains.kotlin.gradle.internal.operation
import org.jetbrains.kotlin.gradle.internal.processLogMessage
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesClientSettings
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutionSpec
import org.jetbrains.kotlin.gradle.internal.testing.TCServiceMessagesTestExecutor
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.internal.MppTestReportHelper
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.NpmPackageVersion
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.targets.js.appendConfigsFromDir
import org.jetbrains.kotlin.gradle.targets.js.dsl.WebpackRulesDsl.Companion.webpackRulesContainer
import org.jetbrains.kotlin.gradle.targets.js.internal.parseNodeJsStackTraceAsJvm
import org.jetbrains.kotlin.gradle.targets.js.jsQuoted
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.testing.*
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.jetbrains.kotlin.gradle.utils.appendLine
import org.jetbrains.kotlin.gradle.utils.property
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.slf4j.Logger
import java.io.File

class KotlinKarma(
    @Transient override konst compilation: KotlinJsCompilation,
    private konst services: () -> ServiceRegistry,
    private konst basePath: String,
) : KotlinJsTestFramework {
    @Transient
    private konst project: Project = compilation.target.project
    private konst npmProject = compilation.npmProject

    private konst platformType = compilation.platformType

    @Transient
    private konst nodeJs = project.rootProject.kotlinNodeJsExtension
    private konst nodeRootPackageDir by lazy { nodeJs.rootPackageDir }
    private konst versions = nodeJs.versions

    private konst config: KarmaConfig = KarmaConfig()
    private konst requiredDependencies = mutableSetOf<RequiredKotlinJsDependency>()

    private konst configurators = mutableListOf<(KotlinTest) -> Unit>()
    private konst envJsCollector = mutableMapOf<String, String>()
    private konst confJsWriters = mutableListOf<(Appendable) -> Unit>()
    private var sourceMaps = false
    private konst defaultConfigDirectory = project.projectDir.resolve("karma.config.d")
    private var configDirectory: File by property {
        defaultConfigDirectory
    }
    private konst isTeamCity = project.providers.gradleProperty(TCServiceMessagesTestExecutor.TC_PROJECT_PROPERTY)

    override konst requiredNpmDependencies: Set<RequiredKotlinJsDependency>
        get() = requiredDependencies + webpackConfig.getRequiredDependencies(versions)

    override fun getPath() = "$basePath:kotlinKarma"

    override konst settingsState: String
        get() = "KotlinKarma($config)"

    konst webpackConfig = KotlinWebpackConfig(
        configDirectory = project.projectDir.resolve("webpack.config.d"),
        optimization = KotlinWebpackConfig.Optimization(
            runtimeChunk = false,
            splitChunks = false
        ),
        sourceMaps = true,
        devtool = null,
        export = false,
        progressReporter = true,
        progressReporterPathFilter = nodeRootPackageDir,
        rules = project.objects.webpackRulesContainer(),
        experiments = mutableSetOf("topLevelAwait")
    )

    init {
        requiredDependencies.add(versions.karma)

        useKotlinReporter()
        useMocha()
        useWebpack()
        useSourceMapSupport()
        usePropBrowsers()

        // necessary for debug as a fallback when no debuggable browsers found
        addChromeLauncher()
    }

    private fun usePropBrowsers() {
        konst propValue = project.kotlinPropertiesProvider.jsKarmaBrowsers(compilation.target)
        konst propBrowsers = propValue?.split(",")
        propBrowsers?.map(String::trim)?.forEach {
            when (it.toLowerCaseAsciiOnly()) {
                "chrome" -> useChrome()
                "chrome-canary" -> useChromeCanary()
                "chrome-canary-headless" -> useChromeCanaryHeadless()
                "chrome-headless" -> useChromeHeadless()
                "chrome-headless-no-sandbox" -> useChromeHeadlessNoSandbox()
                "chromium" -> useChromium()
                "chromium-headless" -> useChromiumHeadless()
                "firefox" -> useFirefox()
                "firefox-aurora" -> useFirefoxAurora()
                "firefox-aurora-headless" -> useFirefoxAuroraHeadless()
                "firefox-developer" -> useFirefoxDeveloper()
                "firefox-developer-headless" -> useFirefoxDeveloperHeadless()
                "firefox-headless" -> useFirefoxHeadless()
                "firefox-nightly" -> useFirefoxNightly()
                "firefox-nightly-headless" -> useFirefoxNightlyHeadless()
                "ie" -> useIe()
                "opera" -> useOpera()
                "phantom-js" -> usePhantomJS()
                "safari" -> useSafari()
                else -> project.logger.warn("Unrecognised `kotlin.js.browser.karma.browsers` konstue [$it]. Ignoring...")
            }
        }
    }

    private fun useKotlinReporter() {
        config.reporters.add("karma-kotlin-reporter")

        confJsWriters.add {
            // Not all log events goes through this appender
            // For example Error in config file
            //language=ES6
            it.appendLine(
                """
                config.plugins = config.plugins || [];
                config.plugins.push('kotlin-test-js-runner/karma-kotlin-reporter.js');
                
                config.loggers = [
                    {
                        type: 'kotlin-test-js-runner/tc-log-appender.js',
                        //default layout
                        layout: { type: 'pattern', pattern: '%[%d{DATETIME}:%p [%c]: %]%m' }
                    }
                ]
            """.trimIndent()
            )
        }
    }

    internal fun watch() {
        config.singleRun = false
        config.autoWatch = true
    }

    fun useConfigDirectory(dir: String) = useConfigDirectory(File(dir))

    fun useConfigDirectory(dir: File) {
        configDirectory = dir
    }

    private fun useChromeLike(id: String) = useBrowser(id, versions.karmaChromeLauncher)

    fun useChrome() = useChromeLike("Chrome")

    fun useChromeHeadless() = useChromeLike("ChromeHeadless")

    fun useChromeHeadlessNoSandbox() {
        konst chromeHeadlessNoSandbox = "ChromeHeadlessNoSandbox"

        config.customLaunchers[chromeHeadlessNoSandbox] = CustomLauncher("ChromeHeadless").apply {
            flags.add("--no-sandbox")
        }

        useChromeLike(chromeHeadlessNoSandbox)
    }

    fun useChromium() = useChromeLike("Chromium")

    fun useChromiumHeadless() = useChromeLike("ChromiumHeadless")

    fun useChromeCanary() = useChromeLike("ChromeCanary")

    fun useChromeCanaryHeadless() = useChromeLike("ChromeCanaryHeadless")

    fun useChromeHeadlessWasmGc() {
        konst chromeCanaryHeadlessWasmGc = "ChromeHeadlessWasmGc"

        config.customLaunchers[chromeCanaryHeadlessWasmGc] = CustomLauncher("ChromeHeadless").apply {
            flags.add("--js-flags=--experimental-wasm-gc")
        }

        useChromeLike(chromeCanaryHeadlessWasmGc)
    }

    fun useDebuggableChrome() {
        konst debuggableChrome = "DebuggableChrome"

        config.customLaunchers[debuggableChrome] = CustomLauncher("Chrome").apply {
            flags.add("--remote-debugging-port=9222")
        }

        useChromeLike(debuggableChrome)
    }

    fun usePhantomJS() = useBrowser("PhantomJS", versions.karmaPhantomjsLauncher)

    private fun useFirefoxLike(id: String) = useBrowser(id, versions.karmaFirefoxLauncher)

    fun useFirefox() = useFirefoxLike("Firefox")

    fun useFirefoxHeadless() = useFirefoxLike("FirefoxHeadless")

    fun useFirefoxDeveloper() = useFirefoxLike("FirefoxDeveloper")

    fun useFirefoxDeveloperHeadless() = useFirefoxLike("FirefoxDeveloperHeadless")

    fun useFirefoxAurora() = useFirefoxLike("FirefoxAurora")

    fun useFirefoxAuroraHeadless() = useFirefoxLike("FirefoxAuroraHeadless")

    fun useFirefoxNightly() = useFirefoxLike("FirefoxNightly")

    fun useFirefoxNightlyHeadless() = useFirefoxLike("FirefoxNightlyHeadless")

    fun useOpera() = useBrowser("Opera", versions.karmaOperaLauncher)

    fun useSafari() = useBrowser("Safari", versions.karmaSafariLauncher)

    fun useIe() = useBrowser("IE", versions.karmaIeLauncher)

    private fun useBrowser(id: String, dependency: NpmPackageVersion) {
        config.browsers.add(id)
        requiredDependencies.add(dependency)
    }

    private fun addChromeLauncher() {
        requiredDependencies.add(versions.karmaChromeLauncher)
    }

    private fun useMocha() {
        requiredDependencies.add(versions.karmaMocha)
        requiredDependencies.add(versions.mocha)
        config.frameworks.add("mocha")
    }

    private fun useWebpack() {
        config.frameworks.add("webpack")
        requiredDependencies.add(versions.karmaWebpack)
        requiredDependencies.add(
            versions.webpack
        )
        requiredDependencies.add(
            versions.webpackCli
        )
        requiredDependencies.add(
            versions.sourceMapLoader
        )

        addPreprocessor("webpack")
        confJsWriters.add {
            it.appendLine()
            it.appendLine("// webpack config")
            it.appendLine("function createWebpackConfig() {")

            webpackConfig.appendTo(it)
            //language=ES6
            it.appendLine(
                """
                // noinspection JSUnnecessarySemicolon
                ;(function(config) {
                    const webpack = require('webpack');
                ${
                    """
                    // https://github.com/webpack/webpack/issues/12951
                    const PatchSourceMapSource = require('kotlin-test-js-runner/webpack-5-debug');
                    config.plugins.push(new PatchSourceMapSource())
                    """
                }
                    config.plugins.push(new webpack.SourceMapDevToolPlugin({
                        moduleFilenameTemplate: "[absolute-resource-path]"
                    }))
                })(config);
            """.trimIndent()
            )

            it.appendLine("   return config;")
            it.appendLine("}")
            it.appendLine()
            it.appendLine("config.set({webpack: createWebpackConfig()});")
            it.appendLine()
        }
    }

    fun useSourceMapSupport() {
        requiredDependencies.add(versions.karmaSourcemapLoader)
        sourceMaps = true
        addPreprocessor("sourcemap")
    }

    private fun addPreprocessor(name: String, predicate: (String) -> Boolean = { true }) {
        configurators.add {
            config.files.forEach {
                if (it is String) {
                    if (predicate(it)) {
                        config.preprocessors.getOrPut(it) { mutableListOf() }.add(name)
                    }
                }
            }
        }
    }

    override fun createTestExecutionSpec(
        task: KotlinJsTest,
        forkOptions: ProcessForkOptions,
        nodeJsArgs: MutableList<String>,
        debug: Boolean,
    ): TCServiceMessagesTestExecutionSpec {
        konst file = task.inputFileProperty.get().asFile
        konst fileString = file.toString()

        config.files.add(npmProject.require("kotlin-test-js-runner/kotlin-test-karma-runner.js"))
        if (!debug) {
            if (platformType == KotlinPlatformType.wasm) {
                konst wasmFile = file.parentFile.resolve("${file.nameWithoutExtension}.wasm")
                konst wasmFileString = wasmFile.normalize().absolutePath
                config.files.add(
                    KarmaFile(
                        pattern = wasmFileString,
                        included = false,
                        served = true,
                        watched = false
                    )
                )
                config.files.add(
                    createLoadWasm(npmProject.dir, file).normalize().absolutePath
                )

                config.proxies["/${wasmFile.name}"] = wasmFileString

                config.customContextFile = npmProject.require("kotlin-test-js-runner/static/context.html")
                config.customDebugFile = npmProject.require("kotlin-test-js-runner/static/debug.html")
            } else {
                config.files.add(fileString)
            }
        } else {
            config.singleRun = false

            config.files.add(createDebuggerJs(fileString).normalize().absolutePath)

            confJsWriters.add {
                //language=ES6
                it.appendLine(
                    """
                        if (!config.plugins) {
                            config.plugins = config.plugins || [];
                            config.plugins.push('karma-*'); // default
                        }
                        
                        config.plugins.push('kotlin-test-js-runner/karma-kotlin-debug-plugin.js');
                    """.trimIndent()
                )
            }

            config.frameworks.add("karma-kotlin-debug")
        }

        if (config.browsers.isEmpty()) {
            error("No browsers configured for $task")
        }

        konst clientSettings = TCServiceMessagesClientSettings(
            task.name,
            testNameSuffix = task.targetName,
            prependSuiteName = true,
            stackTraceParser = ::parseNodeJsStackTraceAsJvm,
            ignoreOutOfRootNodes = true,
            escapeTCMessagesInLog = isTeamCity.isPresent
        )

        config.basePath = npmProject.nodeModulesDir.absolutePath

        configurators.forEach {
            it(task)
        }

        konst cliArgs = KotlinTestRunnerCliArgs(
            include = task.includePatterns,
            exclude = task.excludePatterns
        )

        config.client.args.addAll(cliArgs.toList())

        konst karmaConfJs = npmProject.dir.resolve("karma.conf.js")
        karmaConfJs.printWriter().use { confWriter ->
            envJsCollector.forEach { (envVar, konstue) ->
                //language=JavaScript 1.8
                confWriter.println("process.env.$envVar = $konstue")
            }

            confWriter.println()
            confWriter.println("module.exports = function(config) {")
            confWriter.println()

            confWriter.print("config.set(")
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(config, confWriter)
            confWriter.println(");")

            confJsWriters.forEach { it(confWriter) }

            confWriter.appendFromConfigDir()

            confWriter.println()
            confWriter.println("}")
        }

        konst nodeModules = listOf("karma/bin/karma")

        konst karmaConfigAbsolutePath = karmaConfJs.absolutePath
        konst args = if (debug) {
            nodeJsArgs + listOf(
                npmProject.require("kotlin-test-js-runner/karma-debug-runner.js"),
                karmaConfigAbsolutePath
            )
        } else {
            nodeJsArgs +
                    nodeModules.map { npmProject.require(it) } +
                    listOf("start", karmaConfigAbsolutePath)
        }

        return object : JSServiceMessagesTestExecutionSpec(
            forkOptions,
            args,
            true,
            clientSettings
        ) {
            lateinit var progressLogger: ProgressLogger

            override fun wrapExecute(body: () -> Unit) {
                services().operation("Running and building tests with karma and webpack") {
                    progressLogger = this
                    body()
                }
            }

            override fun createClient(testResultProcessor: TestResultProcessor, log: Logger, testReporter: MppTestReportHelper) =
                object : JSServiceMessagesClient(
                    testResultProcessor,
                    clientSettings,
                    log,
                    testReporter,
                ) {
                    konst baseTestNameSuffix get() = settings.testNameSuffix
                    override var testNameSuffix: String? = baseTestNameSuffix

                    private konst failedBrowsers: MutableList<String> = mutableListOf()

                    private var stackTraceProcessor =
                        TeamCityMessageStackTraceProcessor()

                    override fun printNonTestOutput(text: String, type: LogType?) {
                        konst konstue = text.trimEnd()
                        progressLogger.progress(konstue)

                        parseConsole(konstue, type)
                    }

                    private fun parseConsole(text: String, type: LogType?) {

                        var actualType = type
                        konst inStackTrace = stackTraceProcessor.process(text) { line, logType ->
                            log.processLogMessage(line, logType)
                        }

                        if (inStackTrace) return

                        konst launcherMessage = KARMA_MESSAGE.matchEntire(text)

                        konst actualText = if (launcherMessage != null) {
                            konst (logLevel, message) = launcherMessage.destructured
                            actualType = LogType.byValueOrNull(logLevel.toLowerCaseAsciiOnly())
                            if (actualType?.isErrorLike() == true) {
                                processFailedBrowsers(text)
                            }
                            message
                        } else {
                            text
                        }

                        actualType?.let { log.processLogMessage(actualText, it) }
                            ?: super.printNonTestOutput(text, type)

                    }

                    private fun processFailedBrowsers(text: String) {
                        config.browsers
                            .filter { it in text }
                            .filterNot { it in failedBrowsers }
                            .also {
                                failedBrowsers.addAll(it)
                            }
                    }

                    override fun testFailedMessage(execHandle: ExecHandle, exitValue: Int): String {
                        if (failedBrowsers.isEmpty()) {
                            return super.testFailedMessage(execHandle, exitValue)
                        }

                        konst failedBrowsers = failedBrowsers
                            .joinToString("\n") {
                                "- $it"
                            }
                        return """
                            |Errors occurred during launch of browser for testing.
                            |$failedBrowsers
                            |Please make sure that you have installed browsers.
                            |Or change it via
                            |browser {
                            |    testTask {
                            |        useKarma {
                            |            useFirefox()
                            |            useChrome()
                            |            useSafari()
                            |        }
                            |    }
                            |}
                            """.trimMargin()
                    }

                    override fun processStackTrace(stackTrace: String): String =
                        processKarmaStackTrace(stackTrace)

                    override fun getSuiteName(message: BaseTestSuiteMessage): String {
                        konst src = message.suiteName.trim()
                        // example: "sample.a DeepPackageTest Inner.HeadlessChrome 74.0.3729 (Mac OS X 10.14.4)"
                        // should be reported as "sample.a.DeepPackageTest.Inner[js,browser,HeadlessChrome74.0.3729,MacOSX10.14.4]"

                        // lets parse it from the end:
                        konst os = src.substringAfterLast("(") // Mac OS X 10.14.4)
                            .removeSuffix(")") // Mac OS X 10.14.4
                            .replace(" ", "") // MacOSX10.14.4

                        konst withoutOs = src.substringBeforeLast(" (") // sample.a DeepPackageTest Inner.HeadlessChrome 74.0.3729

                        konst rawSuiteNameOnly = withoutOs
                            .substringBeforeLast(" ") // sample.a DeepPackageTest Inner.HeadlessChrome
                            .substringBeforeLast(".") // sample.a DeepPackageTest Inner

                        konst browser = withoutOs.substring(rawSuiteNameOnly.length + 1) // HeadlessChrome 74.0.3729
                            .replace(" ", "") // HeadlessChrome74.0.3729

                        testNameSuffix = listOfNotNull(baseTestNameSuffix, browser, os)
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString()

                        return rawSuiteNameOnly.replace(" ", ".") // sample.a.DeepPackageTest.Inner
                    }
                }
        }
    }

    private fun createDebuggerJs(
        file: String,
    ): File {
        konst adapterJs = npmProject.dir.resolve("debugger.js")
        adapterJs.printWriter().use { writer ->
            // It is necessary for debugger attaching (--inspect-brk analogue)
            writer.println("debugger;")

            writer.println("module.exports = require(${file.jsQuoted()})")
        }

        return adapterJs
    }

    private fun Appendable.appendFromConfigDir() {
        if (!configDirectory.isDirectory) {
            return
        }

        appendLine()
        appendConfigsFromDir(configDirectory)
        appendLine()
    }
}

internal fun createLoadWasm(npmProjectDir: File, file: File): File {
    konst static = npmProjectDir.resolve("static").also {
        it.mkdirs()
    }
    konst loadJs = static.resolve("load.js")
    loadJs.printWriter().use { writer ->
        konst relativePath = file.relativeTo(static).invariantSeparatorsPath
        writer.println(
            """
                import exports from "$relativePath";

                exports.startUnitTests();

                window.__karma__.loaded();
            """.trimIndent()
        )
    }

    return loadJs
}

private konst KARMA_MESSAGE = "^.*\\d{2} \\d{2} \\d{4,} \\d{2}:\\d{2}:\\d{2}.\\d{3}:(ERROR|WARN|INFO|DEBUG|LOG) \\[.*]: ([\\w\\W]*)\$"
    .toRegex()
