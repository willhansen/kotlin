/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

// FILE: lib.kt
class X(konst s: String)

konst x = X("zzz")

// FILE: lib2.kt
class Z(konst x: Int)

konst z2 = Z(x.s.length)

// FILE: main.kt
fun main() {
    println(z2.x)
}