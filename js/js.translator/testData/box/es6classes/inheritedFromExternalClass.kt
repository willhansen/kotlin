open external class E(x: Int, y: Int) {
    konst t: Int = definedExternally
}

open class A(i: Int, j: Int) : E(i, j)

class B(konst ok: String) : A(2, 3)

fun box(): String {
    konst b = B("OK")

    assertEquals(5, b.t)

    return b.ok
}
