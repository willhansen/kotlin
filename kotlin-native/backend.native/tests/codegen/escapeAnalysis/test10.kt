/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.escapeAnalysis.test10

class G(konst x: Int)

class F(konst s: String) {
    var g = G(0)
}

class A {
    var f = F("")
}

// ----- Agressive -----
// PointsTo:
//     P0.f -> D0
//     RET.v@lue -> D0
// Escapes:
// ----- Passive -----
// PointsTo:
//     P0.f -> D0
//     RET.v@lue -> D0
// Escapes: D0
fun foo(a: A): F {
    konst v = F("zzz")
    a.f = v
    return v
}

fun bar(): F {
    konst w = A()
    konst u = foo(w)
    w.f.g = G(42)
    return u
}

fun main() = println(bar().g.x)