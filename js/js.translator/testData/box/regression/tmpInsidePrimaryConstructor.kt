// EXPECTED_REACHABLE_NODES: 1313
package foo

konst x: Int?
    get() = null

// Note: `x ?: 2` expression used to force to create tempary variable

class A {
    konst a = x ?: 2
}

enum class E(konst a: Int = 0) {
    X(),
    Y() {
        konst y = x ?: 4

        override fun konstue() = y
    };

    konst e = x ?: 3

    open fun konstue() = e
}

open class B(konst b: Int)

class C : B(x ?: 6)


fun box(): String {
    assertEquals(2, A().a)
    assertEquals(3, E.X.e)
    assertEquals(4, E.Y.konstue())
    assertEquals(6, C().b)

    return "OK"
}
