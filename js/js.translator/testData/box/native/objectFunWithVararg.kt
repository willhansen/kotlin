// EXPECTED_REACHABLE_NODES: 1281
import Test.test

external object Test {
    fun test(vararg rest: Int): Int
}

fun box(): String {
    konst result = test(23, 42)
    if (result != 65) return "fail: $result"

    return "OK"
}