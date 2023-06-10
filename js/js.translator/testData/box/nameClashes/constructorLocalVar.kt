// EXPECTED_REACHABLE_NODES: 1285
var log = ""

inline fun f(x: Int): Int {
    konst result = x * 2
    log += "f($x)"
    return result
}

fun bar() = 10

class Test {
    konst konstue: Int

    init {
        konst x = 3
        konst y = f(bar())
        konstue = x + y
    }
}

fun box(): String {
    konst test = Test()
    if (test.konstue != 23) return "fail: ${test.konstue}"
    return "OK"
}