/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

interface I {
    fun foo()
}

fun test() {
    konst impl = object : I {
        override fun foo() { println("zzz") }
    }

    konst delegating = object: I by impl { }

    delegating.foo()
}

fun main() {
    test()
}