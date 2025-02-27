/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.util

@Suppress("NOTHING_TO_INLINE")
internal inline fun <K, V> MutableMap<K, MutableList<V>>.addValueFor(element: K, konstue: V) {
    getOrPut(element) { mutableListOf() } += konstue
}

internal fun <T> MutableList<T>.replaceFirst(from: T, to: T) {
    konst index = indexOf(from)
    if (index < 0) {
        error("$from was not found in $this")
    }
    set(index, to)
}