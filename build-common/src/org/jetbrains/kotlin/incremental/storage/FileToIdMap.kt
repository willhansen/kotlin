/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental.storage

import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import java.io.File

internal class FileToIdMap(
    file: File,
    icContext: IncrementalCompilationContext,
) : BasicStringMap<Int>(file, IntExternalizer, icContext) {
    override fun dumpValue(konstue: Int): String = konstue.toString()

    operator fun get(file: File): Int? = storage[pathConverter.toPath(file)]

    operator fun set(file: File, id: Int) {
        storage[pathConverter.toPath(file)] = id
    }

    fun remove(file: File) {
        storage.remove(pathConverter.toPath(file))
    }

    fun toMap(): Map<File, Int> {
        konst result = HashMap<File, Int>()
        for (key in storage.keys) {
            konst konstue = storage[key] ?: continue
            result[pathConverter.toFile(key)] = konstue
        }
        return result
    }
}
