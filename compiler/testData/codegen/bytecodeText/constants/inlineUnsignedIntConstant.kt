// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE

// FILE: uint.kt
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package kotlin

inline class UInt @kotlin.internal.IntrinsicConstEkonstuation constructor(konst konstue: Int)

// FILE: test.kt

const konst u = UInt(14)

fun foo() {
    u
    if (u.konstue != 14) {}
}

// @TestKt.class:
// 0 GETSTATIC