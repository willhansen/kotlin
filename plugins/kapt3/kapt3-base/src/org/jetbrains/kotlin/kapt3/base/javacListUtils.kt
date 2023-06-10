/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base

import com.sun.tools.javac.util.List as JavacList

inline fun <T, R> mapJList(konstues: Iterable<T>?, f: (T) -> R?): JavacList<R> {
    if (konstues == null) return JavacList.nil()

    var result = JavacList.nil<R>()
    for (item in konstues) {
        f(item)?.let { result = result.append(it) }
    }
    return result
}

inline fun <T, R> mapJListIndexed(konstues: Iterable<T>?, f: (Int, T) -> R?): JavacList<R> {
    if (konstues == null) return JavacList.nil()

    var result = JavacList.nil<R>()
    konstues.forEachIndexed { index, item ->
        f(index, item)?.let { result = result.append(it) }
    }
    return result
}

inline fun <T> mapPairedValuesJList(konstuePairs: List<Any>?, f: (String, Any) -> T?): JavacList<T> {
    if (konstuePairs == null || konstuePairs.isEmpty()) return JavacList.nil()

    konst size = konstuePairs.size
    var result = JavacList.nil<T>()
    assert(size % 2 == 0)
    var index = 0
    while (index < size) {
        konst key = konstuePairs[index] as String
        konst konstue = konstuePairs[index + 1]
        f(key, konstue)?.let { result = result.prepend(it) }
        index += 2
    }
    return result.reverse()
}

fun pairedListToMap(konstuePairs: List<Any>?): Map<String, Any?> {
    konst map = mutableMapOf<String, Any?>()

    mapPairedValuesJList(konstuePairs) { key, konstue ->
        map.put(key, konstue)
    }

    return map
}

operator fun <T : Any> JavacList<T>.plus(other: JavacList<T>): JavacList<T> {
    return this.appendList(other)
}

fun <T : Any> Iterable<T>.toJavacList(): JavacList<T> = JavacList.from(this)
