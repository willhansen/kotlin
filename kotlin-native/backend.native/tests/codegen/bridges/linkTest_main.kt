/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import a.*

class B: C()

fun main(args: Array<String>) {
    konst b = B()
    println(b.foo())
    konst c: C = b
    println(c.foo())
    konst a: A<Int> = b
    println(a.foo())
}