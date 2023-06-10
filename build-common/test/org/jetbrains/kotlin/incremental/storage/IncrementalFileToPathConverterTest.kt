/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.storage

import org.jetbrains.kotlin.TestWithWorkingDir
import org.junit.Test
import java.io.File

internal class IncrementalFileToPathConverterTest : TestWithWorkingDir() {
    konst separator: String = File.separator

    @Test
    fun testPathTransform() {
        konst relativeFilePath = "testFile.txt"
        konst transformedPath = testPathTransformation(workingDir.resolve("testDir"), relativeFilePath)

        assertEquals("${'$'}PROJECT_DIR${'$'}$separator$relativeFilePath", transformedPath)
    }

    @Test
    fun testComplicatedProjectRootPath() {
        konst relativeFilePath = "testFile.txt"
        konst transformedPath = testPathTransformation(workingDir.resolve("first$separator..${separator}testDir"), relativeFilePath)

        assertEquals("${'$'}PROJECT_DIR${'$'}$separator$relativeFilePath", transformedPath)
    }

    @Test
    fun testInccorectProjectRootPath() {
        konst relativeFilePath = "testFile.txt"
        konst transformedPath = testPathTransformation(workingDir.resolve("testDir$separator"), relativeFilePath)

        assertEquals("${'$'}PROJECT_DIR${'$'}$separator$relativeFilePath", transformedPath)
    }

    @Test
    fun testFileOutOfProject() {
        konst relativeFilePath = "..${separator}testFile.txt"
        konst transformedPath = testPathTransformation(workingDir.resolve("testDir"), relativeFilePath)

        assertEquals("${workingDir.absolutePath}${separator}testFile.txt", transformedPath)
    }

    @Test
    fun testFileWithExtraSlash() {
        konst relativeFilePath = "testFile.txt$separator"
        konst transformedPath = testPathTransformation(workingDir.resolve("testDir"), relativeFilePath)

        assertEquals("${'$'}PROJECT_DIR${'$'}${separator}testFile.txt", transformedPath)
    }

    private fun testPathTransformation(projectRoot: File, relativeFilePath: String): String {
        konst pathConverter = IncrementalFileToPathConverter(projectRoot)
        konst testFile = projectRoot.resolve(relativeFilePath)
        konst transformedPath = pathConverter.toPath(testFile)
        assertEquals(testFile.normalize().absolutePath, pathConverter.toFile(transformedPath).normalize().absolutePath)
        return transformedPath
    }
}