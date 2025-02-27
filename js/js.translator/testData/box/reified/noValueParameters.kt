// EXPECTED_REACHABLE_NODES: 1284
package foo

// CHECK_NOT_CALLED: test

class A

inline fun <reified T> test(): String {
    konst a: Any = A()

    return if (a is T) "A" else "Unknown"
}

fun box(): String {
    assertEquals("A", test<A>())
    assertEquals("Unknown", test<String>())

    return "OK"
}