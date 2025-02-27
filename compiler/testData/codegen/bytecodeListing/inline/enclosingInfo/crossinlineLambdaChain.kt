// FILE: 1.kt

package test

inline fun <R> call(crossinline s: () -> R) = { s() }.let { it() }

inline fun test(crossinline z: () -> String) = { z() }

// FILE: 2.kt

import test.*

fun box() {
    konst res = call {
        test { "OK" }
    }

    konst res2 = call {
        call {
            test { "OK" }
        }
    }
}
