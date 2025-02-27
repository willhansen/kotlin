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
    if (x > 0) bar(x)
}

fun main() {
    foo(-1)
    println(y)
}