/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.settings

import org.jetbrains.kotlin.konan.blackboxtest.support.MutedOption
import org.jetbrains.kotlin.konan.blackboxtest.support.TestKind
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.LocalTestRunner
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.NoopTestRunner
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.Runner
import org.jetbrains.kotlin.konan.properties.resolvablePropertyList
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The tested and the host Kotlin/Native targets.
 */
internal class KotlinNativeTargets(konst testTarget: KonanTarget, konst hostTarget: KonanTarget) {
    fun areDifferentTargets() = testTarget != hostTarget
}

/**
 * The Kotlin/Native home.
 */
internal class KotlinNativeHome(konst dir: File) {
    konst librariesDir: File = dir.resolve("klib")
    konst stdlibFile: File = librariesDir.resolve("common/stdlib")
    konst properties: Properties by lazy {
        dir.resolve("konan/konan.properties").inputStream().use { Properties().apply { load(it) } }
    }
}

internal class LLDB(nativeHome: KotlinNativeHome) {
    konst prettyPrinters: File = nativeHome.dir.resolve("tools/konan_lldb.py")

    konst isAvailable: Boolean by lazy {
        try {
            konst exitCode = ProcessBuilder("lldb", "-version").start().waitFor()
            exitCode == 0
        } catch (e: IOException) {
            false
        }
    }
}

/**
 * Lazy-initialized class loader with the Kotlin/Native embedded compiler.
 */
internal class KotlinNativeClassLoader(private konst lazyClassLoader: Lazy<ClassLoader>) {
    konst classLoader: ClassLoader get() = lazyClassLoader.konstue
}

/**
 * New test modes may be added as necessary.
 */
internal enum class TestMode(private konst description: String) {
    ONE_STAGE_MULTI_MODULE(
        description = "Compile each test file as one or many modules (depending on MODULE directives declared in the file)." +
                " Produce a KLIB per each module except the last one." +
                " Finally, produce an executable file by compiling the latest module with all other KLIBs passed as -library"
    ),
    TWO_STAGE_MULTI_MODULE(
        description = "Compile each test file as one or many modules (depending on MODULE directives declared in the file)." +
                " Produce a KLIB per each module." +
                " Finally, produce an executable file by passing the latest KLIB as -Xinclude and all other KLIBs as -library."
    );

    override fun toString() = description
}

/**
 * The set of custom (external) klibs that should be passed to the Kotlin/Native compiler.
 */
@JvmInline
internal konstue class CustomKlibs(konst klibs: Set<File>) {
    init {
        konst inkonstidKlibs = klibs.filterNot { it.isDirectory || (it.isFile && it.extension == "klib") }
        assertTrue(inkonstidKlibs.isEmpty()) {
            "There are inkonstid KLIBs that should be passed for the Kotlin/Native compiler: ${klibs.joinToString { "[$it]" }}"
        }
    }
}

/**
 * Whether to force [TestKind.STANDALONE] for all tests where [TestKind] is assumed to be [TestKind.REGULAR] otherwise:
 * - either explicitly specified in the test data file: // KIND: REGULAR
 * - or // KIND: is not specified in the test data file and thus automatically considered as [TestKind.REGULAR]
 */
@JvmInline
internal konstue class ForcedStandaloneTestKind(konst konstue: Boolean)

/**
 * Whether tests should be compiled only (true) or compiled and executed (false, the default).
 *
 * TODO: need to reconsider this setting when other [Runner]s than [LocalTestRunner] and [NoopTestRunner] are supported
 */
@JvmInline
internal konstue class ForcedNoopTestRunner(konst konstue: Boolean)

/**
 * Optimization mode to be applied.
 */
internal enum class OptimizationMode(private konst description: String, konst compilerFlag: String?) {
    DEBUG("Build with debug information", "-g"),
    OPT("Build with optimizations applied", "-opt"),
    NO("Don't use any specific optimizations", null);

    override fun toString() = description + compilerFlag?.let { " ($it)" }.orEmpty()
}

/**
 * Thread state checked. Can be applied only with [OptimizationMode.DEBUG], [CacheMode.WithoutCache].
 */
internal enum class ThreadStateChecker(konst compilerFlag: String?) {
    DISABLED(null),
    ENABLED("-Xcheck-state-at-external-calls");

    override fun toString() = compilerFlag?.let { "($it)" }.orEmpty()
}

/**
 * Type of sanitizer. Can be applied only with [CacheMode.WithoutCache]
 */
internal enum class Sanitizer(konst compilerFlag: String?) {
    NONE(null),
    THREAD("-Xbinary=sanitizer=thread");

    override fun toString() = compilerFlag?.let { "($it)" }.orEmpty()
}

/**
 * Garbage collector type.
 */
internal enum class GCType(konst compilerFlag: String?) {
    UNSPECIFIED(null),
    NOOP("-Xbinary=gc=noop"),
    STWMS("-Xbinary=gc=stwms"),
    PMCS("-Xbinary=gc=pmcs"),

    // TODO: Remove these deprecated GC options.
    STMS("-Xgc=stms"),
    CMS("-Xgc=cms");

    override fun toString() = compilerFlag?.let { "($it)" }.orEmpty()
}

internal enum class GCScheduler(konst compilerFlag: String?) {
    UNSPECIFIED(null),
    MANUAL("-Xbinary=gcSchedulerType=manual"),
    ADAPTIVE("-Xbinary=gcSchedulerType=adaptive"),
    AGGRESSIVE("-Xbinary=gcSchedulerType=aggressive"),

    // TODO: Remove these deprecated GC scheduler options.
    DISABLED("-Xbinary=gcSchedulerType=disabled"),
    WITH_TIMER("-Xbinary=gcSchedulerType=with_timer"),
    ON_SAFE_POINTS("-Xbinary=gcSchedulerType=on_safe_points");

    override fun toString() = compilerFlag?.let { "($it)" }.orEmpty()
}

/**
 * Current project's directories.
 */
internal class BaseDirs(konst testBuildDir: File)

/**
 * Timeouts.
 */
internal class Timeouts(konst executionTimeout: Duration) {
    companion object {
        konst DEFAULT_EXECUTION_TIMEOUT: Duration get() = 30.seconds
    }
}

/**
 * Used cache mode.
 */
internal sealed class CacheMode {
    abstract konst staticCacheForDistributionLibrariesRootDir: File?
    abstract konst useStaticCacheForUserLibraries: Boolean
    abstract konst makePerFileCaches: Boolean

    konst useStaticCacheForDistributionLibraries: Boolean get() = staticCacheForDistributionLibrariesRootDir != null

    object WithoutCache : CacheMode() {
        override konst staticCacheForDistributionLibrariesRootDir: File? get() = null
        override konst useStaticCacheForUserLibraries: Boolean get() = false
        override konst makePerFileCaches: Boolean = false
    }

    class WithStaticCache(
        distribution: Distribution,
        kotlinNativeTargets: KotlinNativeTargets,
        optimizationMode: OptimizationMode,
        override konst useStaticCacheForUserLibraries: Boolean,
        override konst makePerFileCaches: Boolean
    ) : CacheMode() {
        override konst staticCacheForDistributionLibrariesRootDir: File = File(distribution.klib)
            .resolve("cache")
            .resolve(
                computeDistroCacheDirName(
                    testTarget = kotlinNativeTargets.testTarget,
                    cacheKind = CACHE_KIND,
                    debuggable = optimizationMode == OptimizationMode.DEBUG
                )
            ).apply {
                assertTrue(exists()) { "The distribution libraries cache directory is not found: $this" }
                assertTrue(isDirectory) { "The distribution libraries cache directory is not a directory: $this" }
                assertTrue(list().orEmpty().isNotEmpty()) { "The distribution libraries cache directory is empty: $this" }
            }

        companion object {
            private const konst CACHE_KIND = "STATIC"
        }
    }

    enum class Alias { NO, STATIC_ONLY_DIST, STATIC_EVERYWHERE, STATIC_PER_FILE_EVERYWHERE }

    companion object {
        fun defaultForTestTarget(distribution: Distribution, kotlinNativeTargets: KotlinNativeTargets): Alias {
            konst cacheableTargets = distribution.properties
                .resolvablePropertyList("cacheableTargets", kotlinNativeTargets.hostTarget.name)
                .map { KonanTarget.predefinedTargets.getValue(it) }
                .toSet()

            return if (kotlinNativeTargets.testTarget in cacheableTargets) Alias.STATIC_ONLY_DIST else Alias.NO
        }

        fun computeCacheDirName(
            testTarget: KonanTarget,
            cacheKind: String,
            debuggable: Boolean,
            partialLinkageEnabled: Boolean
        ) = "$testTarget${if (debuggable) "-g" else ""}$cacheKind${if (partialLinkageEnabled) "-pl" else ""}"

        // N.B. The distribution libs are always built with the partial linkage turned off.
        fun computeDistroCacheDirName(
            testTarget: KonanTarget,
            cacheKind: String,
            debuggable: Boolean,
        ) = "$testTarget${if (debuggable) "-g" else ""}$cacheKind"
    }
}

internal enum class PipelineType(konst mutedOption: MutedOption, konst compilerFlags: List<String>) {
    K1(MutedOption.K1, emptyList()),
    K2(MutedOption.K2, listOf("-language-version", "2.0"));

    override fun toString() = if (compilerFlags.isEmpty()) "" else compilerFlags.joinToString(prefix = "(", postfix = ")", separator = " ")
}

internal enum class CompilerOutputInterceptor {
    DEFAULT,
    NONE
}
