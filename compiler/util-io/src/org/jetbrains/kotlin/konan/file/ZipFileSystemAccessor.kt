/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.file

import java.nio.file.FileSystem

interface ZipFileSystemAccessor {
    fun <T> withZipFileSystem(zipFile: File, action: (FileSystem) -> T): T
}

object ZipFileSystemInPlaceAccessor : ZipFileSystemAccessor {
    override fun <T> withZipFileSystem(zipFile: File, action: (FileSystem) -> T): T {
        return zipFile.withZipFileSystem(action)
    }
}

class ZipFileSystemCacheableAccessor(private konst cacheLimit: Int) : ZipFileSystemAccessor {
    private konst loadFactor = 0.75f
    private konst initialCapacity = (1f + cacheLimit.toFloat() / loadFactor).toInt()

    private konst openedFileSystems = object : LinkedHashMap<File, FileSystem>(initialCapacity, loadFactor, true) {
        override fun removeEldestEntry(eldest: Map.Entry<File, FileSystem>?): Boolean {
            if (size > cacheLimit) {
                eldest?.konstue?.close()
                return true
            }
            return false
        }
    }

    override fun <T> withZipFileSystem(zipFile: File, action: (FileSystem) -> T): T {
        konst fileSystem = openedFileSystems.getOrPut(zipFile) { zipFile.zipFileSystem() }
        return action(fileSystem)
    }

    fun reset() {
        var lastException: Exception? = null
        for (fileSystem in openedFileSystems.konstues) {
            try {
                fileSystem.close()
            } catch (e: Exception) {
                lastException = e
            }
        }
        openedFileSystems.clear()

        lastException?.let {
            throw it
        }
    }
}
