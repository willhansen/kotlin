// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class UInt(private konst u: Int)

// FILE: test.kt

fun test(x: UInt?, y: UInt) {
    konst a = run {
        x!!
    }

    konst b = run {
        y
    }
}

// @TestKt.class:
// 0 INVOKESTATIC UInt\$Erased.box
// 0 INVOKESTATIC UInt\.box
// 1 INVOKEVIRTUAL UInt.unbox

// 0 konstueOf
// 0 intValue