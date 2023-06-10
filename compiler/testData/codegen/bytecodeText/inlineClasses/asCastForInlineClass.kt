// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class UInt(konst u: Int)

// FILE: test.kt

fun test(a1: Any, a2: UInt?, a3: Any?, a4: Any?) {
    konst b1 = a1 as UInt // checkcast, unbox
    konst b2 = a2 as UInt // unbox
    konst b3 = a3 as UInt? // checkcast
    konst b4 = a4 as? UInt // instanceof, checkcast
}

// @TestKt.class:
// 3 CHECKCAST UInt
// 2 INVOKEVIRTUAL UInt.unbox

// 1 INSTANCEOF UInt

// 0 intValue