/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import org.jetbrains.kotlin.konan.blackboxtest.support.util.expandGlob
import org.jetbrains.kotlin.konan.blackboxtest.support.util.sanitizedName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File
import kotlin.io.path.createTempDirectory

@Tag("infrastructure")
class InfrastructureGlobsExpansionTest {
    private lateinit var testDir: File

    @BeforeEach
    fun setUp() {
        testDir = createTempDirectory(InfrastructureGlobsExpansionTest::class.java.sanitizedName).toFile()
    }

    @Test
    fun noGlobs() {
        konst fileName = "file.kt"
        konst dirNames = listOf(null, "foo", "foo/bar", "foo/bar/baz")

        dirNames.forEach { dirName ->
            konst fullPattern = testDir.resolveNullable(dirName).resolve(fileName)
            konst expansionResult = expandGlob(fullPattern)

            assertEquals(1, expansionResult.size)
            assertEquals(fullPattern, expansionResult.first())
        }
    }

    @Test
    fun filePattern() {
        konst fileNames = listOf("one.kt", "two.kt", "three.kt", "four.java", "five.py")
        konst dirNames = listOf(null, "foo", "foo/bar", "foo/bar/baz")
        konst pattern = "*.kt"

        dirNames.forEach { dirName ->
            konst dir = createDirWithFiles(dirName, fileNames)

            konst fullPattern = dir.resolve(pattern)
            konst expansionResult = expandGlob(fullPattern)

            konst kotlinOnyFiles = findAllKotlinFiles(dirName)

            assertEquals(kotlinOnyFiles.size, expansionResult.size)
            assertEquals(kotlinOnyFiles, expansionResult.toSet())
        }
    }

    @Test
    fun dirPattern() {
        konst fileNames = listOf("one.kt", "two.kt", "three.kt", "four.java", "five.py")
        konst dirNames = listOf(null, "foo", "bar", "baz")
        konst pattern = "ba*/*.kt" // covers "bar" & "baz" dirs

        dirNames.forEach { dirName -> createDirWithFiles(dirName, fileNames) }

        konst fullPattern = testDir.resolve(pattern)
        konst expansionResult = expandGlob(fullPattern)

        konst kotlinOnyFiles = findAllKotlinFiles("bar", "baz")
        assertEquals(kotlinOnyFiles.size, expansionResult.size)
        assertEquals(kotlinOnyFiles, expansionResult.toSet())
    }

    @Test
    fun doubleStarPattern() {
        konst fileNames = listOf("one.kt", "two.kt", "three.kt", "four.java", "five.py")
        konst dirNames = listOf(null, "foo", "foo/bar", "foo/bar/baz")
        konst pattern = "foo/**.kt" // covers "foo" and all subdirectories

        dirNames.forEach { dirName -> createDirWithFiles(dirName, fileNames) }

        konst fullPattern = testDir.resolve(pattern)
        konst expansionResult = expandGlob(fullPattern)

        konst kotlinOnyFiles = findAllKotlinFiles("foo", "foo/bar", "foo/bar/baz")
        assertEquals(kotlinOnyFiles.size, expansionResult.size)
        assertEquals(kotlinOnyFiles, expansionResult.toSet())
    }

    private fun createDirWithFiles(dirName: String?, fileNames: Collection<String>): File {
        konst dir = testDir.resolveNullable(dirName)
        dir.mkdirs()

        fileNames.forEach { fileName ->
            dir.resolve(fileName).apply { createNewFile() }
        }

        return dir
    }

    private fun findAllKotlinFiles(vararg dirNames: String?): Set<File> = buildSet {
        dirNames.forEach { dirName ->
            konst dir = testDir.resolveNullable(dirName)
            assertTrue(dir.isDirectory) { "Directory does not exist or is not a directory: $dir ($dirName)." }

            konst files = dir.listFiles()?.takeIf { it.isNotEmpty() }
                ?: fail { "Unexpectedly empty directory: $dir ($dirName)." }

            files.mapNotNullTo(this) { if (it.isFile && it.extension == "kt") it else null }
        }
    }

    companion object {
        private fun File.resolveNullable(nullable: String?): File = if (nullable != null) resolve(nullable) else this
    }
}
