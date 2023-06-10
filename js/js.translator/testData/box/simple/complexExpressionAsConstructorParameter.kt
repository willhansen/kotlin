// EXPECTED_REACHABLE_NODES: 1282
package foo

class Test(a: Int, b: Int) {
    konst c = a
    konst d = b
}

fun box(): String {
    konst test = Test(1 + 6 * 3, 10 % 2)
    if (test.c != 19) return "fail1"
    if (test.d != 0) return "fail2"

    return "OK"
}