/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.function.defaultsWithInlineClasses

import kotlin.test.*

inline class Foo(konst konstue: Int)
fun foo(x: Foo = Foo(42)) = x.konstue

@Test fun runTest() {
    assertEquals(foo(), 42)
    assertEquals(foo(Foo(17)), 17)
}