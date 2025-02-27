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

import com.intellij.util.io.DataExternalizer
import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.name.FqName
import java.io.DataInput
import java.io.DataOutput
import java.io.File

internal data class ICClassesAttributes(konst isSealed: Boolean)

internal object ICClassesAttributesExternalizer : DataExternalizer<ICClassesAttributes> {
    override fun read(input: DataInput): ICClassesAttributes {
        return ICClassesAttributes(input.readBoolean())
    }

    override fun save(output: DataOutput, konstue: ICClassesAttributes) {
        output.writeBoolean(konstue.isSealed)
    }
}

internal open class ClassAttributesMap(
    storageFile: File,
    icContext: IncrementalCompilationContext,
) : BasicStringMap<ICClassesAttributes>(storageFile, ICClassesAttributesExternalizer, icContext) {
    override fun dumpValue(konstue: ICClassesAttributes): String = konstue.toString()

    operator fun set(key: FqName, konstue: ICClassesAttributes) {
        storage[key.asString()] = konstue
    }

    operator fun get(key: FqName): ICClassesAttributes? = storage[key.asString()]

    fun remove(key: FqName) {
        storage.remove(key.asString())
    }
}
