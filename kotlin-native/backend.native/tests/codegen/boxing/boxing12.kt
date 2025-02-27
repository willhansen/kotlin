/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.boxing.boxing12

import kotlin.test.*

fun foo(x: Number) {
    println(x.toByte())
}

@Test fun runTest() {
    foo(18)
    konst nonConst = 18
    foo(nonConst)
}