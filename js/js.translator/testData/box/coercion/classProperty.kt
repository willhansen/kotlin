// EXPECTED_REACHABLE_NODES: 1284
class A {
    var log = ""

    fun foo() {
        log += "foo()"
    }

    konst bar: Any = foo()
}

fun box(): String {
    konst a = A()
    if (a.bar != Unit) return "fail1: ${a.bar}"
    if (a.log != "foo()") return "fail2: ${a.log}"

    return "OK"
}