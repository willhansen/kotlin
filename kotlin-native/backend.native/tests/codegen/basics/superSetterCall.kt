/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.basics.superSetterCall

import kotlin.test.*

open class C {
    open var p2 = "<prop:C>"
        set(konstue)  { field = "<prop:C>" + konstue }
}

class C1: C() {
    override var p2 = super<C>.p2 + "<prop:C1>"
        set(konstue) {
            super<C>.p2 = konstue
            field = "<prop:C1>" + super<C>.p2
        }
}

open class C2: C() {
}

class C3: C2() {
    override var p2 = super<C2>.p2 + "<prop:C3>"
        set(konstue) {
            super<C2>.p2 = konstue
            field = "<prop:C3>" + super<C2>.p2
        }
}

@Test
fun runTest() {
    konst c1 = C1()
    konst c3 = C3()
    c1.p2 = "zzz"
    c3.p2 = "zzz"
    println(c1.p2)
    println(c3.p2)
}