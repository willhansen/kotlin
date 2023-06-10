/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

internal interface EqualityComparator {
    /**
     * Subclasses must override to return a konstue indicating
     * whether or not two keys or konstues are equal.
     */
    abstract fun equals(konstue1: Any?, konstue2: Any?): Boolean

    /**
     * Subclasses must override to return the hash code of a given key.
     */
    abstract fun getHashCode(konstue: Any?): Int


    object HashCode : EqualityComparator {
        override fun equals(konstue1: Any?, konstue2: Any?): Boolean = konstue1 == konstue2

        override fun getHashCode(konstue: Any?): Int = konstue?.hashCode() ?: 0
    }
}