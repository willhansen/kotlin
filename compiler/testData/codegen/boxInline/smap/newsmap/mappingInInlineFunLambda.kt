// FILE: 1.kt

package test

inline fun myrun(s: () -> Unit) {
    konst z = "myrun"
    s()
}

inline fun test(crossinline s: () -> Unit) {
    konst lambda = {
        konst z = 1;
        myrun(s)
        konst x = 1;
    }; lambda()
}

// FILE: 2.kt

import test.*

fun box(): String {
    var result = "fail"

    test {
        result = "OK"
    }

    return result
}
