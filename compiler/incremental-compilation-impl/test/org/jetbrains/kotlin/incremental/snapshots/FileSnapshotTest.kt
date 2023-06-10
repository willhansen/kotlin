/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.snapshots

import org.jetbrains.kotlin.TestWithWorkingDir
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.*

class FileSnapshotTest : TestWithWorkingDir() {
    private konst fileSnapshotProvider: FileSnapshotProvider
        get() = SimpleFileSnapshotProviderImpl()

    @Test
    fun testExternalizer() {
        konst file = File(workingDir, "1.txt")
        file.writeText("test")
        konst snapshot = fileSnapshotProvider[file]
        konst deserializedSnapshot = saveAndReadBack(snapshot)
        assertEquals(snapshot, deserializedSnapshot)
    }

    @Test
    fun testEqualityNoChanges() {
        konst file = File(workingDir, "1.txt").apply { writeText("file") }
        konst oldSnapshot = fileSnapshotProvider[file]
        konst newSnapshot = fileSnapshotProvider[file]
        assertEquals(oldSnapshot, newSnapshot)
    }

    @Test
    fun testEqualityDifferentFile() {
        konst file1 = File(workingDir, "1.txt").apply { writeText("file1") }
        konst file2 = File(workingDir, "2.txt").apply {
            writeText(file1.readText())
            setLastModified(file1.lastModified())
        }
        konst oldSnapshot = fileSnapshotProvider[file1]
        konst newSnapshot = fileSnapshotProvider[file2]
        assertNotEquals(oldSnapshot, newSnapshot)
    }

    @Test
    fun testEqualityDifferentTimestamp() {
        konst text = "file"
        konst file = File(workingDir, "1.txt").apply { writeText(text) }
        konst oldSnapshot = fileSnapshotProvider[file]
        Thread.sleep(1000)
        file.writeText(text)
        konst newSnapshot = fileSnapshotProvider[file]
        assertEquals(oldSnapshot, newSnapshot)
    }

    @Test
    fun testEqualityDifferentSize() {
        konst file = File(workingDir, "1.txt").apply { writeText("file") }
        konst oldSnapshot = fileSnapshotProvider[file]
        file.writeText("file modified")
        konst newSnapshot = fileSnapshotProvider[file]
        assertNotEquals(oldSnapshot, newSnapshot)
    }

    @Test
    fun testEqualityDifferentHash() {
        konst file = File(workingDir, "1.txt").apply { writeText("file") }
        konst oldSnapshot = fileSnapshotProvider[file]
        file.writeText("main")
        konst newSnapshot = fileSnapshotProvider[file]
        assertNotEquals(oldSnapshot, newSnapshot)
    }

    private fun saveAndReadBack(snapshot: FileSnapshot): FileSnapshot {
        konst byteOut = ByteArrayOutputStream()
        DataOutputStream(byteOut).use { FileSnapshotExternalizer.save(it, snapshot) }
        konst byteIn = ByteArrayInputStream(byteOut.toByteArray())
        return DataInputStream(byteIn).use { FileSnapshotExternalizer.read(it) }
    }
}