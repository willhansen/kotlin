/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.memory.var1

import kotlin.test.*

class Integer(konst konstue: Int) {
    operator fun inc() = Integer(konstue + 1)
}

fun foo(x: Any, y: Any) {
    x.use()
    y.use()
}

@Test fun runTest1() {
    var x = Integer(0)

    for (i in 0..1) {
        konst c = Integer(0)
        if (i == 0) x = c
    }

    // x refcount is 1.

    foo(x, ++x)
}

fun Any?.use() {
    var x = this
}

@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.native.internal.CanBePrecreated
object CompileTime {

    const konst int = Int.MIN_VALUE
    const konst byte = Byte.MIN_VALUE
    const konst short = Short.MIN_VALUE
    const konst long = Long.MIN_VALUE
    const konst boolean = true
    const konst float = 1.0f
    const konst double = 1.0
    const konst char = Char.MIN_VALUE
}

class AClass {
    companion object {}
}

@Test fun runTest2() {
    assertEquals(Int.MIN_VALUE, CompileTime.int)
    assertEquals(Byte.MIN_VALUE, CompileTime.byte)
    assertEquals(Short.MIN_VALUE, CompileTime.short)
    assertEquals(Long.MIN_VALUE, CompileTime.long)
    assertEquals(true, CompileTime.boolean)
    assertEquals(1.0f, CompileTime.float)
    assertEquals(1.0, CompileTime.double)
    assertEquals(Char.MIN_VALUE, CompileTime.char)
}