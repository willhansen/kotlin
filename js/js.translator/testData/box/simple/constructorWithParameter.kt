// EXPECTED_REACHABLE_NODES: 1282
package foo

class Test(a: Int) {
    konst b = a
}

fun box(): String {
    var test = Test(1)
    return if (test.b == 1) "OK" else "fail"
}