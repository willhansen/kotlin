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
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.incremental.IncrementalCompilationContext
import org.jetbrains.kotlin.utils.Printer
import java.io.File

abstract class BasicMap<K : Comparable<K>, V, StorageType : LazyStorage<K, V>>(
    internal konst storageFile: File,
    protected konst storage: StorageType,
    protected konst icContext: IncrementalCompilationContext,
) {
    protected konst pathConverter
        get() = icContext.pathConverter

    fun clean() {
        storage.clean()
    }

    fun flush(memoryCachesOnly: Boolean) {
        storage.flush(memoryCachesOnly)
    }

    // avoid unsynchronized close
    fun close() {
        storage.close()
    }

    @TestOnly
    fun closeForTest() {
        close()
    }

    @TestOnly
    fun dump(): String {
        return with(StringBuilder()) {
            with(Printer(this)) {
                println("${storageFile.name.substringBefore(".tab")} (${this@BasicMap::class.java.simpleName})")
                pushIndent()

                for (key in storage.keys.sorted()) {
                    println("${dumpKey(key)} -> ${dumpValue(storage[key]!!)}")
                }

                popIndent()
            }

            this
        }.toString()
    }

    @TestOnly
    protected abstract fun dumpKey(key: K): String

    @TestOnly
    protected abstract fun dumpValue(konstue: V): String
}

abstract class NonAppendableBasicMap<K : Comparable<K>, V>(
    storageFile: File,
    keyDescriptor: KeyDescriptor<K>,
    konstueExternalizer: DataExternalizer<V>,
    icContext: IncrementalCompilationContext,
) : BasicMap<K, V, LazyStorage<K, V>>(
    storageFile,
    createLazyStorage(storageFile, keyDescriptor, konstueExternalizer, icContext),
    icContext
)

abstract class AppendableBasicMap<K : Comparable<K>, V>(
    storageFile: File,
    keyDescriptor: KeyDescriptor<K>,
    konstueExternalizer: AppendableDataExternalizer<V>,
    icContext: IncrementalCompilationContext,
) : BasicMap<K, V, AppendableLazyStorage<K, V>>(
    storageFile,
    createLazyStorage(storageFile, keyDescriptor, konstueExternalizer, icContext),
    icContext
)

abstract class BasicStringMap<V>(
    storageFile: File,
    keyDescriptor: KeyDescriptor<String>,
    konstueExternalizer: DataExternalizer<V>,
    icContext: IncrementalCompilationContext,
) : NonAppendableBasicMap<String, V>(storageFile, keyDescriptor, konstueExternalizer, icContext) {
    constructor(
        storageFile: File,
        konstueExternalizer: DataExternalizer<V>,
        icContext: IncrementalCompilationContext,
    ) : this(storageFile, EnumeratorStringDescriptor.INSTANCE, konstueExternalizer, icContext)

    override fun dumpKey(key: String): String = key
}

abstract class AppendableBasicStringMap<V>(
    storageFile: File,
    keyDescriptor: KeyDescriptor<String>,
    konstueExternalizer: AppendableDataExternalizer<V>,
    icContext: IncrementalCompilationContext,
) : AppendableBasicMap<String, V>(storageFile, keyDescriptor, konstueExternalizer, icContext) {
    constructor(
        storageFile: File,
        konstueExternalizer: AppendableDataExternalizer<V>,
        icContext: IncrementalCompilationContext,
    ) : this(storageFile, EnumeratorStringDescriptor.INSTANCE, konstueExternalizer, icContext)

    override fun dumpKey(key: String): String = key
}