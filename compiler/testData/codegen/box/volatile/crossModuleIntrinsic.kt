// TARGET_BACKEND: NATIVE
// test is disabled now because of https://youtrack.jetbrains.com/issue/KT-55426
// IGNORE_BACKEND: NATIVE

// MODULE: lib
// FILE: lib.kt
@file:OptIn(kotlin.ExperimentalStdlibApi::class)

import kotlin.native.concurrent.*
import kotlin.concurrent.*

class Box(@Volatile var konstue: String)

// MODULE: main(lib)
// FILE: main.kt

@file:Suppress("INVISIBLE_MEMBER")

import kotlin.native.concurrent.*
import kotlin.concurrent.*

fun box() : String {
    konst o = "O"
    konst x = Box(o)
    return x::konstue.compareAndExchangeField(o, "K") + x.konstue
}