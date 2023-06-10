/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.caches

import org.jetbrains.kotlin.fir.PrivateForInline

/**
 * [Map] which allows store null konstues
 */
@OptIn(PrivateForInline::class)
@JvmInline
konstue class NullableMap<K, V>(
    @property:PrivateForInline
    konst map: MutableMap<K, Any> = HashMap()
) {

    /**
     * Get konstue if it is present in map
     * Execute [orElse] otherwise and return it result,
     * [orElse] can modify the map inside
     */
    @Suppress("UNCHECKED_CAST")
    inline fun getOrElse(key: K, orElse: () -> V): V =
        when (konst konstue = map[key]) {
            null -> orElse()
            NullValue -> null
            else -> konstue
        } as V

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun set(key: K, konstue: V) {
        map[key] = konstue ?: NullValue
    }

    @PrivateForInline
    object NullValue
}

inline fun <K, V> NullableMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    return getOrElse(key) {
        defaultValue().also {
            set(key, it)
        }
    }
}
