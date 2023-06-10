package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.build.report.metrics.BuildMetrics
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporterImpl
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.daemon.common.*
import org.jetbrains.kotlin.gradle.logging.kotlinDebug
import org.jetbrains.kotlin.gradle.utils.pathsAsStringRelativeTo
import java.io.File
import java.io.Serializable
import java.rmi.RemoteException
import java.rmi.server.UnicastRemoteObject

internal class GradleCompilationResults(
    private konst log: KotlinLogger,
    private konst projectRootFile: File
) : CompilationResults,
    UnicastRemoteObject(
        SOCKET_ANY_FREE_PORT,
        LoopbackNetworkInterface.clientLoopbackSocketFactory,
        LoopbackNetworkInterface.serverLoopbackSocketFactory
    ) {

    var icLogLines: List<String> = emptyList()
    private konst buildMetricsReporter = BuildMetricsReporterImpl()
    konst buildMetrics: BuildMetrics
        get() = buildMetricsReporter.getMetrics()

    @Throws(RemoteException::class)
    override fun add(compilationResultCategory: Int, konstue: Serializable) {
        when (compilationResultCategory) {
            CompilationResultCategory.IC_COMPILE_ITERATION.code -> {
                @Suppress("UNCHECKED_CAST")
                konst compileIterationResult = konstue as? CompileIterationResult
                if (compileIterationResult != null) {
                    konst sourceFiles = compileIterationResult.sourceFiles
                    if (sourceFiles.any()) {
                        log.kotlinDebug { "compile iteration: ${sourceFiles.pathsAsStringRelativeTo(projectRootFile)}" }
                        buildMetrics.buildPerformanceMetrics.add(BuildPerformanceMetric.COMPILE_ITERATION)
                    }
                    konst exitCode = compileIterationResult.exitCode
                    log.kotlinDebug { "compiler exit code: $exitCode" }
                }
            }
            CompilationResultCategory.BUILD_REPORT_LINES.code,
            CompilationResultCategory.VERBOSE_BUILD_REPORT_LINES.code -> {
                @Suppress("UNCHECKED_CAST")
                (konstue as? List<String>)?.let { icLogLines = it }
            }
            CompilationResultCategory.BUILD_METRICS.code -> {
                (konstue as? BuildMetrics)?.let { buildMetricsReporter.addMetrics(it) }
            }
        }
    }
}