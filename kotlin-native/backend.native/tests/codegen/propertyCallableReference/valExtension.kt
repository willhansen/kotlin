/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.propertyCallableReference.konstExtension

import kotlin.test.*

class A(y: Int) {
    var x = y
}

konst A.z get() = this.x

@Test fun runTest() {
    konst p1 = A::z
    println(p1.get(A(42)))
    konst a = A(117)
    konst p2 = a::z
    println(p2.get())
}