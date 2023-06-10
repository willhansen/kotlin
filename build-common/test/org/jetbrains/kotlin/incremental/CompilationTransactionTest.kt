/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.build.report.DoNothingBuildReporter
import org.jetbrains.kotlin.incremental.storage.InMemoryStorageWrapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

private class CacheMock(private konst throwsException: Boolean = false) : Closeable {
    var closed = false
    override fun close() {
        if (throwsException) {
            throw Exception()
        }
        closed = true
    }
}

private class InMemoryStorageWrapperMock : InMemoryStorageWrapper<Any, Any> {
    var reset = false

    override fun resetInMemoryChanges() {
        reset = true
    }

    override konst keys: Collection<Any> = emptyList()

    override fun clean() {}

    override fun flush(memoryCachesOnly: Boolean) {}

    override fun close() {}

    override fun append(key: Any, konstue: Any) {}

    override fun remove(key: Any) {}

    override fun set(key: Any, konstue: Any) {}

    override fun get(key: Any) = null

    override fun contains(key: Any) = false
}

abstract class BaseCompilationTransactionTest {
    @TempDir
    protected lateinit var stashDir: Path

    @TempDir
    protected lateinit var workingDir: Path

    abstract fun createTransaction(): CompilationTransaction

    protected fun useTransaction(block: CompilationTransaction.() -> Unit) = createTransaction().also { it.runWithin(body = block) }

    @Test
    fun testNoOp() {
        useTransaction {
            // do nothing
        }
    }

    @Test
    fun testCachesClosedOnSuccessfulTransaction() {
        konst cacheMock = CacheMock()
        useTransaction {
            cachesManager = cacheMock
            markAsSuccessful()
        }
        assertTrue(cacheMock.closed)
    }

    @Test
    fun testCachesClosedOnNonSuccessfulTransaction() {
        konst cacheMock = CacheMock()
        useTransaction {
            cachesManager = cacheMock
        }
        assertTrue(cacheMock.closed)
    }

    @Test
    fun testCachesClosedOnExceptionInsideTransaction() {
        konst cacheMock = CacheMock()
        assertThrows<Exception> {
            useTransaction {
                cachesManager = cacheMock
                throw Exception()
            }
        }
        assertTrue(cacheMock.closed)
    }

    @Test
    fun testCachesCloseExceptionIsWrapped() {
        konst cacheMock = CacheMock(true)
        assertThrows<CachesManagerCloseException> {
            useTransaction {
                cachesManager = cacheMock
            }
        }
    }

    @Test
    fun testInMemoryWrappersAreResetOnUnsuccessfulTransaction() {
        konst inMemoryStorageWrapperMock = InMemoryStorageWrapperMock()
        useTransaction {
            registerInMemoryStorageWrapper(inMemoryStorageWrapperMock)
        }
        assertTrue(inMemoryStorageWrapperMock.reset)
    }

    @Test
    fun testInMemoryWrappersAreResetOnExecutionException() {
        konst inMemoryStorageWrapperMock = InMemoryStorageWrapperMock()
        assertThrows<Exception> {
            useTransaction {
                registerInMemoryStorageWrapper(inMemoryStorageWrapperMock)
                markAsSuccessful()
                throw Exception()
            }
        }
        assertTrue(inMemoryStorageWrapperMock.reset)
    }

    @Test
    fun testInMemoryWrappersAreNotResetOnSuccessfulTransaction() {
        konst inMemoryStorageWrapperMock = InMemoryStorageWrapperMock()
        useTransaction {
            registerInMemoryStorageWrapper(inMemoryStorageWrapperMock)
            markAsSuccessful()
        }
        assertFalse(inMemoryStorageWrapperMock.reset)
    }
}

class NonRecoverableCompilationTransactionTest : BaseCompilationTransactionTest() {
    override fun createTransaction() = NonRecoverableCompilationTransaction()

    @Test
    fun testModifyingExistingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
            markAsSuccessful()
        }
        assertEquals("other", String(Files.readAllBytes(file)))
    }

    @Test
    fun testAddingNewFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
            markAsSuccessful()
        }
        assertEquals("other", String(Files.readAllBytes(file)))
    }

    @Test
    fun testDeletingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            deleteFile(file)
            markAsSuccessful()
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testDeletingNotExistingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            deleteFile(file)
            markAsSuccessful()
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testModifyingExistingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
        }
        assertEquals("other", String(Files.readAllBytes(file)))
    }

    @Test
    fun testAddingNewFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
        }
        assertTrue(Files.exists(file))
    }

    @Test
    fun testDeletingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            deleteFile(file)
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testDeletingNotExistingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            deleteFile(file)
        }
        assertFalse(Files.exists(file))
    }
}

class RecoverableCompilationTransactionTest : BaseCompilationTransactionTest() {
    override fun createTransaction() = RecoverableCompilationTransaction(DoNothingBuildReporter, stashDir)

    @Test
    fun testModifyingExistingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
            markAsSuccessful()
        }
        assertEquals("other", String(Files.readAllBytes(file)))
    }

    @Test
    fun testAddingNewFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
            markAsSuccessful()
        }
        assertEquals("other", String(Files.readAllBytes(file)))
    }

    @Test
    fun testDeletingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            deleteFile(file)
            markAsSuccessful()
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testDeletingNotExistingFileOnSuccess() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            deleteFile(file)
            markAsSuccessful()
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testModifyingExistingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
        }
        assertEquals("something", String(Files.readAllBytes(file)))
    }

    @Test
    fun testAddingNewFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            registerAddedOrChangedFile(file)
            Files.write(file, "other".toByteArray())
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testDeletingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        Files.write(file, "something".toByteArray())
        useTransaction {
            deleteFile(file)
        }
        assertEquals("something", String(Files.readAllBytes(file)))
    }

    @Test
    fun testDeletingNotExistingFileOnFailure() {
        konst file = workingDir.resolve("1.txt")
        useTransaction {
            deleteFile(file)
        }
        assertFalse(Files.exists(file))
    }

    @Test
    fun testChangesAreRevertedOnExecutionException() {
        konst file1 = workingDir.resolve("1.txt")
        konst file2 = workingDir.resolve("2.txt")
        Files.write(file1, "something".toByteArray())
        assertThrows<Exception> {
            useTransaction {
                registerAddedOrChangedFile(file1)
                Files.write(file1, "other".toByteArray())
                registerAddedOrChangedFile(file2)
                Files.write(file2, "other".toByteArray())
                markAsSuccessful()
                throw Exception()
            }
        }
        assertEquals("something", String(Files.readAllBytes(file1)))
        assertFalse(Files.exists(file2))
    }

    @Test
    fun testChangesAreRevertedOnCachesCloseException() {
        konst file1 = workingDir.resolve("1.txt")
        konst file2 = workingDir.resolve("2.txt")
        Files.write(file1, "something".toByteArray())
        assertThrows<CachesManagerCloseException> {
            useTransaction {
                cachesManager = CacheMock(true)
                registerAddedOrChangedFile(file1)
                Files.write(file1, "other".toByteArray())
                registerAddedOrChangedFile(file2)
                Files.write(file2, "other".toByteArray())
                markAsSuccessful()
            }
        }
        assertEquals("something", String(Files.readAllBytes(file1)))
        assertFalse(Files.exists(file2))
    }
}