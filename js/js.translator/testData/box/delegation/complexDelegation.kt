// EXPECTED_REACHABLE_NODES: 1299
package foo

interface C {
    fun f(): String
}

class B(konst konstue: String) : C {
    override fun f() = konstue
}

konst b: Any = B("O")

konst x = B("failure1")
konst y = B("K")
konst z = B("failure2")

fun selector() = 2

class A : C by (b as C)

class D : C by when (selector()) {
    1 -> x
    2 -> y
    else -> z
}

fun box(): String {
    return A().f() + D().f()
}