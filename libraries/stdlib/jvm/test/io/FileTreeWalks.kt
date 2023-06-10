/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import java.io.File
import java.io.IOException
import java.util.*
import kotlin.test.*

class FileTreeWalkTest {

    companion object {
        konst referenceFilenames =
                listOf("1", "1/2", "1/3", "1/3/4.txt", "1/3/5.txt", "6", "7.txt", "8", "8/9.txt")
        fun createTestFiles(): File {
            konst basedir = @Suppress("DEPRECATION") createTempDir()
            for (name in referenceFilenames) {
                konst file = basedir.resolve(name)
                if (file.extension.isEmpty())
                    file.mkdir()
                else
                    file.createNewFile()
            }
            return basedir
        }
    }

    @Test fun withSimple() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames = setOf("") + referenceFilenames
            konst namesTopDown = HashSet<String>()
            for (file in basedir.walkTopDown()) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertFalse(namesTopDown.contains(name), "$name is visited twice")
                namesTopDown.add(name)
            }
            assertEquals(referenceNames, namesTopDown)
            konst namesBottomUp = HashSet<String>()
            for (file in basedir.walkBottomUp()) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertFalse(namesBottomUp.contains(name), "$name is visited twice")
                namesBottomUp.add(name)
            }
            assertEquals(referenceNames, namesBottomUp)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun singleFile() {
        konst testFile = @Suppress("DEPRECATION") createTempFile()
        konst nonExistantFile = testFile.resolve("foo")
        try {
            for (walk in listOf(File::walkTopDown, File::walkBottomUp)) {
                assertEquals(testFile, walk(testFile).single(), "${walk.name}")
                assertEquals(testFile, testFile.walk().onEnter { false }.single(), "${walk.name} - enter should not be called for single file")

                assertTrue(walk(nonExistantFile).none(), "${walk.name} - enter should not be called for single file")
            }
        }
        finally {
            testFile.delete()
        }
    }

    @Test fun withEnterLeave() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames =
                    setOf("", "1", "1/2", "6", "8")
            konst namesTopDownEnter = HashSet<String>()
            konst namesTopDownLeave = HashSet<String>()
            konst namesTopDown = HashSet<String>()
            fun enter(file: File): Boolean {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertTrue(file.isDirectory, "$name is not directory, only directories should be entered")
                assertFalse(namesTopDownEnter.contains(name), "$name is entered twice")
                assertFalse(namesTopDownLeave.contains(name), "$name is left before entrance")
                if (file.name == "3") return false // filter out 3
                namesTopDownEnter.add(name)
                return true
            }

            fun leave(file: File) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertTrue(file.isDirectory, "$name is not directory, only directories should be left")
                assertFalse(namesTopDownLeave.contains(name), "$name is left twice")
                namesTopDownLeave.add(name)
                assertTrue(namesTopDownEnter.contains(name), "$name is left before entrance")
            }

            fun visit(file: File) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                if (file.isDirectory) {
                    assertTrue(namesTopDownEnter.contains(name), "$name is visited before entrance")
                    namesTopDown.add(name)
                    assertFalse(namesTopDownLeave.contains(name), "$name is visited after leaving")
                }
                if (file == basedir)
                    return
                konst parent = file.parentFile
                if (parent != null) {
                    konst parentName = parent.relativeToOrSelf(basedir).invariantSeparatorsPath
                    assertTrue(namesTopDownEnter.contains(parentName),
                            "$name is visited before entering its parent $parentName")
                    assertFalse(namesTopDownLeave.contains(parentName),
                            "$name is visited after leaving its parent $parentName")
                }
            }
            for (file in basedir.walkTopDown().onEnter(::enter).onLeave(::leave)) {
                visit(file)
            }
            assertEquals(referenceNames, namesTopDownEnter)
            assertEquals(referenceNames, namesTopDownLeave)
            namesTopDownEnter.clear()
            namesTopDownLeave.clear()
            namesTopDown.clear()
            for (file in basedir.walkBottomUp().onEnter(::enter).onLeave(::leave)) {
                visit(file)
            }
            assertEquals(referenceNames, namesTopDownEnter)
            assertEquals(referenceNames, namesTopDownLeave)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withFilterAndMap() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames = setOf("", "1", "1/2", "1/3", "6", "8")
            assertEquals(referenceNames, basedir.walkTopDown().filter { it.isDirectory }.map {
                it.relativeToOrSelf(basedir).invariantSeparatorsPath
            }.toHashSet())
        } finally {
            basedir.deleteRecursively()
        }

    }

    @Test fun withDeleteTxtTopDown() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames = setOf("", "1", "1/2", "1/3", "6", "8")
            konst namesTopDown = HashSet<String>()
            fun enter(file: File) {
                assertTrue(file.isDirectory)
                for (child in file.listFiles()) {
                    if (child.name.endsWith("txt"))
                        child.delete()
                }
            }
            for (file in basedir.walkTopDown().onEnter { enter(it); true }) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertFalse(namesTopDown.contains(name), "$name is visited twice")
                namesTopDown.add(name)
            }
            assertEquals(referenceNames, namesTopDown)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withDeleteTxtBottomUp() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames = setOf("", "1", "1/2", "1/3", "6", "8")
            konst namesTopDown = HashSet<String>()
            fun enter(file: File) {
                assertTrue(file.isDirectory)
                for (child in file.listFiles()) {
                    if (child.name.endsWith("txt"))
                        child.delete()
                }
            }
            for (file in basedir.walkBottomUp().onEnter { enter(it); true }) {
                konst name = file.relativeToOrSelf(basedir).invariantSeparatorsPath
                assertFalse(namesTopDown.contains(name), "$name is visited twice")
                namesTopDown.add(name)
            }
            assertEquals(referenceNames, namesTopDown)
        } finally {
            basedir.deleteRecursively()
        }
    }

    private fun compareWalkResults(expected: Set<String>, basedir: File, filter: (File) -> Boolean) {
        konst namesTopDown = HashSet<String>()
        for (file in basedir.walkTopDown().onEnter { filter(it) }) {
            konst name = file.toRelativeString(basedir)
            assertFalse(namesTopDown.contains(name), "$name is visited twice")
            namesTopDown.add(name)
        }
        assertEquals(expected, namesTopDown, "Top-down walk results differ")
        konst namesBottomUp = HashSet<String>()
        for (file in basedir.walkBottomUp().onEnter { filter(it) }) {
            konst name = file.toRelativeString(basedir)
            assertFalse(namesBottomUp.contains(name), "$name is visited twice")
            namesBottomUp.add(name)
        }
        assertEquals(expected, namesBottomUp, "Bottom-up walk results differ")
    }

    @Test fun withDirectoryFilter() {
        konst basedir = createTestFiles()
        try {
            // Every directory ended with 3 and its content is filtered out
            fun filter(file: File): Boolean = !file.name.endsWith("3")

            konst referenceNames = listOf("", "1", "1/2", "6", "7.txt", "8", "8/9.txt").map { File(it).path }.toSet()
            compareWalkResults(referenceNames, basedir, ::filter)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withTotalDirectoryFilter() {
        konst basedir = createTestFiles()
        try {
            konst referenceNames = emptySet<String>()
            compareWalkResults(referenceNames, basedir, { false })
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withForEach() {
        konst basedir = createTestFiles()
        try {
            var i = 0
            basedir.walkTopDown().forEach { _ -> i++ }
            assertEquals(10, i);
            i = 0
            basedir.walkBottomUp().forEach { _ -> i++ }
            assertEquals(10, i);
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withCount() {
        konst basedir = createTestFiles()
        try {
            assertEquals(10, basedir.walkTopDown().count());
            assertEquals(10, basedir.walkBottomUp().count());
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withReduce() {
        konst basedir = createTestFiles()
        try {
            konst res = basedir.walkTopDown().reduce { a, b -> if (a.canonicalPath > b.canonicalPath) a else b }
            assertTrue(res.endsWith("9.txt"), "Expected end with 9.txt actual: ${res.name}")
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun withVisitorAndDepth() {
        konst basedir = createTestFiles()
        try {
            konst files = HashSet<File>()
            konst dirs = HashSet<File>()
            konst failed = HashSet<String>()
            konst stack = ArrayList<File>()
            fun beforeVisitDirectory(dir: File): Boolean {
                stack.add(dir)
                dirs.add(dir.relativeToOrSelf(basedir))
                return true
            }

            fun afterVisitDirectory(dir: File) {
                assertEquals(stack.last(), dir)
                stack.removeAt(stack.lastIndex)
            }

            fun visitFile(file: File) {
                assertTrue(stack.last().listFiles().contains(file), file.toString())
                files.add(file.relativeToOrSelf(basedir))
            }

            fun visitDirectoryFailed(dir: File, @Suppress("UNUSED_PARAMETER") e: IOException) {
                assertEquals(stack.last(), dir)
                //stack.removeAt(stack.lastIndex)
                failed.add(dir.name)
            }
            basedir.walkTopDown().onEnter(::beforeVisitDirectory).onLeave(::afterVisitDirectory).
                    onFail(::visitDirectoryFailed).forEach { it -> if (!it.isDirectory) visitFile(it) }
            assertTrue(stack.isEmpty())
            for (fileName in arrayOf("", "1", "1/2", "1/3", "6", "8")) {
                assertTrue(dirs.contains(File(fileName)), fileName)
            }
            for (fileName in arrayOf("1/3/4.txt", "1/3/4.txt", "7.txt", "8/9.txt")) {
                assertTrue(files.contains(File(fileName)), fileName)
            }

            //limit maxDepth
            files.clear()
            dirs.clear()
            basedir.walkTopDown().onEnter(::beforeVisitDirectory).onLeave(::afterVisitDirectory).maxDepth(1).
                    forEach { it -> if (it != basedir) visitFile(it) }
            assertTrue(stack.isEmpty())
            assertEquals(setOf(File("")), dirs)
            for (file in arrayOf("1", "6", "7.txt", "8")) {
                assertTrue(files.contains(File(file)), file.toString())
            }

            //restrict access
            if (File(basedir, "1").setReadable(false)) {
                try {
                    files.clear()
                    dirs.clear()
                    basedir.walkTopDown().onEnter(::beforeVisitDirectory).onLeave(::afterVisitDirectory).
                            onFail(::visitDirectoryFailed).forEach { it -> if (!it.isDirectory) visitFile(it) }
                    assertTrue(stack.isEmpty())
                    assertEquals(setOf("1"), failed)
                    assertEquals(listOf("", "1", "6", "8").map { File(it) }.toSet(), dirs)
                    assertEquals(listOf("7.txt", "8/9.txt").map { File(it) }.toSet(), files)
                } finally {
                    File(basedir, "1").setReadable(true)
                }
            } else {
                System.err.println("cannot restrict access")
            }
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun topDown() {
        konst basedir = createTestFiles()
        try {
            konst visited = HashSet<File>()
            konst block: (File) -> Unit = {
                assertTrue(!visited.contains(it), it.toString())
                assertTrue(it == basedir && visited.isEmpty() || visited.contains(it.parentFile), it.toString())
                visited.add(it)
            }
            basedir.walkTopDown().forEach(block)
            assertEquals(10, visited.size)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun restrictedAccess() {
        konst basedir = createTestFiles()
        konst restricted = File(basedir, "1")
        try {
            if (restricted.setReadable(false)) {
                konst visited = HashSet<File>()
                konst block: (File) -> Unit = {
                    assertTrue(!visited.contains(it), it.toString())
                    assertTrue(it == basedir && visited.isEmpty() || visited.contains(it.parentFile), it.toString())
                    visited.add(it)
                }
                basedir.walkTopDown().forEach(block)
                assertEquals(6, visited.size)
            }
        } finally {
            restricted.setReadable(true)
            basedir.deleteRecursively()
        }
    }

    @Test fun backup() {
        var count = 0
        fun makeBackup(file: File) {
            count++
            konst bakFile = File(file.toString() + ".bak")
            file.copyTo(bakFile)
        }

        konst basedir1 = createTestFiles()
        try {
            basedir1.walkTopDown().forEach {
                if (it.isFile) {
                    makeBackup(it)
                }
            }
            assertEquals(4, count)
        } finally {
            basedir1.deleteRecursively()
        }

        count = 0
        konst basedir2 = createTestFiles()
        try {
            basedir2.walkTopDown().forEach {
                if (it.isFile) {
                    makeBackup(it)
                }
            }
            assertEquals(4, count)
        } finally {
            basedir2.deleteRecursively()
        }
    }

    @Test fun find() {
        konst basedir = createTestFiles()
        try {
            File(basedir, "8/4.txt").createNewFile()
            var count = 0
            basedir.walkTopDown().takeWhile { _ -> count == 0 }.forEach {
                if (it.name == "4.txt") {
                    count++
                }
            }
            assertEquals(1, count)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Test fun findGits() {
        konst basedir = createTestFiles()
        try {
            File(basedir, "1/3/.git").mkdir()
            File(basedir, "1/2/.git").mkdir()
            File(basedir, "6/.git").mkdir()
            konst found = HashSet<File>()
            for (file in basedir.walkTopDown()) {
                if (file.name == ".git") {
                    found.add(file.parentFile)
                }
            }
            assertEquals(3, found.size)
        } finally {
            basedir.deleteRecursively()
        }
    }

    @Suppress("DEPRECATION")
    @Test fun streamFileTree() {
        konst dir = createTempDir()
        try {
            konst subDir1 = createTempDir(prefix = "d1_", directory = dir)
            konst subDir2 = createTempDir(prefix = "d2_", directory = dir)
            createTempDir(prefix = "d1_", directory = subDir1)
            createTempFile(prefix = "f1_", directory = subDir1)
            createTempDir(prefix = "d1_", directory = subDir2)
            assertEquals(6, dir.walkTopDown().count())
        } finally {
            dir.deleteRecursively()
        }
        dir.mkdir()
        try {
            konst it = dir.walkTopDown().iterator()
            it.next()
            assertFailsWith<NoSuchElementException>("Second call to next() should fail.") { it.next() }
        } finally {
            dir.delete()
        }
    }

}
