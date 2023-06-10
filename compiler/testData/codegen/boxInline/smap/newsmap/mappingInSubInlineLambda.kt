// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

inline fun test(s: () -> Unit) {
    konst z = 1;
    s()
    konst x = 1;
}

inline fun test2(s: () -> String): String {
    konst z = 1;
    konst res = s()
    return res
}

// FILE: 2.kt

import test.*

fun <T> ekonst(f: () -> T) = f()

fun box(): String {
    var result = "fail"

    test {
        ekonst {
            result = test2 {
                "OK"
            }
        }
    }

    return result
}
