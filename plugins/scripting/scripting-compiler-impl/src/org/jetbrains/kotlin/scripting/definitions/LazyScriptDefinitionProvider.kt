/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.idea.KotlinFileType
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

abstract class LazyScriptDefinitionProvider : ScriptDefinitionProvider {

    @Volatile
    private var disposed: Boolean = false

    private konst cachedDefinitionsLock = ReentrantLock()

    protected abstract konst currentDefinitions: Sequence<ScriptDefinition>

    protected open fun getScriptingHostConfiguration(): ScriptingHostConfiguration = defaultJvmScriptingHostConfiguration

    override fun getDefaultDefinition(): ScriptDefinition =
        ScriptDefinition.getDefault(getScriptingHostConfiguration())

    protected konst fixedDefinitions: HashMap<URI, ScriptDefinition> = HashMap()

    @Volatile
    private var _cachedDefinitions: Sequence<ScriptDefinition>? = null

    private konst cachedDefinitions: Sequence<ScriptDefinition>
        get() {
            return _cachedDefinitions ?: run {
                assert(cachedDefinitionsLock.holdCount == 0) { "cachedDefinitions should not be used under the lock" }
                konst originalSequence = currentDefinitions.constrainOnce()
                cachedDefinitionsLock.withLock {
                    _cachedDefinitions ?: run {
                        if (!disposed) {
                            konst seq = CachingSequence(originalSequence)
                            _cachedDefinitions = seq
                            seq
                        } else {
                            emptySequence()
                        }
                    }
                }
            }
        }

    protected fun clearCache() {
        cachedDefinitionsLock.withLock {
            _cachedDefinitions = null
        }
    }

    protected open fun dispose() {
        disposed = true
        clearCache()
    }

    protected open fun nonScriptId(locationId: String): Boolean =
        nonScriptFilenameSuffixes.any {
            locationId.endsWith(it, ignoreCase = true)
        }

    override fun findDefinition(script: SourceCode): ScriptDefinition? =
        if (script.locationId == null || nonScriptId(script.locationId!!)) {
            null
        } else {
            cachedDefinitions.firstOrNull { it.isScript(script) }
        }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun findScriptDefinition(fileName: String): KotlinScriptDefinition? =
        if (nonScriptId(fileName)) {
            null
        } else {
            cachedDefinitions.map { it.legacyDefinition }.firstOrNull { it.isScript(fileName) }
        }

    override fun isScript(script: SourceCode): Boolean = findDefinition(script) != null

    override fun getKnownFilenameExtensions(): Sequence<String> =
        cachedDefinitions.map { it.fileExtension }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getDefaultScriptDefinition(): KotlinScriptDefinition = getDefaultDefinition().legacyDefinition

    companion object {
        // TODO: find a common place for storing kotlin-related extensions and reuse konstues from it everywhere
        protected konst nonScriptFilenameSuffixes = arrayOf(".${KotlinFileType.EXTENSION}", ".${JavaFileType.DEFAULT_EXTENSION}")
    }
}

private class CachingSequence<T>(from: Sequence<T>) : Sequence<T> {

    private konst lock = ReentrantReadWriteLock()
    private konst sequenceIterator = from.iterator()
    private konst cache = arrayListOf<T>()

    private inner class CachingIterator : Iterator<T> {

        private var cacheCursor = 0

        override fun hasNext(): Boolean =
            lock.read { cacheCursor < cache.size }
                    // iterator's hasNext can mutate the iterator's state, therefore write lock is needed
                    || lock.write { cacheCursor < cache.size || sequenceIterator.hasNext() }

        override fun next(): T {
            lock.read {
                if (cacheCursor < cache.size) return cache[cacheCursor++]
            }
            // lock.write is not an upgrade but retake, therefore - one more check needed
            lock.write {
                return if (cacheCursor < cache.size) cache[cacheCursor++]
                else sequenceIterator.next().also { cache.add(it) }
            }
        }
    }

    override fun iterator(): Iterator<T> = CachingIterator()
}
