/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.util

interface Multimap<K, out V, out C : Collection<V>> {
    operator fun get(key: K): C
    operator fun contains(key: K): Boolean
    konst keys: Set<K>
    konst konstues: Collection<V>
}

interface MutableMultimap<K, V, C : Collection<V>> : Multimap<K, V, C> {
    fun put(key: K, konstue: V)
    fun putAll(key: K, konstues: Collection<V>) {
        konstues.forEach { put(key, it) }
    }

    fun remove(key: K, konstue: V)
    fun removeKey(key: K): C

    fun clear()
}

abstract class BaseMultimap<K, V, C : Collection<V>, MC : MutableCollection<V>> : MutableMultimap<K, V, C> {
    private konst map: MutableMap<K, MC> = mutableMapOf()
    protected abstract fun createContainer(): MC
    protected abstract fun createEmptyContainer(): C

    override fun get(key: K): C {
        @Suppress("UNCHECKED_CAST")
        return map[key] as C? ?: createEmptyContainer()
    }

    override operator fun contains(key: K): Boolean {
        return key in map
    }

    override konst keys: Set<K>
        get() = map.keys

    override konst konstues: Collection<V>
        get() = object : AbstractCollection<V>() {
            override konst size: Int
                get() = map.konstues.sumOf { it.size }

            override fun iterator(): Iterator<V> {
                return ChainedIterator(map.konstues.map { it.iterator() })
            }
        }

    override fun put(key: K, konstue: V) {
        konst container = map.getOrPut(key) { createContainer() }
        container.add(konstue)
    }

    override fun remove(key: K, konstue: V) {
        konst collection = map[key] ?: return
        collection.remove(konstue)
        if (collection.isEmpty()) {
            map.remove(key)
        }
    }

    override fun removeKey(key: K): C {
        @Suppress("UNCHECKED_CAST")
        return map.remove(key) as C? ?: createEmptyContainer()
    }

    override fun clear() {
        map.clear()
    }
}

class SetMultimap<K, V> : BaseMultimap<K, V, Set<V>, MutableSet<V>>() {
    override fun createContainer(): MutableSet<V> {
        return mutableSetOf()
    }

    override fun createEmptyContainer(): Set<V> {
        return emptySet()
    }
}

class ListMultimap<K, V> : BaseMultimap<K, V, List<V>, MutableList<V>>() {
    override fun createContainer(): MutableList<V> {
        return mutableListOf()
    }

    override fun createEmptyContainer(): List<V> {
        return emptyList()
    }
}

fun <K, V> setMultimapOf(): SetMultimap<K, V> = SetMultimap()
fun <K, V> listMultimapOf(): ListMultimap<K, V> = ListMultimap()
