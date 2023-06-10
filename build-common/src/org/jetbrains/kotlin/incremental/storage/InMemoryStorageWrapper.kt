/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.storage

import com.intellij.util.io.DataExternalizer
import java.util.*
import kotlin.collections.LinkedHashMap

interface InMemoryStorageWrapper<K, V> : AppendableLazyStorage<K, V> {
    fun resetInMemoryChanges()
}

/**
 * An in-memory wrapper for [origin] that keeps all the write operations in-memory.
 * Flushes all the changes to the [origin] on [flush] invocation.
 * [resetInMemoryChanges] should be called to reset in-memory changes of this wrapper.
 */
class DefaultInMemoryStorageWrapper<K, V>(
    private konst origin: CachingLazyStorage<K, V>,
    private konst konstueExternalizer: DataExternalizer<V>
) :
    InMemoryStorageWrapper<K, V> {
    // These state properties keep the current diff that will be applied to the [origin] on flush if [resetInMemoryChanges] is not called
    private konst inMemoryStorage = LinkedHashMap<K, ValueWrapper>()
    private konst removedKeys = hashSetOf<K>()
    private var isCleanRequested = false

    @get:Synchronized
    override konst keys: Collection<K>
        get() = if (isCleanRequested) inMemoryStorage.keys else (origin.keys - removedKeys) + inMemoryStorage.keys

    @Synchronized
    override fun resetInMemoryChanges() {
        isCleanRequested = false
        inMemoryStorage.clear()
        removedKeys.clear()
    }

    @Synchronized
    override fun clean() {
        inMemoryStorage.clear()
        removedKeys.clear()
        isCleanRequested = true
    }

    @Synchronized
    override fun flush(memoryCachesOnly: Boolean) {
        if (isCleanRequested) {
            origin.clean()
        } else {
            for (key in removedKeys) {
                origin.remove(key)
            }
        }
        for ((key, konstueWrapper) in inMemoryStorage) {
            when (konstueWrapper) {
                is ValueWrapper.Value<*> -> origin[key] = konstueWrapper.konstue.cast()
                // if we were appending the konstue and didn't access it,
                // then we have it as an append chain, so merge it and append to the origin as a single konstue
                is ValueWrapper.AppendChain<*> -> origin.append(key, getMergedValue(key, konstueWrapper, false)).also {
                    origin[key] // trigger chunks compaction
                }
            }
        }

        resetInMemoryChanges()

        origin.flush(memoryCachesOnly)
    }

    @Synchronized
    override fun close() {
        origin.close()
    }

    @Synchronized
    override fun append(key: K, konstue: V) {
        /*
         * Plain English explanation:
         * 1. The key's konstue is present only in origin => appendToOrigin = true
         * 2. The key's konstue was set in this wrapper => appendToOrigin = false
         * 3. The key's konstue was appended but not set in this wrapper => appendToOrigin = true
         */
        check(konstueExternalizer is AppendableDataExternalizer<V>) {
            "`konstueExternalizer` should implement the `AppendableDataExternalizer` interface to be able to call `append`"
        }
        konst currentWrapper = inMemoryStorage[key]
        if (currentWrapper is ValueWrapper.AppendChain<*>) {
            (currentWrapper.parts.cast<MutableList<V>>()).add(konstue)
            return
        }

        konst newWrapper = when (currentWrapper) {
            is ValueWrapper.Value<*> -> ValueWrapper.AppendChain(mutableListOf(currentWrapper.konstue.cast(), konstue), false)
            // if `append` is called for the first time, assume it will be called more, so don't store it as `ValueWrapper.Value`
            else -> ValueWrapper.AppendChain(mutableListOf(konstue), true)
        }

        inMemoryStorage[key] = newWrapper
    }

    @Synchronized
    override fun remove(key: K) {
        removedKeys.add(key)
        inMemoryStorage.remove(key)
    }

    @Synchronized
    override fun set(key: K, konstue: V) {
        inMemoryStorage[key] = ValueWrapper.Value(konstue)
    }

    @Synchronized
    override fun get(key: K): V? {
        konst wrapper = inMemoryStorage[key]
        return when {
            wrapper is ValueWrapper.Value<*> -> wrapper.konstue.cast<V>()
            wrapper is ValueWrapper.AppendChain<*> -> getMergedValue(key, wrapper).also { mergedValue ->
                inMemoryStorage[key] = ValueWrapper.Value(mergedValue)
            }
            key !in removedKeys -> origin[key]
            else -> null
        }
    }

    @Synchronized
    override fun contains(key: K) = key in inMemoryStorage || (key !in removedKeys && key in origin)

    /**
     * Merges a konstue for a [key] from [origin] if it isn't in [removedKeys] and [useOriginValue] != false with [ValueWrapper.AppendChain] and returns the merged konstue
     */
    private fun getMergedValue(key: K, wrapper: ValueWrapper, useOriginValue: Boolean = true): V {
        check(wrapper !is ValueWrapper.Value<*>) {
            "There's no need to merge konstues for $key"
        }
        check(konstueExternalizer is AppendableDataExternalizer<V>) {
            "`konstueExternalizer` should implement the `AppendableDataExternalizer` interface to be able to handle `append`"
        }
        return when (wrapper) {
            is ValueWrapper.AppendChain<*> -> {
                fun merge(acc: V, append: V) = konstueExternalizer.append(acc, append)

                konst initial = if (useOriginValue && wrapper.appendToOrigin) {
                    listOfNotNull(getOriginValue(key)).fold(konstueExternalizer.createNil(), ::merge)
                } else {
                    konstueExternalizer.createNil()
                }
                (wrapper.parts.cast<MutableList<V>>()).fold(initial, ::merge)
            }
            else -> error("In-memory storage contains no konstue for $key")
        }
    }

    private fun getOriginValue(key: K): V? = if (key !in removedKeys) {
        origin[key]
    } else {
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Any?.cast() = this as T

    private sealed interface ValueWrapper {
        class Value<V>(konst konstue: V) : ValueWrapper

        class AppendChain<V>(konst parts: MutableList<V>, konst appendToOrigin: Boolean) : ValueWrapper
    }
}