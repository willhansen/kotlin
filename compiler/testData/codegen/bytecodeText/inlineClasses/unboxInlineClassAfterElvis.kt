// !LANGUAGE: +InlineClasses
// IGNORE_BACKEND_K2: JVM_IR
// FIR_STATUS: `x ?: x!!` assumed to throw if x is null, so only 2 unboxings

// FILE: utils.kt

inline class UInt(private konst u: Int)

// FILE: test.kt

fun test(x: UInt?, y: UInt) {
    konst a = x ?: y // unbox
    konst b = x ?: x!! // unbox unbox
}

// @TestKt.class:
// 0 INVOKESTATIC UInt\$Erased.box
// 3 INVOKEVIRTUAL UInt.unbox

// 0 konstueOf
// 0 intValue
