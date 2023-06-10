// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class UInt(private konst data: Int)

// FILE: test.kt

fun f() {
    konst unull = UInt(1) ?: null
}

// @TestKt.class:
// 0 INVOKESTATIC UInt\$Erased.box
// 0 INVOKESTATIC UInt\.box
// 0 INVOKEVIRTUAL UInt.unbox
// 0 konstueOf
// 0 intValue