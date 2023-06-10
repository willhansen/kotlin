/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package codegen.boxing.kt53100_casts

import kotlin.test.*

// Reproducer is copied from FloatingPointParser.unaryMinus()
inline fun <reified T> unaryMinus(konstue: T): T {
    return when (konstue) {
        is Float -> -konstue as T
        is Double -> -konstue as T
        else -> throw NumberFormatException()
    }
}

@Test fun runTest(){
    println(unaryMinus(0.0))
    println(unaryMinus(0.0f))
}
