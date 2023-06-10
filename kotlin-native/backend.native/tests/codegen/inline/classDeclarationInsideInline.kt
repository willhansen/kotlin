/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inline.classDeclarationInsideInline

import kotlin.test.*

fun f() {
    run {
        class Test1<T : Number, G>(konst x: T, konst y: G) {
            override fun toString() = "test1: ${x.toDouble()}"
        }

        class Test2<X>(konst a: Test1<Int, X>) {
            override fun toString() = "test2"
        }

        konst v = Test2(Test1(1, Test2(Test1(1, 3))))
        println(v.a)
        println(v.a.x)
        println(v.a.y)
    }
}

@Test fun test() {
    f()
}
