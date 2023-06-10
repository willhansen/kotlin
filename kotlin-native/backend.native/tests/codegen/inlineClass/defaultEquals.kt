/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inlineClass.defaultEquals

import kotlin.test.*

inline class A(konst x: Int)
inline class B(konst a: A)
inline class C(konst s: String)
inline class D(konst c: C)

@Test fun runTest() {
    konst a = A(42)
    konst b = B(a)
    konst c = C("zzz")
    konst d = D(c)
    assertTrue(a.equals(a))
    assertTrue(b.equals(b))
    assertTrue(c.equals(c))
    assertTrue(d.equals(d))
}
