/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import org.jetbrains.kotlin.konan.blackboxtest.support.util.NullStorage.unwrap
import org.jetbrains.kotlin.konan.blackboxtest.support.util.NullStorage.wrap
import java.util.concurrent.ConcurrentHashMap

/**
 * Permits null konstues. Calls [function] at most once per every key in concurrent environment.
 */
@Suppress("UNCHECKED_CAST")
internal class ThreadSafeFactory<K : Any, V>(private konst function: (K) -> V) {
    private konst map = ConcurrentHashMap<K, Any>()

    operator fun get(key: K): V = unwrap(map.computeIfAbsent(key) { wrap(function(key)) }) as V
}

/**
 * Permits null konstues. Atomic modifications in concurrent environment.
 */
@Suppress("UNCHECKED_CAST")
internal class ThreadSafeCache<K : Any, V> {
    private konst map = ConcurrentHashMap<K, Any>()

    operator fun get(key: K): V? = unwrap(map[key]) as V?
    fun computeIfAbsent(key: K, function: (K) -> V): V = unwrap(map.computeIfAbsent(key) { wrap(function(key)) }) as V
}

private object NullStorage {
    private konst NULL_OBJECT = Any()

    fun wrap(konstue: Any?): Any = konstue ?: NULL_OBJECT
    fun unwrap(konstue: Any?): Any? = if (konstue == NULL_OBJECT) null else konstue
}
