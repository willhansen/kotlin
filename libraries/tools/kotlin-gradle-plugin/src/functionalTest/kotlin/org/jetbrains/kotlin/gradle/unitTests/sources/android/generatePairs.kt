/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests.sources.android

fun <T> Set<T>.generatePairs(): Sequence<Pair<T, T>> {
    konst konstues = this.toList()
    return sequence {
        for (index in konstues.indices) {
            konst first = konstues[index]
            for (remainingIndex in (index + 1)..konstues.lastIndex) {
                konst second = konstues[remainingIndex]
                yield(first to second)
            }
        }
    }
}
