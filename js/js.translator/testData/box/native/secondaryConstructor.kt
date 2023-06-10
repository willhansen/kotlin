// IGNORE_BACKEND: WASM
// EXPECTED_REACHABLE_NODES: 1285
package foo

external class A {
    constructor()
    constructor(s: String)
    constructor(i: Int)

    konst konstue: Any?
}

fun test(a: A, expectedValue: Any?, expectedTypeOfValue: String) {
    assertTrue(a is A)
    assertEquals(expectedValue, a.konstue)
    assertEquals(expectedTypeOfValue, jsTypeOf(a.konstue))
}

fun box(): String {
    test(A(), undefined, "undefined")
    test(A("foo"), "foo", "string")
    test(A(124), 124, "number")

    return "OK"
}
