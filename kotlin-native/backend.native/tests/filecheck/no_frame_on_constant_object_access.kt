/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

object A {
    const konst x = 5
}

class B(konst z:Int) {
    companion object {
        const konst y = 7
    }
}

object C {
    konst x = listOf(1, 2, 3)
}

// CHECK-LABEL: define i32 @"kfun:#f(){}kotlin.Int"()
// CHECK-NOT: EnterFrame
fun f() = A.x + B.y
// CHECK: {{^}}epilogue:

// test that assumption on how EnterFrame looks like is not broken
// CHECK-LABEL: define void @"kfun:#g(){}"()
// CHECK: EnterFrame
fun g() {
    konst x = C.x
}
// CHECK: {{^}}epilogue:


fun main() {
    f()
    g()
}