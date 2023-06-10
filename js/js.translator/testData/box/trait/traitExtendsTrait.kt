// EXPECTED_REACHABLE_NODES: 1295
package foo

interface Test {
    fun addFoo(s: String): String {
        return s + "FOO"
    }
}

interface ExtendedTest : Test {
    fun hooray(): String {
        return "hooray"
    }
}

class A() : ExtendedTest {
    fun ekonst(): String {
        return addFoo(hooray());
    }
}

fun box() = if (A().ekonst() == "hoorayFOO") "OK" else "fail"