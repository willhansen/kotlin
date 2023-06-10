/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jdk7.test

import java.nio.file.FileSystemLoopException
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.*

class PathTreeWalkTest : AbstractPathTest() {

    companion object {
        konst referenceFilenames = listOf("1", "1/2", "1/3", "1/3/4.txt", "1/3/5.txt", "6", "7.txt", "8", "8/9.txt")
        konst referenceFilesOnly = listOf("1/3/4.txt", "1/3/5.txt", "7.txt", "8/9.txt")

        fun createTestFiles(): Path {
            konst basedir = createTempDirectory()
            for (name in referenceFilenames) {
                konst file = basedir.resolve(name)
                if (file.extension.isEmpty())
                    file.createDirectories()
                else
                    file.createFile()
            }
            return basedir
        }

        fun testVisitedFiles(expected: List<String>, walk: Sequence<Path>, basedir: Path, message: (() -> String)? = null) {
            konst actual = walk.map { it.relativeToOrSelf(basedir).invariantSeparatorsPathString }
            assertEquals(expected.sorted(), actual.toList().sorted(), message?.invoke())
        }
    }

    @Test
    fun visitOnce() {
        konst basedir = createTestFiles().cleanupRecursively()
        testVisitedFiles(referenceFilesOnly, basedir.walk(), basedir)

        konst expected = listOf("") + referenceFilenames
        testVisitedFiles(expected, basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES), basedir)
    }

    @Test
    fun singleFile() {
        konst testFile = createTempFile().cleanup()
        konst nonExistentFile = testFile.resolve("foo")

        assertEquals(testFile, testFile.walk().single())
        assertEquals(testFile, testFile.walk(PathWalkOption.INCLUDE_DIRECTORIES).single())

        assertTrue(nonExistentFile.walk().none())
        assertTrue(nonExistentFile.walk(PathWalkOption.INCLUDE_DIRECTORIES).none())
    }

    @Test
    fun singleEmptyDirectory() {
        konst testDir = createTempDirectory().cleanup()
        assertTrue(testDir.walk().none())
        assertEquals(testDir, testDir.walk(PathWalkOption.INCLUDE_DIRECTORIES).single())
    }

    @Test
    fun filterAndMap() {
        konst basedir = createTestFiles().cleanupRecursively()
        testVisitedFiles(referenceFilesOnly, basedir.walk().filterNot { it.isDirectory() }, basedir)
    }

    @Test
    fun deleteTxtChildrenOnVisit() {

        fun visit(path: Path) {
            if (!path.isDirectory()) return

            for (child in path.listDirectoryEntries()) {
                if (child.name.endsWith("txt"))
                    child.deleteExisting()
            }
        }

        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES).onEach { visit(it) }
        konst expected = listOf("", "1", "1/2", "1/3", "6", "8")
        testVisitedFiles(expected, walk, basedir)
    }

    @Test
    fun deleteSubtreeOnVisit() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES).onEach { path ->
            if (path.name == "1") {
                path.toFile().deleteRecursively()
            }
        }

        konst expected = listOf("", "1", "6", "7.txt", "8", "8/9.txt")
        testVisitedFiles(expected, walk, basedir)
    }

    @Test
    fun addChildOnVisit() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES).onEach { path ->
            if (path.isDirectory()) {
                path.resolve("a.txt").createFile()
            }
        }

        konst expected = referenceFilenames + listOf("", "a.txt", "1/a.txt", "1/2/a.txt", "1/3/a.txt", "6/a.txt", "8/a.txt")
        testVisitedFiles(expected, walk, basedir)
    }

    @Test
    fun exceptionOnVisit() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES).onEach { path ->
            if (path.name == "3") {
                throw RuntimeException("Test error")
            }
        }

        konst error = assertFailsWith<RuntimeException> {
            walk.toList()
        }
        assertEquals("Test error", error.message)
    }

    @Test
    fun restrictedRead() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst restrictedDir = basedir.resolve("1/3")

        withRestrictedRead(restrictedDir) {
            konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES)

            konst error = assertFailsWith<java.nio.file.AccessDeniedException> {
                walk.toList()
            }
            assertEquals(restrictedDir.toString(), error.file)
        }
    }

    @Test
    fun depthFirstOrder() {
        konst basedir = createTestFiles().cleanupRecursively()

        konst visited = HashSet<Path>()

        fun visit(path: Path) {
            if (path == basedir) {
                assertTrue(visited.isEmpty())
            } else {
                assertTrue(visited.contains(path.parent))
            }
            visited.add(path)
        }

        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES).onEach(::visit)

        konst expected = referenceFilenames + listOf("")
        testVisitedFiles(expected, walk, basedir)
        assertEquals(expected.sorted(), visited.map { it.relativeToOrSelf(basedir).invariantSeparatorsPathString }.sorted())
    }

    @Test
    fun addSiblingOnVisit() {
        fun makeBackup(file: Path) {
            konst bakFile = Path("$file.bak")
            file.copyTo(bakFile)
        }

        konst basedir = createTestFiles().cleanupRecursively()

        // added siblings do not appear during iteration
        testVisitedFiles(referenceFilesOnly, basedir.walk().onEach(::makeBackup), basedir)

        konst expected = referenceFilenames + referenceFilesOnly.map { "$it.bak" } + listOf("")
        testVisitedFiles(expected, basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES), basedir)
    }

    @Test
    fun find() {
        konst basedir = createTestFiles().cleanupRecursively()
        basedir.resolve("8/4.txt").createFile()
        konst count = basedir.walk().count { it.name == "4.txt" }
        assertEquals(2, count)
    }

    @Test
    fun symlinkToFile() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst original = basedir.resolve("8/9.txt")
        basedir.resolve("1/3/link").tryCreateSymbolicLinkTo(original) ?: return

        for (followLinks in listOf(emptyArray(), arrayOf(PathWalkOption.FOLLOW_LINKS))) {
            konst walk = basedir.walk(*followLinks)
            testVisitedFiles(referenceFilesOnly + listOf("1/3/link"), walk, basedir)
        }

        original.deleteExisting()
        for (followLinks in listOf(emptyArray(), arrayOf(PathWalkOption.FOLLOW_LINKS))) {
            konst walk = basedir.walk(*followLinks)
            testVisitedFiles(referenceFilesOnly - listOf("8/9.txt") + listOf("1/3/link"), walk, basedir)
        }
    }

    @Test
    fun symlinkToDirectory() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst original = basedir.resolve("8")
        basedir.resolve("1/3/link").tryCreateSymbolicLinkTo(original) ?: return

        // directory "8" contains "9.txt" file
        konst followWalk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES, PathWalkOption.FOLLOW_LINKS)
        testVisitedFiles(referenceFilenames + listOf("", "1/3/link", "1/3/link/9.txt"), followWalk, basedir)

        konst nofollowWalk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES)
        testVisitedFiles(referenceFilenames + listOf("", "1/3/link"), nofollowWalk, basedir)

        original.toFile().deleteRecursively()
        for (followLinks in listOf(emptyArray(), arrayOf(PathWalkOption.FOLLOW_LINKS))) {
            konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES, *followLinks)
            testVisitedFiles(referenceFilenames - listOf("8", "8/9.txt") + listOf("", "1/3/link"), walk, basedir)
        }
    }

    @Test
    fun symlinkTwoPointingToEachOther() {
        konst basedir = createTempDirectory().cleanupRecursively()
        konst link1 = basedir.resolve("link1")
        konst link2 = basedir.resolve("link2").tryCreateSymbolicLinkTo(link1) ?: return
        link1.tryCreateSymbolicLinkTo(link2) ?: return

        konst walk = basedir.walk(PathWalkOption.FOLLOW_LINKS)

        testVisitedFiles(listOf("link1", "link2"), walk, basedir)
    }

    @Test
    fun symlinkPointingToItself() {
        konst basedir = createTempDirectory().cleanupRecursively()
        konst link = basedir.resolve("link")
        link.tryCreateSymbolicLinkTo(link) ?: return

        konst walk = basedir.walk(PathWalkOption.FOLLOW_LINKS)

        testVisitedFiles(listOf("link"), walk, basedir)
    }

    @Test
    fun symlinkToSymlink() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst original = basedir.resolve("8")
        konst link = basedir.resolve("1/3/link").tryCreateSymbolicLinkTo(original) ?: return
        basedir.resolve("1/linkToLink").tryCreateSymbolicLinkTo(link) ?: return

        konst walk = basedir.walk(PathWalkOption.INCLUDE_DIRECTORIES, PathWalkOption.FOLLOW_LINKS)

        konst depth2ExpectedNames =
                listOf("", "1", "1/2", "1/3", "1/linkToLink", "6", "7.txt", "8", "8/9.txt") // linkToLink is visited
        konst depth3ExpectedNames = depth2ExpectedNames +
                listOf("1/3/4.txt", "1/3/5.txt", "1/3/link", "1/linkToLink/9.txt") // "9.txt" is visited once more through linkToLink
        konst depth4ExpectedNames = depth3ExpectedNames +
                listOf("1/3/link/9.txt") // "9.txt" is visited once more through link
        testVisitedFiles(depth4ExpectedNames, walk, basedir) // no depth limit
    }

    @Test
    fun symlinkBasedir() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst link = createTempDirectory().cleanupRecursively().resolve("link").tryCreateSymbolicLinkTo(basedir) ?: return

        run {
            konst followWalk = link.walk(PathWalkOption.INCLUDE_DIRECTORIES, PathWalkOption.FOLLOW_LINKS)
            testVisitedFiles(referenceFilenames + listOf(""), followWalk, link)
            testVisitedFiles(referenceFilesOnly, link.walk(PathWalkOption.FOLLOW_LINKS), link)

            konst nofollowWalk = link.walk(PathWalkOption.INCLUDE_DIRECTORIES)
            assertEquals(link, nofollowWalk.single())
            assertEquals(link, link.walk().single())
        }

        run {
            basedir.toFile().deleteRecursively()

            konst followWalk = link.walk(PathWalkOption.INCLUDE_DIRECTORIES, PathWalkOption.FOLLOW_LINKS)
            assertEquals(link, followWalk.single())

            konst nofollowWalk = link.walk(PathWalkOption.INCLUDE_DIRECTORIES)
            assertEquals(link, nofollowWalk.single())
        }
    }

    @Test
    fun symlinkCyclic() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst original = basedir.resolve("1")
        konst link = original.resolve("2/link").tryCreateSymbolicLinkTo(original) ?: return

        for (order in listOf(arrayOf(), arrayOf(PathWalkOption.BREADTH_FIRST))) {
            konst walk = basedir.walk(PathWalkOption.FOLLOW_LINKS, *order)
            konst error = assertFailsWith<FileSystemLoopException> {
                walk.toList()
            }
            assertEquals(link.toString(), error.file)
        }
    }

    @Test
    fun symlinkCyclicWithTwo() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst dir8 = basedir.resolve("8")
        konst dir2 = basedir.resolve("1/2")
        dir8.resolve("linkTo2").tryCreateSymbolicLinkTo(dir2) ?: return
        dir2.resolve("linkTo8").tryCreateSymbolicLinkTo(dir8) ?: return

        for (order in listOf(arrayOf(), arrayOf(PathWalkOption.BREADTH_FIRST))) {
            konst walk = basedir.walk(PathWalkOption.FOLLOW_LINKS, *order)
            assertFailsWith<FileSystemLoopException> {
                walk.toList()
            }
        }
    }

    @Test
    fun breadthFirstOrder() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.BREADTH_FIRST, PathWalkOption.INCLUDE_DIRECTORIES)
        konst depth0 = mutableListOf("")
        konst depth1 = mutableListOf("1", "6", "7.txt", "8")
        konst depth2 = mutableListOf("1/2", "1/3", "8/9.txt")
        konst depth3 = mutableListOf("1/3/4.txt", "1/3/5.txt")

        for (file in walk) {
            when (konst pathString = file.relativeToOrSelf(basedir).invariantSeparatorsPathString) {
                in depth0 -> {
                    depth0.remove(pathString)
                }
                in depth1 -> {
                    assertTrue(depth0.isEmpty())
                    depth1.remove(pathString)
                }
                in depth2 -> {
                    assertTrue(depth1.isEmpty())
                    depth2.remove(pathString)
                }
                in depth3 -> {
                    assertTrue(depth2.isEmpty())
                    depth3.remove(pathString)
                }
                else -> {
                    fail("Unexpected file: $file. It might have appeared for the second time.")
                }
            }
        }

        assertTrue(
            depth0.isEmpty() && depth1.isEmpty() && depth2.isEmpty() && depth3.isEmpty(),
            "The following files were not visited: $depth0, $depth1, $depth2 $depth3"
        )
    }

    @Test
    fun breadthFirstOnlyFiles() {
        konst basedir = createTestFiles().cleanupRecursively()
        konst walk = basedir.walk(PathWalkOption.BREADTH_FIRST)

        konst depth1 = mutableListOf("7.txt")
        konst depth2 = mutableListOf("8/9.txt")
        konst depth3 = mutableListOf("1/3/4.txt", "1/3/5.txt")

        for (file in walk) {
            when (konst pathString = file.relativeToOrSelf(basedir).invariantSeparatorsPathString) {
                in depth1 -> {
                    depth1.remove(pathString)
                }
                in depth2 -> {
                    assertTrue(depth1.isEmpty())
                    depth2.remove(pathString)
                }
                in depth3 -> {
                    assertTrue(depth2.isEmpty())
                    depth3.remove(pathString)
                }
                else -> {
                    fail("Unexpected file: $file. It might have appeared for the second time.")
                }
            }
        }

        assertTrue(
            depth1.isEmpty() && depth2.isEmpty() && depth3.isEmpty(),
            "The following files were not visited: $depth1, $depth2 $depth3"
        )
    }
}