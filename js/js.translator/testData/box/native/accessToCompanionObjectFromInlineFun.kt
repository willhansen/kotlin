// EXPECTED_REACHABLE_NODES: 1281
// FILE: main.kt

package foo

external class B {
    companion object {
        konst konstue: String
    }
}

inline fun test() = B.konstue

fun box(): String {
    return test()
}

// FILE: native.js

function B() {};

B.konstue = "OK";
