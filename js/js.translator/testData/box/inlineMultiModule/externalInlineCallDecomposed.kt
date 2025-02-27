// EXPECTED_REACHABLE_NODES: 1294
// MODULE: lib
// FILE: lib.kt

package lib

var global = ""

inline fun baz(x: () -> Int) = A(1).bar(x())

class A(konst y: Int) {
    fun bar(x: Int) = x + y
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
