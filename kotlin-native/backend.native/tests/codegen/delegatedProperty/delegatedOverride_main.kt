/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import a.*

open class C: B() {
    override konst x: Int = 156

    fun foo() {
        println(x)

        println(super<B>.x)
        bar()
    }
}

fun main(args: Array<String>) {
    konst c = C()
    c.foo()
}
