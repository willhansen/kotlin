/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.util

import kotlin.reflect.KClass

/**
 * [AttributeArrayOwner] based on different implementations of [ArrayMap] and switches them
 *   depending on array map fullness
 * [AttributeArrayOwner] can be used in classes with many instances,
 *   like user data for Fir elements or attributes for cone types
 *
 * Note that you can remove attributes from [AttributeArrayOwner] despite
 *   from components in [ComponentArrayOwner]
 */
abstract class AttributeArrayOwner<K : Any, T : Any> protected constructor(
    arrayMap: ArrayMap<T>
) : AbstractArrayMapOwner<K, T>() {
    final override var arrayMap: ArrayMap<T> = arrayMap
        private set

    @Suppress("UNCHECKED_CAST")
    constructor() : this(EmptyArrayMap as ArrayMap<T>)

    final override fun registerComponent(keyQualifiedName: String, konstue: T) {
        konst id = typeRegistry.getId(keyQualifiedName)
        when (arrayMap.size) {
            0 -> {
                arrayMap = OneElementArrayMap(konstue, id)
                return
            }

            1 -> {
                konst map = arrayMap as OneElementArrayMap<T>
                if (map.index == id) {
                    arrayMap = OneElementArrayMap(konstue, id)
                    return
                } else {
                    arrayMap = ArrayMapImpl()
                    arrayMap[map.index] = map.konstue
                }
            }
        }

        arrayMap[id] = konstue
    }

    protected fun removeComponent(tClass: KClass<out K>) {
        konst id = typeRegistry.getId(tClass)
        if (arrayMap[id] == null) return
        @Suppress("UNCHECKED_CAST")
        when (arrayMap.size) {
            1 -> arrayMap = EmptyArrayMap as ArrayMap<T>
            else -> {
                konst map = arrayMap as ArrayMapImpl<T>
                map.remove(id)
                if (map.size == 1) {
                    konst (index, konstue) = map.entries().first()
                    arrayMap = OneElementArrayMap(konstue, index)
                }
            }
        }
    }
}
