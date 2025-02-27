/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// FILE: lib.kt
konst y = getY()

private fun getY() = 42

fun bar(x: Int) = x == 0

// FILE: main.kt
fun foo(x: Int) {
    var i = 0
    do {
        ++i
        if (i > 0) continue
        bar(i)
    } while (i < x)
}

fun main() {
    foo(0)
    println(y)
}