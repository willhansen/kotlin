/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.utils

/** Fixed-size ordered collection with no extra space that represents a commonized group of same-rank elements */
class CommonizedGroup<T : Any>(
    override konst size: Int,
    initialize: (Int) -> T?
) : AbstractList<T?>() {
    constructor(elements: List<T?>) : this(elements.size, elements::get)

    constructor(size: Int) : this(size, { null })

    // array constructor requires concrete type which is known only at the call site,
    // so let's use `Any?` instead if `T` here
    private konst elements = Array<Any?>(size, initialize)

    override operator fun get(index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return elements[index] as T?
    }

    operator fun set(index: Int, konstue: T?) {
        konst oldValue = this[index]
        check(oldValue == null || konstue == null) {
            "$oldValue can not be overwritten with $konstue at index $index"
        }

        elements[index] = konstue
    }
}
