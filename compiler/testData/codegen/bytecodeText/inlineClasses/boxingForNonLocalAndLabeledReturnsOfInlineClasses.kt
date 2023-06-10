// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class ULong(konst l: Long)

// FILE: test.kt

fun nonLocal(): ULong? {
    konst u = ULong(0)

    run {
        return u // box
    }

    TODO()
}

fun foo(): Boolean = true

fun labeled(): ULong? {
    konst u = ULong(0)
    return run {
        if (foo()) return@run u // box
        u // box
    }
}

// @TestKt.class:
// 0 INVOKEVIRTUAL ULong.unbox

// 0 konstueOf
// 0 intValue

// JVM_TEMPLATES:
// 3 INVOKESTATIC ULong\.box

// JVM_IR_TEMPLATES:
// 2 INVOKESTATIC ULong\.box
