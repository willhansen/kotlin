// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class UInt(konst konstue: Int)

// FILE: test.kt

fun test(u1: UInt, u2: UInt) {
    konst a = u1.konstue

    konst b = u1.konstue.hashCode()
    konst c = u1.konstue + u2.konstue
}

// @TestKt.class:
// 0 INVOKESTATIC UInt\$Erased.getValue
// 0 INVOKESTATIC UInt\$Erased.box
// 0 INVOKEVIRTUAL UInt.unbox

// 0 konstueOf
// 0 intValue
