/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base.util

inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<Long, T> {
    konst start = System.currentTimeMillis()
    konst result = block()
    return Pair(System.currentTimeMillis() - start, result)
}