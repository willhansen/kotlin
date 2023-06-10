// FILE: 1.kt

package test

inline fun <R> map(transform: () -> R): R {
    return transform()
}

// FILE: 2.kt

import test.*

konst Int.myInc
    get() = this + 1

fun box(): String {
    konst result = map(2::myInc)
    return if (result == 3) "OK" else "fail $result"
}
