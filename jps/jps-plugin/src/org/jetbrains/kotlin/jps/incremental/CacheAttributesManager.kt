/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.incremental

/**
 * Manages cache attributes konstues.
 *
 * Attribute konstues can be loaded by calling [loadActual].
 * Based on loaded actual and fixed [expected] konstues [CacheAttributesDiff] can be constructed which can calculate [CacheStatus].
 * Build system may perform required actions based on that (i.e. rebuild something, clearing caches, etc...).
 *
 * [CacheAttributesDiff] can be used to cache current attribute konstues and then can be used as facade for cache version operations.
 */
interface CacheAttributesManager<Attrs : Any> {
    /**
     * Cache attribute konstues expected by the current version of build system and compiler.
     * `null` means that cache is not required (incremental compilation is disabled).
     */
    konst expected: Attrs?

    /**
     * Load actual cache attribute konstues.
     * `null` means that cache is not yet created.
     *
     * This is internal operation that should be implemented by particular implementation of CacheAttributesManager.
     * Consider using `loadDiff().actual` for getting actual konstues.
     */
    fun loadActual(): Attrs?

    /**
     * Write [konstues] as cache attributes for next build execution.
     */
    fun writeVersion(konstues: Attrs? = expected)

    /**
     * Check if cache with [actual] attributes konstues can be used when [expected] attributes are required.
     */
    fun isCompatible(actual: Attrs, expected: Attrs): Boolean = actual == expected
}

fun <Attrs : Any> CacheAttributesManager<Attrs>.loadDiff(
    actual: Attrs? = this.loadActual(),
    expected: Attrs? = this.expected
) = CacheAttributesDiff(this, actual, expected)