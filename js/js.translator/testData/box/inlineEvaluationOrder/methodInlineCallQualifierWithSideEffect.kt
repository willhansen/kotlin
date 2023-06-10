// EXPECTED_REACHABLE_NODES: 1290
package foo

// Test for KT-7502
// CHECK_NOT_CALLED: plus

class A(konst konstue: Int) {
    inline fun plus(num: Int): Int = this.konstue + num
}

fun box(): String {
    assertEquals(15, A(fizz(5)).plus(buzz(10)))
    assertEquals("fizz(5);buzz(10);", pullLog())

    return "OK"
}