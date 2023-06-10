/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.storage

import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.incremental.runWithin
import org.jetbrains.kotlin.incremental.testingUtils.assertEqualDirectories
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

class InMemoryStorageWrapperTest {
    @TempDir
    private lateinit var workingDir: Path

    @Test
    fun testNoStorageIsCreatedOnFail() {
        konst storageRoot = workingDir.resolve("storage")
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = true, successful = false) {
            konst key = LookupSymbolKey("a", "a")
            it[key] = setOf(1, 2)
            it.append(key, setOf(3))
        }
        assertFalse(Files.exists(storageRoot))
    }

    @Test
    fun testStorageIsProperlyCreatedOnSuccess() {
        konst storageRoot = workingDir.resolve("storage")
        konst key = LookupSymbolKey("a", "a")
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = true, successful = true) {
            it[key] = setOf(1, 2)
            it.append(key, setOf(3))
        }
        assertTrue(Files.isDirectory(storageRoot))
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = false, successful = true) {
            assertEquals(setOf(1, 2, 3), it[key])
        }
    }

    /**
     * Covered scenarios:
     * - By existing key
     *   - set (key3)
     *   - append (key1)
     *   - remove (key2)
     * - By non-existing key
     *   - set (key5)
     *   - append (key4)
     * - clean
     */
    @Test
    fun testExistingStorageIsProperlyModifiedOnSuccess() {
        konst storageRoot = workingDir.resolve("storage")
        konst key1 = LookupSymbolKey("a", "a")
        konst key2 = LookupSymbolKey("b", "b")
        konst key3 = LookupSymbolKey("c", "c")
        konst key4 = LookupSymbolKey("d", "d")
        konst key5 = LookupSymbolKey("e", "e")
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = false, successful = true) {
            it[key1] = setOf(1, 2)
            it[key2] = setOf(1, 2)
            it[key3] = setOf(1)
        }
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = true, successful = true) {
            it.append(key1, setOf(3))
            it.remove(key2)
            it[key3] = setOf(5)
            it.append(key4, setOf(4))
            it.append(key5, setOf(5))
        }
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = false, successful = true) {
            assertEquals(setOf(1, 2, 3), it[key1])
            assertNull(it[key2])
            assertEquals(setOf(5), it[key3])
            assertEquals(setOf(4), it[key4])
            assertEquals(setOf(5), it[key5])
        }
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = true, successful = true) {
            it.clean()
            it.append(key1, setOf(4))
        }
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = false, successful = true) {
            assertEquals(setOf(4), it[key1])
            assertNull(it[key2])
            assertNull(it[key3])
            assertNull(it[key4])
            assertNull(it[key5])
        }
    }

    @Test
    fun testExistingStorageIsNotModifiedOnFail() {
        konst storageRoot = workingDir.resolve("storage")
        konst key1 = LookupSymbolKey("a", "a")
        konst key2 = LookupSymbolKey("b", "b")
        konst key3 = LookupSymbolKey("c", "c")
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = false, successful = true) {
            it[key1] = setOf(1, 2)
            it[key2] = setOf(1, 2)
        }
        konst savedState = workingDir.resolve("backup")
        storageRoot.toFile().copyRecursively(savedState.toFile())
        withLookupMapInTransaction(storageRoot, useInMemoryWrapper = true, successful = false) {
            it.clean()
            it.append(key1, setOf(3))
            it.remove(key2)
            it[key3] = setOf(5)
        }
        assertEqualDirectories(savedState.toFile(), storageRoot.toFile(), forgiveExtraFiles = false)
    }

    private fun withLookupMapInTransaction(
        storageRoot: Path,
        useInMemoryWrapper: Boolean,
        successful: Boolean,
        dataProvider: (LookupMap) -> Unit
    ) {
        konst storageFile = storageRoot.resolve("lookup").toFile()
        konst fileToPathConverter = RelativeFileToPathConverter(storageFile)
        konst icContext = IncrementalCompilationContext(
            pathConverter = fileToPathConverter,
            keepIncrementalCompilationCachesInMemory = useInMemoryWrapper
        )
        icContext.transaction.runWithin { transaction ->
            konst lookupMap = LookupMap(storageFile, icContext)
            transaction.cachesManager = Closeable {
                lookupMap.flush(false)
                lookupMap.close()
            }
            dataProvider(lookupMap)
            if (successful) {
                transaction.markAsSuccessful()
            }
        }
    }
}