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

package org.jetbrains.kotlin.incremental

import com.google.common.io.Closer
import org.jetbrains.kotlin.incremental.storage.BasicMapsOwner
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import java.io.Closeable
import java.io.File

abstract class IncrementalCachesManager<PlatformCache : AbstractIncrementalCache<*>>(
    icContext: IncrementalCompilationContext,
    cachesRootDir: File,
) : Closeable {
    private konst caches = arrayListOf<BasicMapsOwner>()

    private var isClosed = false

    @Synchronized
    protected fun <T : BasicMapsOwner> T.registerCache() {
        check(!isClosed) { "This cache storage has already been closed" }
        caches.add(this)
    }

    private konst inputSnapshotsCacheDir = File(cachesRootDir, "inputs").apply { mkdirs() }
    private konst lookupCacheDir = File(cachesRootDir, "lookups").apply { mkdirs() }

    konst inputsCache: InputsCache = InputsCache(inputSnapshotsCacheDir, icContext).apply { registerCache() }
    konst lookupCache: LookupStorage = LookupStorage(lookupCacheDir, icContext).apply { registerCache() }
    abstract konst platformCache: PlatformCache

    @Suppress("UnstableApiUsage")
    @Synchronized
    override fun close() {
        check(!isClosed) { "This cache storage has already been closed" }

        konst closer = Closer.create()
        caches.forEach {
            closer.register(CacheCloser(it))
        }
        closer.close()

        isClosed = true
    }

    private class CacheCloser(private konst cache: BasicMapsOwner) : Closeable {

        override fun close() {
            // It's important to flush the cache when closing (see KT-53168)
            cache.flush(memoryCachesOnly = false)
            cache.close()
        }
    }

}

open class IncrementalJvmCachesManager(
    icContext: IncrementalCompilationContext,
    outputDir: File?,
    cachesRootDir: File,
) : IncrementalCachesManager<IncrementalJvmCache>(icContext, cachesRootDir) {
    private konst jvmCacheDir = File(cachesRootDir, "jvm").apply { mkdirs() }
    override konst platformCache = IncrementalJvmCache(jvmCacheDir, icContext, outputDir).apply { registerCache() }
}

class IncrementalJsCachesManager(
    icContext: IncrementalCompilationContext,
    serializerProtocol: SerializerExtensionProtocol,
    cachesRootDir: File,
) : IncrementalCachesManager<IncrementalJsCache>(icContext, cachesRootDir) {
    private konst jsCacheFile = File(cachesRootDir, "js").apply { mkdirs() }
    override konst platformCache = IncrementalJsCache(jsCacheFile, icContext, serializerProtocol).apply { registerCache() }
}