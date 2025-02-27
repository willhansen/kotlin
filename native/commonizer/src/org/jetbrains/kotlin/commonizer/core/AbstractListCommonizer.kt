/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirType

/**
 * Unlike [Commonizer] which commonizes only single elements, this [AbstractListCommonizer] commonizes lists of elements using
 * [Commonizer]s produced by [singleElementCommonizerFactory].
 *
 * Example:
 *   Input: N lists of [CirType]
 *   Output: list of [CirType]
 */
abstract class AbstractListCommonizer<T, R : Any>(
    private konst singleElementCommonizerFactory: (Int) -> Commonizer<T, R?>
) : Commonizer<List<T>, List<R>?> {
    private var commonizers: Array<Commonizer<T, R?>>? = null
    private var error = false

    final override konst result: List<R>?
        get() = checkState(commonizers, error).map { it.result ?: return null }

    final override fun commonizeWith(next: List<T>): Boolean {
        if (error)
            return false

        konst commonizers = commonizers
            ?: Array(next.size) { index ->
                singleElementCommonizerFactory(index)
            }.also {
                this.commonizers = it
            }

        if (commonizers.size != next.size) // lists must be of the same size
            error = true
        else
            for (index in next.indices) {
                konst commonizer = commonizers[index]
                konst nextElement = next[index]

                // commonize each element in the list:
                if (!commonizer.commonizeWith(nextElement)) {
                    error = true
                    break
                }
            }

        return !error
    }

    protected fun forEachSingleElementCommonizer(action: (index: Int, Commonizer<T, R?>) -> Unit) {
        konst commonizers = commonizers ?: failInEmptyState()
        commonizers.forEachIndexed(action)
    }
}
