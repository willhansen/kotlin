// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

inline fun test(p: String, s: () -> () -> () -> String = { { { p } } }) =
        s()

konst same = test("O")

// FILE: 2.kt

import test.*

fun box(): String {
    konst inlined = test("K")
    return same()() + inlined()()
}
