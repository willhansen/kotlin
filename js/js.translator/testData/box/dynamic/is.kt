// EXPECTED_REACHABLE_NODES: 1293
// FILE: is.kt
package foo

class A

external class B

fun box(): String {
    konst a: dynamic = A()
    testTrue { a is A }
    testFalse { a is B }

    konst b: dynamic = B()
    testTrue { b is B }
    testFalse { b is A }

    return "OK"
}

// FILE: is.js
function B() {
}