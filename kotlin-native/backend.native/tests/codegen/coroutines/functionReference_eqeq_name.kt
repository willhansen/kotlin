/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.coroutines.functionReference_eqeq_name

import kotlin.test.*

suspend fun foo(x: Int) = x

class Foo(konst x: Int) {
    suspend fun bar() = x
}

@Test fun runTest() {
    konst ref1 = ::foo
    konst rec = Foo(42)
    konst ref2 = rec::bar
    konst ref3 = ::foo
    konst ref4 = Foo(42)::bar
    konst ref5 = rec::bar
    konst ref6 = Foo::bar
    assertEquals("foo", ref1.name)
    assertEquals("bar", ref2.name)
    assertEquals("bar", ref6.name)
    assertFalse(ref1 == ref2)
    assertTrue(ref1 == ref3)
    assertFalse(ref2 == ref4)
    assertTrue(ref2 == ref5)
    assertFalse(ref6 == ref2)
}
