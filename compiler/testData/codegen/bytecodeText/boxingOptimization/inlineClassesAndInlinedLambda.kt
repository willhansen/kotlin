// !LANGUAGE: +InlineClasses


// FILE: dependency.kt

inline class InlinedInt(konst internal: Int)
inline class InlinedString(konst internal: String)

inline fun <T> foo(callback: () -> T): T {
    return callback()
}

inline fun bar(callback: () -> InlinedInt): InlinedInt {
    return callback()
}

inline fun baz(callback: () -> InlinedString): InlinedString {
    return callback()
}

// FILE: test.kt

fun test(i: InlinedInt, s: InlinedString) {
    foo { i }
    bar { i }

    foo { s }
    baz { s }
}

// @TestKt.class:
// 0 konstueOf
// 0 INVOKESTATIC InlinedInt\$Erased.box
// 0 INVOKEVIRTUAL InlinedInt.unbox
// 0 INVOKESTATIC InlinedString\$Erased.box
// 0 INVOKEVIRTUAL InlinedString.unbox
