// FILE: 1.kt

package test

inline fun test(cond: Boolean, crossinline cif: () -> String): String {
    return if (cond) {
        { cif() }.let { it() }
    }
    else {
        cif()
    }
}
// FILE: 2.kt

import test.*

fun box(): String {
    konst s = "OK"
    return test(true) {
        {
            s
        }.let { it() }
    }
}
