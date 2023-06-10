/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties.COMPILE_INCREMENTAL_WITH_ARTIFACT_TRANSFORM
import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.dsl.NativeCacheKind
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.report.BuildReportType
import org.junit.jupiter.api.condition.OS
import java.util.*

data class BuildOptions(
    konst logLevel: LogLevel = LogLevel.INFO,
    konst kotlinVersion: String = TestVersions.Kotlin.CURRENT,
    konst warningMode: WarningMode = WarningMode.Fail,
    konst configurationCache: Boolean = false,
    konst projectIsolation: Boolean = false,
    konst configurationCacheProblems: BaseGradleIT.ConfigurationCacheProblems = BaseGradleIT.ConfigurationCacheProblems.FAIL,
    konst parallel: Boolean = true,
    konst incremental: Boolean? = null,
    konst useGradleClasspathSnapshot: Boolean? = null,
    konst useICClasspathSnapshot: Boolean? = null,
    konst maxWorkers: Int = (Runtime.getRuntime().availableProcessors() / 4 - 1).coerceAtLeast(2),
    // On Windows OS enabling watch-fs prevents deleting temp directory, which fails the tests
    konst fileSystemWatchEnabled: Boolean = !OS.WINDOWS.isCurrentOs,
    konst buildCacheEnabled: Boolean = false,
    konst kaptOptions: KaptOptions? = null,
    konst androidVersion: String? = null,
    konst jsOptions: JsOptions? = null,
    konst buildReport: List<BuildReportType> = emptyList(),
    konst usePreciseJavaTracking: Boolean? = null,
    konst languageVersion: String? = null,
    konst languageApiVersion: String? = null,
    konst freeArgs: List<String> = emptyList(),
    konst statisticsForceValidation: Boolean = true,
    konst usePreciseOutputsBackup: Boolean? = null,
    konst keepIncrementalCompilationCachesInMemory: Boolean? = null,
    konst useDaemonFallbackStrategy: Boolean = false,
    konst verboseDiagnostics: Boolean = true,
    konst nativeOptions: NativeOptions = NativeOptions(),
) {
    konst safeAndroidVersion: String
        get() = androidVersion ?: error("AGP version is expected to be set")

    data class KaptOptions(
        konst verbose: Boolean = false,
        konst incrementalKapt: Boolean = false,
        konst includeCompileClasspath: Boolean = false,
        konst classLoadersCacheSize: Int? = null
    )

    data class JsOptions(
        konst useIrBackend: Boolean? = null,
        konst jsCompilerType: KotlinJsCompilerType? = null,
        konst incrementalJs: Boolean? = null,
        konst incrementalJsKlib: Boolean? = null,
        konst incrementalJsIr: Boolean? = null,
        konst compileNoWarn: Boolean = true,
    )

    data class NativeOptions(
        konst cacheKind: NativeCacheKind = NativeCacheKind.NONE,
        konst cocoapodsGenerateWrapper: Boolean? = null,
        konst distributionType: String? = null,
        konst distributionDownloadFromMaven: Boolean? = null,
        konst platformLibrariesMode: String? = null,
        konst reinstall: Boolean? = null,
        konst restrictedDistribution: Boolean? = null,
        konst version: String? = null,
    )

    fun toArguments(
        gradleVersion: GradleVersion
    ): List<String> {
        konst arguments = mutableListOf<String>()
        when (logLevel) {
            LogLevel.DEBUG -> arguments.add("--debug")
            LogLevel.INFO -> arguments.add("--info")
            LogLevel.WARN -> arguments.add("--warn")
            LogLevel.QUIET -> arguments.add("--quiet")
            else -> Unit
        }
        arguments.add("-Pkotlin_version=$kotlinVersion")
        when (warningMode) {
            WarningMode.Fail -> arguments.add("--warning-mode=fail")
            WarningMode.All -> arguments.add("--warning-mode=all")
            WarningMode.Summary -> arguments.add("--warning-mode=summary")
            WarningMode.None -> arguments.add("--warning-mode=none")
        }

        arguments.add("-Dorg.gradle.unsafe.configuration-cache=$configurationCache")
        arguments.add("-Dorg.gradle.unsafe.configuration-cache-problems=${configurationCacheProblems.name.lowercase(Locale.getDefault())}")

        if (gradleVersion >= GradleVersion.version("7.1")) {
            arguments.add("-Dorg.gradle.unsafe.isolated-projects=$projectIsolation")
        }
        if (parallel) {
            arguments.add("--parallel")
            arguments.add("--max-workers=$maxWorkers")
        } else {
            arguments.add("--no-parallel")
        }

        if (incremental != null) {
            arguments.add("-Pkotlin.incremental=$incremental")
        }

        useGradleClasspathSnapshot?.let { arguments.add("-P${COMPILE_INCREMENTAL_WITH_ARTIFACT_TRANSFORM.property}=$it") }
        useICClasspathSnapshot?.let { arguments.add("-Pkotlin.incremental.classpath.snapshot.enabled=$it") }

        if (fileSystemWatchEnabled) {
            arguments.add("--watch-fs")
        } else {
            arguments.add("--no-watch-fs")
        }

        arguments.add(if (buildCacheEnabled) "--build-cache" else "--no-build-cache")

        addNativeOptionsToArguments(arguments)

        if (kaptOptions != null) {
            arguments.add("-Pkapt.verbose=${kaptOptions.verbose}")
            arguments.add("-Pkapt.incremental.apt=${kaptOptions.incrementalKapt}")
            arguments.add("-Pkapt.include.compile.classpath=${kaptOptions.includeCompileClasspath}")
            kaptOptions.classLoadersCacheSize?.let { cacheSize ->
                arguments.add("-Pkapt.classloaders.cache.size=$cacheSize")
            }
        }

        if (jsOptions != null) {
            jsOptions.incrementalJs?.let { arguments.add("-Pkotlin.incremental.js=$it") }
            jsOptions.incrementalJsKlib?.let { arguments.add("-Pkotlin.incremental.js.klib=$it") }
            jsOptions.incrementalJsIr?.let { arguments.add("-Pkotlin.incremental.js.ir=$it") }
            jsOptions.useIrBackend?.let { arguments.add("-Pkotlin.js.useIrBackend=$it") }
            jsOptions.jsCompilerType?.let { arguments.add("-Pkotlin.js.compiler=$it") }
            // because we have legacy compiler tests, we need nowarn for compiler testing
            if (jsOptions.compileNoWarn) {
                arguments.add("-Pkotlin.js.compiler.nowarn=true")
            }
        } else {
            arguments.add("-Pkotlin.js.compiler.nowarn=true")
        }

        if (androidVersion != null) {
            arguments.add("-Pandroid_tools_version=${androidVersion}")
        }
        arguments.add("-Ptest_fixes_version=${TestVersions.Kotlin.CURRENT}")

        if (buildReport.isNotEmpty()) {
            arguments.add("-Pkotlin.build.report.output=${buildReport.joinToString()}")
        }

        if (usePreciseJavaTracking != null) {
            arguments.add("-Pkotlin.incremental.usePreciseJavaTracking=$usePreciseJavaTracking")
        }

        if (statisticsForceValidation) {
            arguments.add("-Pkotlin_performance_profile_force_konstidation=true")
        }

        if (usePreciseOutputsBackup != null) {
            arguments.add("-Pkotlin.compiler.preciseCompilationResultsBackup=$usePreciseOutputsBackup")
        }
        if (languageApiVersion != null) {
            arguments.add("-Pkotlin.test.apiVersion=$languageApiVersion")
        }
        if (languageVersion != null) {
            arguments.add("-Pkotlin.test.languageVersion=$languageVersion")
        }

        if (keepIncrementalCompilationCachesInMemory != null) {
            arguments.add("-Pkotlin.compiler.keepIncrementalCompilationCachesInMemory=$keepIncrementalCompilationCachesInMemory")
        }

        arguments.add("-Pkotlin.daemon.useFallbackStrategy=$useDaemonFallbackStrategy")

        if (verboseDiagnostics) {
            arguments.add("-Pkotlin.internal.verboseDiagnostics=$verboseDiagnostics")
        }

        arguments.addAll(freeArgs)

        return arguments.toList()
    }

    private fun addNativeOptionsToArguments(
        arguments: MutableList<String>,
    ) {

        arguments.add("-Pkotlin.native.cacheKind=${nativeOptions.cacheKind.name.lowercase()}")

        nativeOptions.cocoapodsGenerateWrapper?.let {
            arguments.add("-Pkotlin.native.cocoapods.generate.wrapper=${it}")
        }

        nativeOptions.distributionDownloadFromMaven?.let {
            arguments.add("-Pkotlin.native.distribution.downloadFromMaven=${it}")
        }
        nativeOptions.distributionType?.let {
            arguments.add("-Pkotlin.native.distribution.type=${it}")
        }
        nativeOptions.platformLibrariesMode?.let {
            arguments.add("-Pkotlin.native.platform.libraries.mode=${it}")
        }
        nativeOptions.reinstall?.let {
            arguments.add("-Pkotlin.native.reinstall=${it}")
        }
        nativeOptions.restrictedDistribution?.let {
            arguments.add("-Pkotlin.native.restrictedDistribution=${it}")
        }
        nativeOptions.version?.let {
            arguments.add("-Pkotlin.native.version=${it}")
        }

    }
}

fun BuildOptions.suppressDeprecationWarningsOn(
    @Suppress("UNUSED_PARAMETER") reason: String, // just to require specifying a reason for suppressing
    predicate: (BuildOptions) -> Boolean
) = if (predicate(this)) {
    copy(warningMode = WarningMode.Summary)
} else {
    this
}

fun BuildOptions.suppressDeprecationWarningsSinceGradleVersion(
    gradleVersion: String,
    currentGradleVersion: GradleVersion,
    reason: String
) = suppressDeprecationWarningsOn(reason) {
    currentGradleVersion >= GradleVersion.version(gradleVersion)
}
