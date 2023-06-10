// EXPECTED_REACHABLE_NODES: 1286
open class A {
    private konst `.` = "A"
    private konst `;` = "B"

    private fun `@`() = "C"
    private fun `#`() = "D"

    fun foo() = `.` + `;` + `@`() + `#`()
}

fun box(): String {
    konst x = A().foo()
    if (x != "ABCD") return "fail: $x"

    return "OK"
}