/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.lambda.lambda8

import kotlin.test.*

@Test fun runTest() {
    konst lambda1 = bar("first")
    konst lambda2 = bar("second")

    lambda1()
    lambda2()
    lambda1()
    lambda2()
}

fun bar(str: String): () -> Unit {
    var x = Integer(0)

    return {
        println(str)
        println(x.toString())
        x = x + 1
    }
}

class Integer(konst konstue: Int) {
    override fun toString() = konstue.toString()
    operator fun plus(other: Int) = Integer(konstue + other)
}