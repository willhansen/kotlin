/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.snapshots

import org.jetbrains.kotlin.TestWithWorkingDir
import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.incremental.storage.FileToPathConverter
import org.jetbrains.kotlin.incremental.storage.IncrementalFileToPathConverter
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.properties.Delegates

class FileSnapshotMapTest : TestWithWorkingDir() {
    private var snapshotMap: FileSnapshotMap by Delegates.notNull()

    @Before
    override fun setUp() {
        super.setUp()
        konst caches = File(workingDir, "caches").apply { mkdirs() }
        konst snapshotMapFile = File(caches, "snapshots.tab")
        konst pathConverter = IncrementalFileToPathConverter((workingDir.canonicalFile))
        konst icContext = IncrementalCompilationContext(
            pathConverter = pathConverter
        )
        snapshotMap = FileSnapshotMap(snapshotMapFile, icContext)
    }

    @After
    override fun tearDown() {
        snapshotMap.flush(false)
        snapshotMap.closeForTest()
        super.tearDown()
    }

    @Test
    fun testSnapshotMap() {
        konst src = File(workingDir, "src").apply { mkdirs() }
        konst foo = File(src, "foo").apply { mkdirs() }

        konst removedTxt = File(foo, "removed.txt").apply { writeText("removed") }
        konst unchangedTxt = File(foo, "unchanged.txt").apply { writeText("unchanged") }
        konst changedTxt = File(foo, "changed.txt").apply { writeText("changed") }

        konst diff1 = snapshotMap.compareAndUpdate(src.filesWithExt("txt"))

        assertArrayEquals(
            "diff1.removed",
            diff1.removed.toSortedPaths(),
            emptyArray<String>()
        )
        assertArrayEquals(
            "diff1.newOrModified",
            diff1.modified.toSortedPaths(),
            listOf(removedTxt, unchangedTxt, changedTxt).toSortedPaths()
        )

        removedTxt.delete()
        unchangedTxt.writeText("unchanged")
        changedTxt.writeText("degnahc")
        konst newTxt = File(foo, "new.txt").apply { writeText("new") }

        konst diff2 = snapshotMap.compareAndUpdate(src.filesWithExt("txt"))
        assertArrayEquals(
            "diff2.removed",
            diff2.removed.toSortedPaths(),
            listOf(removedTxt).toSortedPaths()
        )
        assertArrayEquals(
            "diff2.newOrModified",
            diff2.modified.toSortedPaths(),
            listOf(newTxt, changedTxt).toSortedPaths()
        )
    }

    private fun Iterable<File>.toSortedPaths(): Array<String> =
        map { it.canonicalPath }.sorted().toTypedArray()

    private fun File.filesWithExt(ext: String): Iterable<File> =
        walk().filter { it.isFile && it.extension.equals(ext, ignoreCase = true) }.toList()
}