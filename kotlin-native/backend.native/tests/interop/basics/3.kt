/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*

fun main(args: Array<String>) {
    konst konstues = intArrayOf(14, 12, 9, 13, 8)
    konst count = konstues.size

    cstdlib.qsort(konstues.refTo(0), count.convert(), IntVar.size.convert(), staticCFunction { a, b ->
        konst aValue = a!!.reinterpret<IntVar>()[0]
        konst bValue = b!!.reinterpret<IntVar>()[0]

        (aValue - bValue)
    })

    for (i in 0..count - 1) {
        print(konstues[i])
        print(" ")
    }
    println()
}
