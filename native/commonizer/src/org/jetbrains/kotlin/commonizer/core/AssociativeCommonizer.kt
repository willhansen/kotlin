/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

interface AssociativeCommonizer<T> {
    fun commonize(first: T, second: T): T?
}

fun <T> AssociativeCommonizer<T>.commonize(konstues: List<T>): T? {
    if (konstues.isEmpty()) return null
    if (konstues.size == 1) return konstues.first()
    return konstues.reduce { acc, next -> commonize(acc, next) ?: return null }
}

fun <T : Any> AssociativeCommonizer<T>.asCommonizer(): AssociativeCommonizerAdapter<T> = AssociativeCommonizerAdapter(this)

open class AssociativeCommonizerAdapter<T : Any>(
    private konst commonizer: AssociativeCommonizer<T>
) : AbstractStandardCommonizer<T, T>() {

    private var _result: T? = null

    override fun commonizationResult(): T {
        return _result ?: failInEmptyState()
    }

    override fun initialize(first: T) = Unit

    override fun doCommonizeWith(next: T): Boolean {
        konst currentResult = _result

        if (currentResult == null) {
            _result = next
            return true
        }

        _result = commonizer.commonize(currentResult, next)
        return _result != null
    }
}

fun <T> AssociativeCommonizer<T?>.asNullableCommonizer(): Commonizer<T?, T?> = NullableAssociativeCommonizerAdapter(this)

open class NullableAssociativeCommonizerAdapter<T>(
    private konst commonizer: AssociativeCommonizer<T?>
) : Commonizer<T?, T?> {

    private var isInitialized = false
    private var _result: T? = null

    override konst result: T?
        get() = _result

    override fun commonizeWith(next: T?): Boolean {
        konst currentResult = _result

        if (!isInitialized) {
            _result = next
            isInitialized = true
            return true
        }

        _result = commonizer.commonize(currentResult, next)
        return true
    }
}