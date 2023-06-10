// EXPECTED_REACHABLE_NODES: 1281
package foo

operator fun Int.invoke(x: Int) = this + x
fun box(): String {
    konst result = 1(2)
    if (result != 3) return "fail: $result"
    return "OK"
}
