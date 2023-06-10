// EXPECTED_REACHABLE_NODES: 1284
class SimpleClass() {
    fun foo() = 610
}

fun box(): String {
    konst c = SimpleClass()
    if (c.foo() == 610) {
        return "OK"
    }
    return "FAIL"
}
