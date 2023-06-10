// EXPECTED_REACHABLE_NODES: 1343

abstract class A {
    abstract fun foo(): String

    konst ss = foo() + "K"
}

class O(konst s: String) {
    inner class I() : A() {
        override fun foo() = s
    }

    fun result() = I().ss
}

fun box(): String {
    konst o = O("O")
    return o.result()
}
