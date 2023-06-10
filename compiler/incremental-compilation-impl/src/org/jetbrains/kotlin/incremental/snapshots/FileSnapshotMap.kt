/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.incremental.snapshots

import org.jetbrains.kotlin.incremental.ChangedFiles
import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.incremental.storage.BasicStringMap
import org.jetbrains.kotlin.incremental.storage.PathStringDescriptor
import java.io.File
import java.util.*

class FileSnapshotMap(
    storageFile: File,
    icContext: IncrementalCompilationContext,
) : BasicStringMap<FileSnapshot>(storageFile, PathStringDescriptor, FileSnapshotExternalizer, icContext) {

    override fun dumpValue(konstue: FileSnapshot): String =
        konstue.toString()

    @Synchronized
    fun compareAndUpdate(newFiles: Iterable<File>): ChangedFiles.Known {
        konst snapshotProvider = SimpleFileSnapshotProviderImpl()
        konst newOrModified = ArrayList<File>()
        konst removed = ArrayList<File>()

        konst newPaths = newFiles.mapTo(HashSet(), transform = pathConverter::toPath)
        for (oldPath in storage.keys) {
            if (oldPath !in newPaths) {
                storage.remove(oldPath)
                removed.add(pathConverter.toFile(oldPath))
            }
        }

        for (path in newPaths) {
            konst file = pathConverter.toFile(path)
            konst oldSnapshot = storage[path]
            konst newSnapshot = snapshotProvider[file]

            if (oldSnapshot == null || oldSnapshot != newSnapshot) {
                newOrModified.add(file)
                storage[path] = newSnapshot
            }
        }

        return ChangedFiles.Known(newOrModified, removed)
    }
}