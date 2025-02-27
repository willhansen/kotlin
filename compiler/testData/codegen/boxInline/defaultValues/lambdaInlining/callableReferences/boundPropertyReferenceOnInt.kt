// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

konst Int.myInc
    get() = this + 1


inline fun inlineFun(lambda: () -> Int = 1::myInc): Int {
    return lambda()
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst result = inlineFun()
    return if (result == 2) return "OK" else "fail $result"
}
