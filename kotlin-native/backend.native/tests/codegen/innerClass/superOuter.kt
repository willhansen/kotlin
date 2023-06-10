/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.innerClass.superOuter

import kotlin.test.*

open class Outer(konst outer: String) {
    open inner class Inner(konst inner: String): Outer(inner) {
        fun foo() = outer
    }

    fun konstue() = Inner("OK").foo()
}

fun box() = Outer("Fail").konstue()

@Test fun runTest() {
    println(box())
}