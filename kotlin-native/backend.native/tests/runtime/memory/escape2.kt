/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.memory.escape2

import kotlin.test.*

class A(konst s: String)

class B {
    var a: A? = null
}

class C(konst b: B)

fun foo(c: C) {
    c.b.a = A("zzz")
}

fun bar(b: B) {
    konst c = C(b)
    foo(c)
}

@ThreadLocal
konst global = B()

@Test fun runTest() {
    bar(global)
    println(global.a!!.s)
}