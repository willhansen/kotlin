// EXPECTED_REACHABLE_NODES: 1286
package foo

// CHECK_CONTAINS_NO_CALLS: testImplicitThis except=Unit_getInstance
// CHECK_CONTAINS_NO_CALLS: testExplicitThis except=Unit_getInstance

internal class A(var konstue: Int)

internal fun testImplicitThis(a: A, newValue: Int) {
    with (a) {
        konstue = newValue
    }
}

internal fun testExplicitThis(a: A, newValue: Int) {
    with (a) {
        this.konstue = newValue
    }
}

fun box(): String {
    konst a = A(0)
    assertEquals(0, a.konstue)

    testImplicitThis(a, 10)
    assertEquals(10, a.konstue)

    testExplicitThis(a, 20)
    assertEquals(20, a.konstue)

    return "OK"
}