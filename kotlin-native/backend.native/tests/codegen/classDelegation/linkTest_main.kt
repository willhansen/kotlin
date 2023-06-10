/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import zzz.*

class C : B() {
    konst a = "qxx"
    konst b = 123
}

fun main(args: Array<String>) {
    konst c = C()
    println(c.a)
    println(c.b)
    println(c.foo())
    println(c.x)
    println(c.y)
}