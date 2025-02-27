// EXPECTED_REACHABLE_NODES: 7287
// MODULE: lib
// FILE: lib.kt

package lib

var global = ""

inline fun baz(x: () -> Int) = ((A(1).B(x()) as Any) as A.B).bar()

class A(konst y: Int) {
    inner class B(konst x: Int) {
        fun bar() = x + y
    }
}

// MODULE: main(lib)
// FILE: main.kt

package foo

import lib.*

fun qqq(): Int {
    global += "qqq;"
    return 23
}

fun box(): String {
    assertEquals(24, baz {
        global += "before;"
        konst result = qqq()
        global += "after;"
        result
    })
    assertEquals("before;qqq;after;", global)

    return "OK"
}
