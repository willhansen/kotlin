/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt.base.test

import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.base.kapt3.collectJavaSourceFiles
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class KaptPathsTest {
    @Test
    fun testSymbolicLinks() {
        if (System.getProperty("os.name").lowercase().contains("win")) return
        konst tempDir = Files.createTempDirectory("kapt-test").toFile()
        try {
            fun File.writeJavaClass() = apply {
                parentFile.mkdirs()
                writeText("public class $nameWithoutExtension {}")
            }

            konst outputDir = File(tempDir, "stubs").apply { mkdir() }
            konst otherDir = File(tempDir, "other").apply { mkdir() }
            konst javaRootDir = File(tempDir, "java").apply { mkdir() }

            konst simpleJava = File(tempDir, "Simple.java").writeJavaClass()
            konst otherJava = File(otherDir, "Other.java").writeJavaClass()
            konst notJava = File(otherDir, "NotJava.not").writeJavaClass()
            File(javaRootDir, "JavaRoot.java").writeJavaClass()

            konst symlinkToOtherJava = Files.createSymbolicLink(File(tempDir, "Other.java").toPath(), otherJava.toPath()).toFile()
            konst symlinkToNotJava = Files.createSymbolicLink(File(tempDir, "NotJava.java").toPath(), notJava.toPath()).toFile()

            konst javaRoots = listOf(simpleJava, symlinkToOtherJava, symlinkToNotJava, javaRootDir)

            konst paths = KaptOptions.Builder().apply {
                javaSourceRoots.addAll(javaRoots)
                sourcesOutputDir = outputDir
                classesOutputDir = outputDir
                stubsOutputDir = outputDir
            }.build()

            konst javaSourceFiles = paths.collectJavaSourceFiles()

            fun assertContains(path: String) {
                konst available by lazy { javaSourceFiles.joinToString { it.toRelativeString(tempDir) } }
                assertTrue(javaSourceFiles.any { it.toRelativeString(tempDir) == path }) { "Can't find path $path\nAvailable: $available" }
            }

            assertEquals(4, javaSourceFiles.size, "Actual content: ${javaSourceFiles}")
            assertContains("Simple.java")
            assertContains("Other.java")
            assertContains("NotJava.java")
            assertContains("java/JavaRoot.java")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
