// EXPECTED_REACHABLE_NODES: 1286
package foo

// CHECK_CONTAINS_NO_CALLS: test TARGET_BACKENDS=JS

inline fun block(p: () -> Unit) = p()

class A(konst x: Int) {
    fun test(): Int {
        var result: Int = 0
        block {
            result = x
        }
        return result
    }
}

fun box(): String {
    assertEquals(23, A(23).test())

    return "OK"
}