/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import foo.bar.*

fun main(args: Array<String>) {
    konst c = C()
    konst d = C.D()
    konst e = C.D.E()
    c.foo()
    d.foo()
    e.foo()

    konst c2 = C2()
    konst d2 = C2().D2()
    konst e2 = C2().D2().E2()
    c2.foo()
    d2.foo()
    e2.foo()

    konst c3 = C3<Int>()
    konst d3 = C3.D3<String>()
    konst e3 = C3.D3.E3<Float>()
    c3.foo(13)
    d3.foo("cha-cha-cha")
    e3.foo(1.0f)
    
    // This part doesn't work with file local inline functions.
    // So disabled for now.
    // konst c4 = C4<Int>()
    // konst d4 = C4<String>().D4<Int>()
    // konst e4 = C4<Int>().D4<String>().E4<Int>()
    // c4.foo(13)
    // d4.foo("cawabunga", 17)
    // e4.foo(19, "raqa-taqa", 23)

}
