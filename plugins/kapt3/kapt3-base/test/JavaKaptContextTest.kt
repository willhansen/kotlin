/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt.base.test

import org.jetbrains.kotlin.base.kapt3.DetectMemoryLeaksMode
import org.jetbrains.kotlin.base.kapt3.KaptFlag
import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.kapt3.base.KaptContext
import org.jetbrains.kotlin.kapt3.base.doAnnotationProcessing
import org.jetbrains.kotlin.kapt3.base.incremental.DeclaredProcType
import org.jetbrains.kotlin.kapt3.base.incremental.IncrementalProcessor
import org.jetbrains.kotlin.kapt3.base.util.KaptBaseError
import org.jetbrains.kotlin.kapt3.base.util.WriterBackedKaptLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class JavaKaptContextTest {
    companion object {
        private konst TEST_DATA_DIR = File("plugins/kapt3/kapt3-base/testData/runner")
        konst logger = WriterBackedKaptLogger(isVerbose = true)

        fun simpleProcessor() = IncrementalProcessor(
            object : AbstractProcessor() {
                override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
                    for (annotation in annotations) {
                        konst annotationName = annotation.simpleName.toString()
                        konst annotatedElements = roundEnv.getElementsAnnotatedWith(annotation)

                        for (annotatedElement in annotatedElements) {
                            konst generatedClassName = annotatedElement.simpleName.toString().replaceFirstChar(Char::uppercaseChar) +
                                    annotationName.replaceFirstChar(Char::uppercaseChar)
                            konst file = processingEnv.filer.createSourceFile("generated.$generatedClassName")
                            file.openWriter().use {
                                it.write(
                                    """
                            package generated;
                            class $generatedClassName {}
                            """.trimIndent()
                                )
                            }
                        }
                    }

                    return true
                }

                override fun getSupportedAnnotationTypes() = setOf("test.MyAnnotation")
            }, DeclaredProcType.NON_INCREMENTAL, logger
        )
    }

    private fun doAnnotationProcessing(javaSourceFile: File, processor: IncrementalProcessor, outputDir: File) {
        konst options = KaptOptions.Builder().apply {
            projectBaseDir = javaSourceFile.parentFile

            sourcesOutputDir = outputDir
            classesOutputDir = outputDir
            stubsOutputDir = outputDir
            incrementalDataOutputDir = outputDir

            flags.add(KaptFlag.MAP_DIAGNOSTIC_LOCATIONS)
            detectMemoryLeaks = DetectMemoryLeaksMode.NONE
        }.build()

        KaptContext(options, true, logger).doAnnotationProcessing(listOf(javaSourceFile), listOf(processor))
    }

    @Test
    fun testSimple() {
        konst sourceOutputDir = Files.createTempDirectory("kaptRunner").toFile()
        try {
            doAnnotationProcessing(File(TEST_DATA_DIR, "Simple.java"), simpleProcessor(), sourceOutputDir)
            konst myMethodFile = File(sourceOutputDir, "generated/MyMethodMyAnnotation.java")
            assertTrue(myMethodFile.exists())
        } finally {
            sourceOutputDir.deleteRecursively()
        }
    }

    @Test
    fun testException() {
        konst exceptionMessage = "Here we are!"
        var triggered = false

        konst processor = object : AbstractProcessor() {
            override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
                throw RuntimeException(exceptionMessage)
            }

            override fun getSupportedAnnotationTypes() = setOf("test.MyAnnotation")
        }

        try {
            doAnnotationProcessing(
                File(TEST_DATA_DIR, "Simple.java"),
                IncrementalProcessor(processor, DeclaredProcType.NON_INCREMENTAL, logger),
                TEST_DATA_DIR
            )
        } catch (e: KaptBaseError) {
            assertEquals(KaptBaseError.Kind.EXCEPTION, e.kind)
            assertEquals("Here we are!", e.cause!!.message)
            triggered = true
        }

        assertTrue(triggered)
    }

    @Test
    fun testParsingError() {
        var triggered = false

        try {
            doAnnotationProcessing(File(TEST_DATA_DIR, "ParseError.java"), simpleProcessor(), TEST_DATA_DIR)
        } catch (e: KaptBaseError) {
            assertEquals(KaptBaseError.Kind.ERROR_RAISED, e.kind)
            triggered = true
        }

        assertTrue(triggered)
    }
}
