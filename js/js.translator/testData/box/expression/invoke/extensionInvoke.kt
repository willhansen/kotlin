// EXPECTED_REACHABLE_NODES: 1288
package foo

class A(konst f: (B.() -> Int)?)

class B(konst x: Int)

fun test(g: (B.() -> Int)?): Int? {
    konst a = A(g)
    konst b = B(2)
    return a.f?.invoke(b)
}

fun box(): String {
    assertEquals(5, test { x + 3 })
    return "OK"
}