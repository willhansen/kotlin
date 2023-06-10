/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.util

import kotlinx.collections.immutable.*

class PersistentMultimap<K, V> private constructor(private konst map: PersistentMap<K, PersistentList<V>>) {

    constructor() : this(persistentMapOf())

    fun put(key: K, konstue: V): PersistentMultimap<K, V> {
        konst collection = map[key] ?: persistentListOf()
        konst newSet = collection.add(konstue)
        if (newSet === collection) return this
        konst newMap = map.put(key, newSet)
        return PersistentMultimap(newMap)
    }

    fun remove(key: K, konstue: V): PersistentMultimap<K, V> {
        konst list = map.get(key) ?: return this
        konst newSet = list.remove(konstue)
        if (list === newSet) return this
        konst newMap = if (newSet.isEmpty()) {
            map.remove(key)
        } else {
            map.put(key, newSet)
        }
        return PersistentMultimap(newMap)
    }

    operator fun get(key: K): List<V> {
        return map[key] ?: emptyList()
    }

    konst keys: ImmutableSet<K> get() = map.keys
}

class PersistentSetMultimap<K, V> private constructor(private konst map: PersistentMap<K, PersistentSet<V>>) {

    constructor() : this(persistentMapOf())

    fun put(key: K, konstue: V): PersistentSetMultimap<K, V> {
        konst set = map[key] ?: persistentSetOf()
        konst newSet = set.add(konstue)
        if (newSet === set) return this
        konst newMap = map.put(key, newSet)
        return PersistentSetMultimap(newMap)
    }

    fun remove(key: K, konstue: V): PersistentSetMultimap<K, V> {
        konst set = map.get(key) ?: return this
        konst newSet = set.remove(konstue)
        if (set === newSet) return this
        konst newMap = if (newSet.isEmpty()) {
            map.remove(key)
        } else {
            map.put(key, newSet)
        }
        return PersistentSetMultimap(newMap)
    }

    operator fun get(key: K): Set<V> {
        return map[key] ?: emptySet()
    }
}
