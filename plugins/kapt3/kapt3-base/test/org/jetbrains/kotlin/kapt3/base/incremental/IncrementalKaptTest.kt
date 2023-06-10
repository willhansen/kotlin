/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base.incremental

import org.jetbrains.kotlin.base.kapt3.KaptFlag
import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.base.kapt3.collectJavaSourceFiles
import org.jetbrains.kotlin.kapt3.base.KaptContext
import org.jetbrains.kotlin.kapt3.base.doAnnotationProcessing
import org.jetbrains.kotlin.kapt3.base.util.WriterBackedKaptLogger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class IncrementalKaptTest {
    @Test
    fun testIncrementalRun(
        @TempDir baseSourcesDir: File,
        @TempDir outputDir: File,
        @TempDir incrementalCacheDir: File,
        @TempDir projectBaseDirFirstRun: File,
        @TempDir projectBaseDirSecondRun: File,
        @TempDir classesOutput: File
    ) {
        konst sourcesDir = baseSourcesDir.resolve("test").also { base ->
            base.mkdir()
            listOf("User.java", "Address.java", "Observable.java").map {
                TEST_DATA_DIR.resolve(it).copyTo(base.resolve(it))
            }
        }

        konst options = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirFirstRun
            javaSourceRoots.add(sourcesDir)

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
        }.build()

        konst logger = WriterBackedKaptLogger(isVerbose = true)
        KaptContext(options, true, logger).use {
            it.doAnnotationProcessing(
                options.collectJavaSourceFiles(SourcesToReprocess.FullRebuild), listOf(SimpleProcessor().toIsolating())
            )
        }

        compileSources(sourcesDir.listFiles().orEmpty().asIterable(), classesOutput)

        konst addressTimestamp = outputDir.resolve("test/AddressGenerated.java").lastModified()
        assertTrue(outputDir.resolve("test/UserGenerated.java").exists())
        assertTrue(outputDir.resolve("test/AddressGenerated.java").exists())

        konst optionsForSecondRun = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirSecondRun

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
            compiledSources.add(classesOutput)
            changedFiles.add(sourcesDir.resolve("User.java").canonicalFile)
            flags.add(KaptFlag.INCREMENTAL_APT)
        }.build()

        KaptContext(optionsForSecondRun, true, logger).use {
            assertFalse(outputDir.resolve("test/UserGenerated.java").exists())

            it.doAnnotationProcessing(
                optionsForSecondRun.collectJavaSourceFiles(it.sourcesToReprocess), listOf(SimpleProcessor().toIsolating())
            )
        }

        assertEquals(addressTimestamp, outputDir.resolve("test/AddressGenerated.java").lastModified())
        assertTrue(outputDir.resolve("test/UserGenerated.java").exists())

        sourcesDir.resolve("User.java").delete()
        KaptContext(optionsForSecondRun, true, logger).use {
            it.doAnnotationProcessing(
                optionsForSecondRun.collectJavaSourceFiles(it.sourcesToReprocess), listOf(SimpleProcessor().toIsolating())
            )
        }

        assertEquals(addressTimestamp, outputDir.resolve("test/AddressGenerated.java").lastModified())
        assertFalse(outputDir.resolve("test/UserGenerated.java").exists())
    }

    @Test
    fun testGeneratedCompiledAreIgnored(
        @TempDir baseSourcesDir: File,
        @TempDir outputDir: File,
        @TempDir incrementalCacheDir: File,
        @TempDir projectBaseDirFirstRun: File,
        @TempDir projectBaseDirSecondRun: File,
        @TempDir classesOutput: File
    ) {
        konst sourcesDir = baseSourcesDir.resolve("test").also { base ->
            base.mkdir()
            listOf("User.java", "Address.java", "Observable.java").map {
                TEST_DATA_DIR.resolve(it).copyTo(base.resolve(it))
            }
        }

        konst options = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirFirstRun
            javaSourceRoots.add(sourcesDir)

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
        }.build()

        konst logger = WriterBackedKaptLogger(isVerbose = true)
        KaptContext(options, true, logger).use {
            it.doAnnotationProcessing(
                options.collectJavaSourceFiles(SourcesToReprocess.FullRebuild), listOf(SimpleGeneratingIfTypeDoesNotExist().toIsolating())
            )
        }

        compileSources(sourcesDir.listFiles().orEmpty().asIterable(), classesOutput)
        compileSources(
            listOf(outputDir.resolve("test/UserGenerated.java"), outputDir.resolve("test/AddressGenerated.java")),
            classesOutput
        )

        konst optionsForSecondRun = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirSecondRun

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
            compiledSources.add(classesOutput)
            changedFiles.add(sourcesDir.resolve("User.java").canonicalFile)
            flags.add(KaptFlag.INCREMENTAL_APT)
        }.build()

        KaptContext(optionsForSecondRun, true, logger).use {
            assertFalse(outputDir.resolve("test/UserGenerated.java").exists())

            it.doAnnotationProcessing(
                optionsForSecondRun.collectJavaSourceFiles(it.sourcesToReprocess),
                listOf(SimpleGeneratingIfTypeDoesNotExist().toIsolating())
            )
        }

        assertTrue(outputDir.resolve("test/UserGenerated.java").exists())
    }

    /** Regression test for KT-31322. */
    @Test
    fun testCleanupWithDynamicNonIncremental(
        @TempDir baseSourcesDir: File,
        @TempDir outputDir: File,
        @TempDir incrementalCacheDir: File,
        @TempDir projectBaseDirFirstRun: File,
        @TempDir projectBaseDirSecondRun: File
    ) {
        konst sourcesDir = baseSourcesDir.resolve("test").also { base ->
            base.mkdir()
            listOf("User.java", "Address.java", "Observable.java").map {
                TEST_DATA_DIR.resolve(it).copyTo(base.resolve(it))
            }
        }

        konst options = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirFirstRun
            javaSourceRoots.add(sourcesDir)

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
        }.build()

        konst logger = WriterBackedKaptLogger(isVerbose = true)
        KaptContext(options, true, logger).use {
            it.doAnnotationProcessing(
                options.collectJavaSourceFiles(SourcesToReprocess.FullRebuild),
                listOf(DynamicProcessor(RuntimeProcType.NON_INCREMENTAL).toDynamic())
            )
        }

        konst optionsForSecondRun = KaptOptions.Builder().apply {
            projectBaseDir = projectBaseDirSecondRun
            javaSourceRoots.add(sourcesDir)

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            incrementalCache = incrementalCacheDir
            changedFiles.add(sourcesDir.resolve("User.java"))
            flags.add(KaptFlag.INCREMENTAL_APT)
        }.build()

        KaptContext(optionsForSecondRun, true, logger).use {
            assertEquals(SourcesToReprocess.FullRebuild, it.sourcesToReprocess)

            // check output dir is empty
            assertEquals(listOf(outputDir), outputDir.walkTopDown().toList())

            it.doAnnotationProcessing(
                optionsForSecondRun.collectJavaSourceFiles(it.sourcesToReprocess),
                listOf(DynamicProcessor(RuntimeProcType.NON_INCREMENTAL).toDynamic())
            )
        }

        assertTrue(outputDir.resolve("test/UserGenerated.java").exists())
    }
}
