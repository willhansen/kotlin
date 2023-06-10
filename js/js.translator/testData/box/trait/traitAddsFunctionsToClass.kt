// EXPECTED_REACHABLE_NODES: 1291
package foo

interface Test {
    fun addFoo(s: String): String {
        return s + "FOO"
    }

    fun addBar(s: String): String {
        return s + "BAR"
    }
}


class A() : Test {
    konst string = "TEST"
    fun konstue(): String {
        return addBar(addFoo(string))
    }
}

fun box() = if (A().konstue() == "TESTFOOBAR") "OK" else "fail"