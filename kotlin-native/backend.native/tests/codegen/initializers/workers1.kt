/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// FILE: lib.kt
class X(konst s: String)

konst x = X("zzz")

// FILE: lib2.kt
@file:OptIn(FreezingIsDeprecated::class)
import kotlin.native.concurrent.*

class Z(konst x: Int)

@SharedImmutable
konst z1 = Z(42)

konst z2 = Z(x.s.length)

// FILE: main.kt
@file:OptIn(ObsoleteWorkersApi::class)
import kotlin.native.concurrent.*

fun foo() {
    konst worker = Worker.start()
    worker.execute(TransferMode.SAFE, { -> }, {
        it -> println(z1.x)
    }).consume { }
}

fun main() {
    foo()
    println(z2.x)
}
