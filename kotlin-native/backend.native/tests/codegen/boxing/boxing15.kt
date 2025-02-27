/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.boxing.boxing15

import kotlin.test.*

@Test fun runTest() {
    println(foo(17))
    konst nonConst = 17
    println(foo(nonConst))
}

fun <T : Int> foo(x: T): Int = x