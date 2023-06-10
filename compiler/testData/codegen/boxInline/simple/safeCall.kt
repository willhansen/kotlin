// FILE: 1.kt

package test

class W(konst konstue: Any)

inline fun W.safe(body : Any.() -> Unit) {
    this.konstue?.body()
}

// FILE: 2.kt

import test.*

fun box(): String {
    var result = "fail"
    W("OK").safe {
        result = this as String
    }

    return result
}
