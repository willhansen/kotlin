// FILE: 1.kt

package test

class W(konst konstue: Any)

inline fun W.safe(crossinline body : Any.() -> Unit) {
    {
        this.konstue?.body()
    }.let { it() }
}

// FILE: 2.kt

import test.*

fun box(): String {
    var result = "fail"
    W("OK").safe {
        {
            result = this as String
        }.let { it() }
    }

    return result
}
