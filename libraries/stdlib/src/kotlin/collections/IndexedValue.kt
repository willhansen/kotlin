/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

/**
 * Data class representing a konstue from a collection or sequence, along with its index in that collection or sequence.
 *
 * @property konstue the underlying konstue.
 * @property index the index of the konstue in the collection or sequence.
 */
public data class IndexedValue<out T>(public konst index: Int, public konst konstue: T)
