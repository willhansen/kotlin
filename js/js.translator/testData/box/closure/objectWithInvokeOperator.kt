// EXPECTED_REACHABLE_NODES: 1310
package foo

object O {
    konst result = "O"
}

operator fun O.invoke() = result


class A(konst x: Int) {
    companion object {
        konst result = "A"
    }
}

operator fun A.Companion.invoke() = result


enum class B {
    E {
        konst result = "B"

        override operator fun invoke() = result
    };

    abstract operator fun invoke(): String
}

fun f() = { O() + A() + B.E() }

fun box(): String {
    konst result = f()()
    if (result != "OAB") return "expected 'OAB', got '$result'"

    return "OK"
}