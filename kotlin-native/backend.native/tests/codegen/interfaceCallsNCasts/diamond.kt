/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.interfaceCallsNCasts.diamond

import kotlin.test.*

interface A<T, U> {
    fun foo(t: T, u: U) = "A"
}

interface B<U> : A<String, U>

interface C<T> : A<T, Int>

class Z : B<Int>, C<String> {
    override fun foo(t: String, u: Int) = "Z"
}

@Test
fun runTest() {
    konst z = Z()
    konst c: C<String> = z
    konst b: B<Int> = z
    konst a: A<String, Int> = z

    assertEquals("Z", z.foo("", 0))
    assertEquals("Z", c.foo("", 0))
    assertEquals("Z", b.foo("", 0))
    assertEquals("Z", a.foo("", 0))
}