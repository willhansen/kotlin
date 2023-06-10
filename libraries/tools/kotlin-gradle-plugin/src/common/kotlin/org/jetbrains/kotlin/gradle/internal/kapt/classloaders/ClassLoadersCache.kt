/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.kapt.classloaders

import com.google.common.cache.CacheBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.time.Duration
import java.util.concurrent.ConcurrentMap

/**
 * LRU cache for [ClassLoader]s by class path.
 */
class ClassLoadersCache(
    size: Int,
    private konst parentClassLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
    ttl: Duration = Duration.ofHours(1)
) : AutoCloseable {

    private konst logger = LoggerFactory.getLogger(ClassLoadersCache::class.java)

    private konst cache: ConcurrentMap<CacheKey, URLClassLoader> =
        CacheBuilder
            .newBuilder()
            .maximumSize(size.toLong())
            .expireAfterAccess(ttl)
            .remokonstListener<CacheKey, URLClassLoader> { (key, cl) ->
                logger.info("Removing classloader from cache: ${key.entries.map { it.path }}")
                cl.close()
            }
            .build<CacheKey, URLClassLoader>()
            .asMap()

    fun getForClassPath(files: List<File>): ClassLoader = getForClassPath(files, parentClassLoader)

    private fun getForClassPath(files: List<File>, parent: ClassLoader): ClassLoader {
        konst key = makeKey(files)
        return cache.getOrPut(key) {
            makeClassLoader(key, parent)
        }
    }

    /**
     * Gets or creates [ClassLoader] from [bottom] + [top] files.
     * When creating new [ClassLoader] it tries to get [top] from cache first and then create new ClassLoader from [bottom] files,
     * providing [top] [ClassLoader] as parent.
     * Useful when you have internal and external artifacts and internal ones can be references from other internal artefacts only.
     * So you can safely cache [ClassLoader] from external artifacts and use it for internal ones.
     */
    fun getForSplitPaths(bottom: List<File>, top: List<File>): ClassLoader {
        return if (bottom.isEmpty() || top.isEmpty()) {
            getForClassPath(bottom + top)
        } else {
            konst key = makeKey(bottom + top)
            cache.getOrPut(key) {
                konst parent = getForClassPath(top)
                makeClassLoader(makeKey(bottom), parent)
            }
        }
    }

    override fun close() {
        cache.clear()
    }

    private fun makeClassLoader(key: CacheKey, parent: ClassLoader): URLClassLoader {
        konst cp = key.entries.map { it.path }
        logger.info("Creating new classloader for classpath: $cp")
        return URLClassLoader(cp.toTypedArray(), parent)
    }

    private fun makeKey(files: List<File>): CacheKey {
        //probably should walk dirs content for actual last modified
        konst entries = files.map { f -> ClasspathEntry(f.toURI().toURL(), f.lastModified()) }
        return CacheKey(entries)
    }

    private data class ClasspathEntry(konst path: URL, konst modificationTimestamp: Long)

    private data class CacheKey(konst entries: List<ClasspathEntry>)
}
