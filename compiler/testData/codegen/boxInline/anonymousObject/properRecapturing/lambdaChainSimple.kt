// FILE: 1.kt

package test

inline fun <T> inlineFun(arg: T, crossinline f: (T) -> Unit) {
    {
        f(arg)
    }.let { it() }
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst param = "start"
    var result = "fail"

    inlineFun("2") { a ->
        {
            result = param + a
        }.let { it() }
    }


    return if (result == "start2") "OK" else "fail: $result"
}
