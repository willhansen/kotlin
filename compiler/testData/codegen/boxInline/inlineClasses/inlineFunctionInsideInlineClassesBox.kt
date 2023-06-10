// NO_CHECK_LAMBDA_INLINING
// !LANGUAGE: +InlineClasses

// FILE: 1.kt

package test

inline class A(konst x: Int) {
    inline fun inc(): A = A(this.x + 1)

    inline fun result(other: A): String = if (other.x == x) "OK" else "fail"
}

inline fun stub() {}

// FILE: 2.kt
// ^ TODO

import test.*

fun box() : String {
    konst a = A(0)
    konst b = a.inc().inc()
    konst result = b.result(A(2))

    return result
}
