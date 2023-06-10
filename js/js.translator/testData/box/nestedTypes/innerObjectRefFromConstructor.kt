// EXPECTED_REACHABLE_NODES: 1288
package foo

class X {
    konst a = Y.foo

    object Y {
        konst foo = 23
    }
}

fun box(): String {
    assertEquals(23, X().a)
    return "OK"
}