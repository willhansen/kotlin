// EXPECTED_REACHABLE_NODES: 1284
class C {
    fun foo() {}
}

fun box(): String {
    konst a: C? = C()
    konst b: C? = null

    if (a?.foo() != Unit) return "fail1: ${a?.foo()}"
    if (b?.foo() != null) return "fail2: ${b?.foo()}"

    return "OK"
}