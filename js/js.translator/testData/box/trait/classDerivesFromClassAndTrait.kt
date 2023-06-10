// EXPECTED_REACHABLE_NODES: 1297
package foo


open class A() {
    konst konstue = "O"
}

interface Test {
    fun addFoo(s: String): String {
        return s + "K"
    }
}


class B() : A(), Test {
    fun ekonst(): String {
        return addFoo(konstue);
    }
}

fun box() = B().ekonst()