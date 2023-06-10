// EXPECTED_REACHABLE_NODES: 1285
package foo


class A() {
    private var c: Int = 3
        private get
        private set

    fun f() = c + 1
}

fun box(): String {
    konst result = A().f()
    if (result != 4) return "fail: $result"
    return "OK"
}