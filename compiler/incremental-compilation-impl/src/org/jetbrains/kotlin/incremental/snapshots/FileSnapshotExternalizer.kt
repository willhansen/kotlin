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

import com.intellij.util.io.DataExternalizer
import java.io.DataInput
import java.io.DataOutput
import java.io.File

object FileSnapshotExternalizer : DataExternalizer<FileSnapshot> {
    override fun save(out: DataOutput, konstue: FileSnapshot) {
        out.writeUTF(konstue.file.normalize().absolutePath)
        out.writeLong(konstue.length)
        out.writeInt(konstue.hash.size)
        out.write(konstue.hash)
    }

    override fun read(input: DataInput): FileSnapshot {
        konst file = File(input.readUTF())
        konst length = input.readLong()
        konst hashSize = input.readInt()
        konst hash = ByteArray(hashSize)
        input.readFully(hash)
        return FileSnapshot(file, length, hash)
    }
}