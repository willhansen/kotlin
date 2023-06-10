/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.enum.reorderedArguments

import kotlin.test.*

// Regression test for https://github.com/JetBrains/kotlin-native/issues/1779
enum class Foo(konst a: Int, konst b: Int, konst c: Int = 0) {
    A(a = 1, b = 0),
    B(b = 1, a = 0),
    C(c = 1, b = 0, a = 0),
    D(0, 0),
    E(1, 1, 1)
}

interface Base<T> {
    konst konstue: T
}

enum class Bar(override konst konstue: Foo) : Base<Foo> {
    A(Foo.A),
    B(Foo.B),
    C(Foo.C),
    D(Foo.D),
    E(Foo.E)
}

@Test fun runTest() {

    assertEquals(Foo.A.a, 1)
    assertEquals(Foo.A.b, 0)
    assertEquals(Foo.A.c, 0)

    assertEquals(Foo.B.a, 0)
    assertEquals(Foo.B.b, 1)
    assertEquals(Foo.B.c, 0)

    assertEquals(Foo.C.a, 0)
    assertEquals(Foo.C.b, 0)
    assertEquals(Foo.C.c, 1)

    assertEquals(Foo.D.a, 0)
    assertEquals(Foo.D.b, 0)
    assertEquals(Foo.D.c, 0)

    assertEquals(Foo.E.a, 1)
    assertEquals(Foo.E.b, 1)
    assertEquals(Foo.E.c, 1)

    assertEquals(Bar.A.konstue.a, 1)
    assertEquals(Bar.A.konstue.b, 0)
    assertEquals(Bar.A.konstue.c, 0)

    assertEquals(Bar.B.konstue.a, 0)
    assertEquals(Bar.B.konstue.b, 1)
    assertEquals(Bar.B.konstue.c, 0)

    assertEquals(Bar.C.konstue.a, 0)
    assertEquals(Bar.C.konstue.b, 0)
    assertEquals(Bar.C.konstue.c, 1)

    assertEquals(Bar.D.konstue.a, 0)
    assertEquals(Bar.D.konstue.b, 0)
    assertEquals(Bar.D.konstue.c, 0)

    assertEquals(Bar.E.konstue.a, 1)
    assertEquals(Bar.E.konstue.b, 1)
    assertEquals(Bar.E.konstue.c, 1)

}