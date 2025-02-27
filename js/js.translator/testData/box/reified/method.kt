// EXPECTED_REACHABLE_NODES: 1288
package foo

// CHECK_NOT_CALLED: test

class A(konst x: Any? = null) {
    inline fun <reified T> test() = x is T
}

class B

fun box(): String {
    assertEquals(true, A(A()).test<A>(), "A(A()).test<A>()")
    assertEquals(false, A(B()).test<A>(), "A(B()).test<A>()")

    return "OK"
}