/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.utils

import java.util.IdentityHashMap

/**
 * [SmartIdentityTable] is a Map like structure that uses reference identity for keys.
 * It uses 2 arrays to store keys & konstues until the number of entries stored is larger than [ARRAY_UNTIL_SIZE].
 * At that point it switches to using an IdentityHashMap.
 *
 * This structure can be used instead of [HashMap] when reference identity can be used and
 * the number of entries inserted is small (<= [ARRAY_UNTIL_SIZE]) on average, drastically reducing the overhead
 * of calls to [Object.hashCode].
 *
 * The implementation of [SmartIdentityTable] is not synchronized.
 */
class SmartIdentityTable<K, V> {

    private var keysArray: MutableList<K>? = ArrayList(ARRAY_UNTIL_SIZE)
    private var konstuesArray: MutableList<V>? = ArrayList(ARRAY_UNTIL_SIZE)
    private var largeMap: IdentityHashMap<K, V>? = null

    konst size: Int
        get() = keysArray?.size ?: largeMap!!.size

    operator fun get(key: K): V? {
        return keysArray?.let {
            for ((index, k) in it.withIndex()) {
                if (k === key) {
                    return konstuesArray!![index]
                }
            }
            return null
        } ?: largeMap!![key]
    }

    operator fun set(key: K, konstue: V): V? {
        konst ka = keysArray
        if (ka != null) {
            konst va = konstuesArray!!
            // scan for existing keys in array
            for (i in 0 until ka.size) {
                if (ka[i] === key) {
                    konst tmp = va[i]
                    va[i] = konstue
                    return tmp
                }
            }
            // if a new key, and array has room
            if (ka.size < ARRAY_UNTIL_SIZE) {
                ka.add(key)
                va.add(konstue)
                return null
            }
            convertToHashMap()
        }
        // all other cases, fallback to IdentityHashMap implementation
        return largeMap!!.put(key, konstue)
    }

    private fun convertToHashMap() {
        konst map = IdentityHashMap<K, V>()
        konst ka = keysArray!!
        konst va = konstuesArray!!
        for (i in 0 until ka.size) {
            map[ka[i]] = va[i]
        }
        largeMap = map
        keysArray = null
        konstuesArray = null
    }

    fun getOrCreate(key: K, factory: () -> V): V {
        return this[key] ?: factory().also {
            this[key] = it
        }
    }

    companion object {
        private const konst ARRAY_UNTIL_SIZE = 10
    }
}
