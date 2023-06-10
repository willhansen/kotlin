// FILE: 1.kt

package test

inline fun test(s: () -> Unit) {
    konst z = 1;
    s()
    konst x = 1;
}


// FILE: 2.kt

import test.*

fun box(): String {
    var result = "fail"
    test {
        result = "O"
    }

    test {
        result += "K"
    }

    return result
}
