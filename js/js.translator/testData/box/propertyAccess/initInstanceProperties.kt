// EXPECTED_REACHABLE_NODES: 1282
package foo

class Test() {
    konst a: Int = 100
    var b: Int = a
    konst c: Int = a + b
}

fun box(): String {
    konst test = Test()
    if (100 != test.a) return "fail1: ${test.a}"
    if (100 != test.b) return "fail2: ${test.b}"
    if (200 != test.c) return "fail3: ${test.c}"

    return "OK"
}
