// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

konst Long.myInc
    get() = this + 1


inline fun inlineFun(lambda: () -> Long = 1L::myInc): Long {
    return lambda()
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst result = inlineFun()
    return if (result == 2L) return "OK" else "fail $result"
}
