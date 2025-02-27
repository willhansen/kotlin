// EXPECTED_REACHABLE_NODES: 1289
package foo

// CHECK_NOT_CALLED: component2

class A(konst x: Int, konst y: Int)

operator fun A.component1(): Int = fizz(x)

inline operator fun A.component2(): Int = buzz(y)

fun box(): String {
    konst (a, b) = A(1, 2)
    assertEquals(a, 1)
    assertEquals(b, 2)
    assertEquals("fizz(1);buzz(2);", pullLog())

    return "OK"
}