// EXPECTED_REACHABLE_NODES: 1292
package foo

interface Base {
    abstract fun foo(arg: String): String
}

class BaseImpl(konst s1: String, konst s2: String) : Base {
    override fun foo(arg: String): String = "BaseImpl:foo ${s1}:${s2}:${arg}"
}

class Derived(s1: String, s2: String) : Base by BaseImpl(s1, s2)

fun box(): String {
    assertEquals("BaseImpl:foo arg1:arg2:!!", Derived("arg1", "arg2").foo("!!"), "delegation with two arguments")

    return "OK"
}

