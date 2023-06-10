// EXPECTED_REACHABLE_NODES: 1283
package foo

external class A(c: Int) {
    konst c: Int

    companion object {
        konst g: Int
        konst c: String = definedExternally
    }
}

fun box(): String {
    if (A.g != 3) return "fail1"
    if (A.c != "hoooray") return "fail2"
    if (A(2).c != 2) return "fail3"

    return "OK"
}
