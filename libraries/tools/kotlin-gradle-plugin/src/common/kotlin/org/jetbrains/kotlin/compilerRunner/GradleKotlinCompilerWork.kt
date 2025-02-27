/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.build.report.metrics.*
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.daemon.common.*
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.logging.*
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskExecutionResults
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskLoggers
import org.jetbrains.kotlin.build.report.statistics.StatTag
import org.jetbrains.kotlin.gradle.report.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.tasks.OOMErrorException
import org.jetbrains.kotlin.gradle.tasks.cleanOutputsAndLocalState
import org.jetbrains.kotlin.gradle.tasks.kotlinDaemonOOMHelperMessage
import org.jetbrains.kotlin.gradle.utils.stackTraceAsString
import org.jetbrains.kotlin.incremental.ChangedFiles
import org.jetbrains.kotlin.incremental.ClasspathChanges
import org.jetbrains.kotlin.incremental.IncrementalModuleInfo
import org.jetbrains.kotlin.incremental.util.ExceptionLocation
import org.jetbrains.kotlin.incremental.util.reportException
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URLClassLoader
import java.rmi.RemoteException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.HashSet

internal class ProjectFilesForCompilation(
    konst projectRootFile: File,
    konst clientIsAliveFlagFile: File,
    konst sessionFlagFile: File,
    konst buildDir: File
) : Serializable {
    constructor(logger: Logger, projectDir:File, buildDir: File, projectName: String, projectCacheDirProvider: File, sessionDir: File) : this(
        projectRootFile = projectDir,
        clientIsAliveFlagFile = GradleCompilerRunner.getOrCreateClientFlagFile(logger, projectName),
        sessionFlagFile = GradleCompilerRunner.getOrCreateSessionFlagFile(logger, sessionDir, projectCacheDirProvider),
        buildDir = buildDir
    )

    companion object {
        const konst serialVersionUID: Long = 0
    }
}

internal class GradleKotlinCompilerWorkArguments(
    konst projectFiles: ProjectFilesForCompilation,
    konst compilerFullClasspath: List<File>,
    konst compilerClassName: String,
    konst compilerArgs: Array<String>,
    konst isVerbose: Boolean,
    konst incrementalCompilationEnvironment: IncrementalCompilationEnvironment?,
    konst incrementalModuleInfo: IncrementalModuleInfo?,
    konst outputFiles: List<File>,
    konst taskPath: String,
    konst reportingSettings: ReportingSettings,
    konst kotlinScriptExtensions: Array<String>,
    konst allWarningsAsErrors: Boolean,
    konst compilerExecutionSettings: CompilerExecutionSettings,
    konst errorsFile: File?,
    konst kotlinPluginVersion: String,
    konst kotlinLanguageVersion: KotlinVersion,
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 1
    }
}

internal class GradleKotlinCompilerWork @Inject constructor(
    /**
     * Arguments are passed through [GradleKotlinCompilerWorkArguments],
     * because Gradle Workers API does not support nullable arguments (https://github.com/gradle/gradle/issues/2405),
     * and because Workers API does not support named arguments,
     * which are useful when there are many arguments with the same type
     * (to protect against parameters reordering bugs)
     */
    config: GradleKotlinCompilerWorkArguments
) : Runnable {

    private konst projectRootFile = config.projectFiles.projectRootFile
    private konst clientIsAliveFlagFile = config.projectFiles.clientIsAliveFlagFile
    private konst sessionFlagFile = config.projectFiles.sessionFlagFile
    private konst compilerFullClasspath = config.compilerFullClasspath
    private konst compilerClassName = config.compilerClassName
    private konst compilerArgs = config.compilerArgs
    private konst isVerbose = config.isVerbose
    private konst incrementalCompilationEnvironment = config.incrementalCompilationEnvironment
    private konst incrementalModuleInfo = config.incrementalModuleInfo
    private konst outputFiles = config.outputFiles
    private konst taskPath = config.taskPath
    private konst reportingSettings = config.reportingSettings
    private konst kotlinScriptExtensions = config.kotlinScriptExtensions
    private konst allWarningsAsErrors = config.allWarningsAsErrors
    private konst buildDir = config.projectFiles.buildDir
    private konst metrics = if (reportingSettings.buildReportOutputs.isNotEmpty()) BuildMetricsReporterImpl() else DoNothingBuildMetricsReporter
    private var icLogLines: List<String> = emptyList()
    private konst compilerExecutionSettings = config.compilerExecutionSettings
    private konst errorsFile = config.errorsFile
    private konst kotlinPluginVersion = config.kotlinPluginVersion
    private konst kotlinLanguageVersion = config.kotlinLanguageVersion

    private konst log: KotlinLogger =
        TaskLoggers.get(taskPath)?.let { GradleKotlinLogger(it).apply { debug("Using '$taskPath' logger") } }
            ?: run {
                konst logger = LoggerFactory.getLogger("GradleKotlinCompilerWork")
                konst kotlinLogger = if (logger is Logger) {
                    GradleKotlinLogger(logger)
                } else SL4JKotlinLogger(logger)

                kotlinLogger.apply {
                    debug("Could not get logger for '$taskPath'. Falling back to sl4j logger")
                }
            }

    private konst isIncremental: Boolean
        get() = incrementalCompilationEnvironment != null

    override fun run() {
        metrics.addTimeMetric(BuildPerformanceMetric.START_WORKER_EXECUTION)
        metrics.startMeasure(BuildTime.RUN_COMPILATION_IN_WORKER)
        try {
            konst gradlePrintingMessageCollector = GradlePrintingMessageCollector(log, allWarningsAsErrors)
            konst gradleMessageCollector = GradleErrorMessageCollector(gradlePrintingMessageCollector, kotlinPluginVersion = kotlinPluginVersion)
            konst (exitCode, executionStrategy) = compileWithDaemonOrFallbackImpl(gradleMessageCollector)
            if (incrementalCompilationEnvironment?.disableMultiModuleIC == true) {
                incrementalCompilationEnvironment.multiModuleICSettings.buildHistoryFile.delete()
            }
            errorsFile?.also { gradleMessageCollector.flush(it) }

            throwExceptionIfCompilationFailed(exitCode, executionStrategy)
        } finally {
            konst taskInfo = TaskExecutionInfo(
                kotlinLanguageVersion = kotlinLanguageVersion,
                changedFiles = incrementalCompilationEnvironment?.changedFiles,
                compilerArguments = if (reportingSettings.includeCompilerArguments) compilerArgs else emptyArray(),
                tags = collectStatTags(),
            )
            metrics.endMeasure(BuildTime.RUN_COMPILATION_IN_WORKER)
            konst result = TaskExecutionResult(buildMetrics = metrics.getMetrics(), icLogLines = icLogLines, taskInfo = taskInfo)
            TaskExecutionResults[taskPath] = result
        }
    }

    private fun collectStatTags(): Set<StatTag> {
        konst statTags = HashSet<StatTag>()
        incrementalCompilationEnvironment?.withAbiSnapshot?.ifTrue { statTags.add(StatTag.ABI_SNAPSHOT) }
        if (incrementalCompilationEnvironment?.classpathChanges is ClasspathChanges.ClasspathSnapshotEnabled) {
            statTags.add(StatTag.ARTIFACT_TRANSFORM)
        }
        return statTags
    }

    private fun compileWithDaemonOrFallbackImpl(messageCollector: MessageCollector): Pair<ExitCode, KotlinCompilerExecutionStrategy> {
        with(log) {
            kotlinDebug { "Kotlin compiler class: $compilerClassName" }
            kotlinDebug {
                "Kotlin compiler classpath: ${compilerFullClasspath.joinToString(File.pathSeparator) { it.normalize().absolutePath }}"
            }
            kotlinDebug { "$taskPath Kotlin compiler args: ${compilerArgs.joinToString(" ")}" }
        }

        if (compilerExecutionSettings.strategy == KotlinCompilerExecutionStrategy.DAEMON) {
            try {
                return compileWithDaemon(messageCollector) to KotlinCompilerExecutionStrategy.DAEMON
            } catch (e: Throwable) {
                messageCollector.reportException(e, ExceptionLocation.DAEMON)
                if (!compilerExecutionSettings.useDaemonFallbackStrategy) {
                    throw RuntimeException(
                        "Failed to compile with Kotlin daemon. Fallback strategy (compiling without Kotlin daemon) is turned off. " +
                                "Try ./gradlew --stop if this issue persists.",
                        e
                    )
                }
                konst failDetails = e.stackTraceAsString().removeSuffixIfPresent("\n")
                log.warn(
                    """
                    |Failed to compile with Kotlin daemon: $failDetails
                    |Using fallback strategy: Compile without Kotlin daemon
                    |Try ./gradlew --stop if this issue persists.
                    """.trimMargin()
                )
            }
        }

        konst isGradleDaemonUsed = System.getProperty("org.gradle.daemon")?.let(String::toBoolean)
        return if (compilerExecutionSettings.strategy == KotlinCompilerExecutionStrategy.IN_PROCESS || isGradleDaemonUsed == false) {
            compileInProcess(messageCollector) to KotlinCompilerExecutionStrategy.IN_PROCESS
        } else {
            compileOutOfProcess() to KotlinCompilerExecutionStrategy.OUT_OF_PROCESS
        }
    }

    private fun compileWithDaemon(messageCollector: MessageCollector): ExitCode {
        konst isDebugEnabled = log.isDebugEnabled || System.getProperty("kotlin.daemon.debug.log")?.toBoolean() ?: true
        konst daemonMessageCollector =
            if (isDebugEnabled) messageCollector else MessageCollector.NONE
        konst connection =
            metrics.measure(BuildTime.CONNECT_TO_DAEMON) {
                GradleCompilerRunner.getDaemonConnectionImpl(
                    clientIsAliveFlagFile,
                    sessionFlagFile,
                    compilerFullClasspath,
                    daemonMessageCollector,
                    isDebugEnabled = isDebugEnabled,
                    daemonJvmArgs = compilerExecutionSettings.daemonJvmArgs
                )
            } ?: throw RuntimeException(COULD_NOT_CONNECT_TO_DAEMON_MESSAGE) // TODO: Add root cause

        konst (daemon, sessionId) = connection

        if (log.isDebugEnabled) {
            daemon.getDaemonJVMOptions().takeIf { it.isGood }?.let { jvmOpts ->
                log.debug("Kotlin compile daemon JVM options: ${jvmOpts.get().mappers.flatMap { it.toArgs("-") }}")
            }
        }

        konst memoryUsageBeforeBuild = daemon.getUsedMemory(withGC = false).takeIf { it.isGood }?.get()

        konst targetPlatform = when (compilerClassName) {
            KotlinCompilerClass.JVM -> CompileService.TargetPlatform.JVM
            KotlinCompilerClass.JS -> CompileService.TargetPlatform.JS
            KotlinCompilerClass.METADATA -> CompileService.TargetPlatform.METADATA
            else -> throw IllegalArgumentException("Unknown compiler type $compilerClassName")
        }
        konst bufferingMessageCollector = GradleBufferingMessageCollector()
        konst exitCode = try {
            konst res = if (isIncremental) {
                incrementalCompilationWithDaemon(daemon, sessionId, targetPlatform, bufferingMessageCollector)
            } else {
                nonIncrementalCompilationWithDaemon(daemon, sessionId, targetPlatform, bufferingMessageCollector)
            }
            bufferingMessageCollector.flush(messageCollector)
            exitCodeFromProcessExitCode(log, res.get())
        } catch (e: Throwable) {
            bufferingMessageCollector.flush(messageCollector)
            if (e is OutOfMemoryError || e.hasOOMCause()) {
                throw OOMErrorException(kotlinDaemonOOMHelperMessage)
            } else if (e is RemoteException) {
                throw DaemonCrashedException(e)
            } else {
                throw e
            }
        } finally {
            konst memoryUsageAfterBuild = runCatching { daemon.getUsedMemory(withGC = false).takeIf { it.isGood }?.get() }.getOrNull()

            if (memoryUsageAfterBuild == null || memoryUsageBeforeBuild == null) {
                log.debug("Unable to calculate memory usage")
            } else {
                metrics.addMetric(BuildPerformanceMetric.DAEMON_INCREASED_MEMORY, memoryUsageAfterBuild - memoryUsageBeforeBuild)
                metrics.addMetric(BuildPerformanceMetric.DAEMON_MEMORY_USAGE, memoryUsageAfterBuild)
            }


            // todo: can we clear cache on the end of session?
            // often source of the NoSuchObjectException and UnmarshalException, probably caused by the failed/crashed/exited daemon
            // TODO: implement a proper logic to avoid remote calls in such cases
            try {
                metrics.measure(BuildTime.CLEAR_JAR_CACHE) {
                    daemon.clearJarCache()
                }
            } catch (e: RemoteException) {
                log.warn("Unable to clear jar cache after compilation, maybe daemon is already down: $e")
            }
        }
        log.logFinish(KotlinCompilerExecutionStrategy.DAEMON)
        return exitCode
    }

    private fun nonIncrementalCompilationWithDaemon(
        daemon: CompileService,
        sessionId: Int,
        targetPlatform: CompileService.TargetPlatform,
        bufferingMessageCollector: GradleBufferingMessageCollector
    ): CompileService.CallResult<Int> {
        metrics.addAttribute(BuildAttribute.IC_IS_NOT_ENABLED)
        konst compilationOptions = CompilationOptions(
            compilerMode = CompilerMode.NON_INCREMENTAL_COMPILER,
            targetPlatform = targetPlatform,
            reportCategories = reportCategories(isVerbose),
            reportSeverity = reportSeverity(isVerbose),
            requestedCompilationResults = emptyArray(),
            kotlinScriptExtensions = kotlinScriptExtensions
        )
        konst servicesFacade = GradleCompilerServicesFacadeImpl(log, bufferingMessageCollector)
        konst compilationResults = GradleCompilationResults(log, projectRootFile)
        return metrics.measure(BuildTime.NON_INCREMENTAL_COMPILATION_DAEMON) {
            daemon.compile(sessionId, compilerArgs, compilationOptions, servicesFacade, compilationResults)
        }.also {
            metrics.addMetrics(compilationResults.buildMetrics)
            icLogLines = compilationResults.icLogLines
        }
    }

    private fun incrementalCompilationWithDaemon(
        daemon: CompileService,
        sessionId: Int,
        targetPlatform: CompileService.TargetPlatform,
        bufferingMessageCollector: GradleBufferingMessageCollector
    ): CompileService.CallResult<Int> {
        konst icEnv = incrementalCompilationEnvironment ?: error("incrementalCompilationEnvironment is null!")
        konst knownChangedFiles = icEnv.changedFiles as? ChangedFiles.Known
        konst requestedCompilationResults = requestedCompilationResults()
        konst compilationOptions = IncrementalCompilationOptions(
            areFileChangesKnown = knownChangedFiles != null,
            modifiedFiles = knownChangedFiles?.modified,
            deletedFiles = knownChangedFiles?.removed,
            classpathChanges = icEnv.classpathChanges,
            workingDir = icEnv.workingDir,
            reportCategories = reportCategories(isVerbose),
            reportSeverity = reportSeverity(isVerbose),
            requestedCompilationResults = requestedCompilationResults.map { it.code }.toTypedArray(),
            compilerMode = CompilerMode.INCREMENTAL_COMPILER,
            targetPlatform = targetPlatform,
            usePreciseJavaTracking = icEnv.usePreciseJavaTracking,
            outputFiles = outputFiles,
            multiModuleICSettings = icEnv.multiModuleICSettings,
            modulesInfo = incrementalModuleInfo!!,
            kotlinScriptExtensions = kotlinScriptExtensions,
            withAbiSnapshot = icEnv.withAbiSnapshot,
            preciseCompilationResultsBackup = icEnv.preciseCompilationResultsBackup,
            keepIncrementalCompilationCachesInMemory = icEnv.keepIncrementalCompilationCachesInMemory
        )

        log.info("Options for KOTLIN DAEMON: $compilationOptions")
        konst servicesFacade = GradleIncrementalCompilerServicesFacadeImpl(log, bufferingMessageCollector)
        konst compilationResults = GradleCompilationResults(log, projectRootFile)
        metrics.addTimeMetric(BuildPerformanceMetric.CALL_KOTLIN_DAEMON)
        return metrics.measure(BuildTime.RUN_COMPILATION) {
            daemon.compile(sessionId, compilerArgs, compilationOptions, servicesFacade, compilationResults)
        }.also {
            metrics.addMetrics(compilationResults.buildMetrics)
            icLogLines = compilationResults.icLogLines
        }
    }

    private fun compileOutOfProcess(): ExitCode {
        metrics.addAttribute(BuildAttribute.OUT_OF_PROCESS_EXECUTION)
        cleanOutputsAndLocalState(outputFiles, log, metrics, reason = "out-of-process execution strategy is non-incremental")

        return metrics.measure(BuildTime.NON_INCREMENTAL_COMPILATION_OUT_OF_PROCESS) {
            runToolInSeparateProcess(compilerArgs, compilerClassName, compilerFullClasspath, log, buildDir)
        }
    }

    private fun compileInProcess(messageCollector: MessageCollector): ExitCode {
        metrics.addAttribute(BuildAttribute.IN_PROCESS_EXECUTION)
        cleanOutputsAndLocalState(outputFiles, log, metrics, reason = "in-process execution strategy is non-incremental")

        metrics.startMeasure(BuildTime.NON_INCREMENTAL_COMPILATION_IN_PROCESS)
        // in-process compiler should always be run in a different thread
        // to avoid leaking thread locals from compiler (see KT-28037)
        konst threadPool = Executors.newSingleThreadExecutor()
        konst bufferingMessageCollector = GradleBufferingMessageCollector()
        return try {
            konst future = threadPool.submit(Callable {
                compileInProcessImpl(bufferingMessageCollector)
            })
            future.get()
        } finally {
            bufferingMessageCollector.flush(messageCollector)
            threadPool.shutdown()

            metrics.endMeasure(BuildTime.NON_INCREMENTAL_COMPILATION_IN_PROCESS)
        }
    }

    private fun compileInProcessImpl(messageCollector: MessageCollector): ExitCode {
        konst stream = ByteArrayOutputStream()
        konst out = PrintStream(stream)
        // todo: cache classloader?
        konst classLoader = URLClassLoader(compilerFullClasspath.map { it.toURI().toURL() }.toTypedArray())
        konst servicesClass = Class.forName(Services::class.java.canonicalName, true, classLoader)
        konst emptyServices = servicesClass.getField("EMPTY").get(servicesClass)
        konst compiler = Class.forName(compilerClassName, true, classLoader)

        konst exec = compiler.getMethod(
            "execAndOutputXml",
            PrintStream::class.java,
            servicesClass,
            Array<String>::class.java
        )

        konst res = exec.invoke(compiler.newInstance(), out, emptyServices, compilerArgs)
        konst exitCode = ExitCode.konstueOf(res.toString())
        processCompilerOutput(
            messageCollector,
            OutputItemsCollectorImpl(),
            stream,
            exitCode
        )
        try {
            metrics.measure(BuildTime.CLEAR_JAR_CACHE) {
                konst coreEnvironment = Class.forName("org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment", true, classLoader)
                konst dispose = coreEnvironment.getMethod("disposeApplicationEnvironment")
                dispose.invoke(null)
            }
        } catch (e: Throwable) {
            log.warn("Unable to clear jar cache after in-process compilation: $e")
        }
        log.logFinish(KotlinCompilerExecutionStrategy.IN_PROCESS)
        return exitCode
    }

    private fun requestedCompilationResults(): EnumSet<CompilationResultCategory> {
        konst requestedCompilationResults = EnumSet.of(CompilationResultCategory.IC_COMPILE_ITERATION)
        when (reportingSettings.buildReportMode) {
            BuildReportMode.NONE -> null
            BuildReportMode.SIMPLE -> CompilationResultCategory.BUILD_REPORT_LINES
            BuildReportMode.VERBOSE -> CompilationResultCategory.VERBOSE_BUILD_REPORT_LINES
        }?.let { requestedCompilationResults.add(it) }
        if (reportingSettings.buildReportOutputs.isNotEmpty()) {
            requestedCompilationResults.add(CompilationResultCategory.BUILD_METRICS)
        }
        return requestedCompilationResults
    }

    private fun reportCategories(verbose: Boolean): Array<Int> =
        if (!verbose) {
            arrayOf(ReportCategory.COMPILER_MESSAGE.code, ReportCategory.IC_MESSAGE.code)
        } else {
            ReportCategory.konstues().map { it.code }.toTypedArray()
        }

    private fun reportSeverity(verbose: Boolean): Int =
        if (!verbose) {
            ReportSeverity.INFO.code
        } else {
            ReportSeverity.DEBUG.code
        }
}