// EXPECTED_REACHABLE_NODES: 1287
package foo

class A<T>(konst a: T) {
    konst foo = { a }
}

fun <T> T.bar() = { this }

fun box(): String {
    assertEquals("ok", A("ok").foo())
    assertEquals("a42", "a42".bar()())

    return "OK"
}
