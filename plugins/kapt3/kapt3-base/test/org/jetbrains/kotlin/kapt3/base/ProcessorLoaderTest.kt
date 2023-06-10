/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base

import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.kapt3.base.newClassesFolder
import org.jetbrains.kotlin.kapt3.base.newFile
import org.jetbrains.kotlin.kapt3.base.newSourcesFolder
import org.jetbrains.kotlin.kapt3.base.newStubsFolder
import org.jetbrains.kotlin.kapt3.base.util.WriterBackedKaptLogger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProcessorLoaderTest {
    @JvmField
    @TempDir
    var _rootTempDirectory: File? = null

    konst rootTempDirectory: File
        get() = _rootTempDirectory!!

    @Test
    fun testProcessorClasspath() {
        konst kaptOptions = with(KaptOptions.Builder()) {
            konst jar = rootTempDirectory.newFile("empty.jar").also {
                ZipOutputStream(it.outputStream()).use {
                    it.putNextEntry(ZipEntry("fake_entry"))
                    it.closeEntry()
                }
            }
            processingClasspath.add(jar)
            sourcesOutputDir = rootTempDirectory.newSourcesFolder()
            classesOutputDir = rootTempDirectory.newClassesFolder()
            stubsOutputDir = rootTempDirectory.newStubsFolder()
            build()
        }
        konst loadedProcessors = ProcessorLoader(kaptOptions, WriterBackedKaptLogger(false)).loadProcessors()
        assertTrue(loadedProcessors.processors.isEmpty())
    }

    @Test
    fun testProcessorUpperCaseExtensionClasspath() {
        konst kaptOptions = with(KaptOptions.Builder()) {
            konst jar = rootTempDirectory.newFile("empty.JAR").also {
                ZipOutputStream(it.outputStream()).use {
                    it.putNextEntry(ZipEntry("fake_entry"))
                    it.closeEntry()
                }
            }
            processingClasspath.add(jar)
            sourcesOutputDir = rootTempDirectory.newSourcesFolder()
            classesOutputDir = rootTempDirectory.newClassesFolder()
            stubsOutputDir = rootTempDirectory.newStubsFolder()
            build()
        }
        konst loadedProcessors = ProcessorLoader(kaptOptions, WriterBackedKaptLogger(false)).loadProcessors()
        assertTrue(loadedProcessors.processors.isEmpty())
    }

    @Test
    fun testEmptyClasspath() {
        konst kaptOptions = with(KaptOptions.Builder()) {
            sourcesOutputDir = rootTempDirectory.newSourcesFolder()
            classesOutputDir = rootTempDirectory.newClassesFolder()
            stubsOutputDir = rootTempDirectory.newStubsFolder()
            build()
        }
        konst loadedProcessors = ProcessorLoader(kaptOptions, WriterBackedKaptLogger(false)).loadProcessors()
        assertTrue(loadedProcessors.processors.isEmpty())
    }

    @Test
    fun testClasspathWithNonJars() {
        konst kaptOptions = with(KaptOptions.Builder()) {
            processingClasspath.add(rootTempDirectory.newFile("do-not-load.gz"))
            sourcesOutputDir = rootTempDirectory.newSourcesFolder()
            classesOutputDir = rootTempDirectory.newClassesFolder()
            stubsOutputDir = rootTempDirectory.newStubsFolder()
            build()
        }
        konst loadedProcessors = ProcessorLoader(kaptOptions, WriterBackedKaptLogger(false)).loadProcessors()
        assertTrue(loadedProcessors.processors.isEmpty())
    }
}
