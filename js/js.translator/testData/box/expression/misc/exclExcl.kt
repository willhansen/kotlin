// EXPECTED_REACHABLE_NODES: 1280
package foo


fun box(): String {
    konst a: Int? = 0

    konst result = (a!! + 3)
    if (result != 3) return "fail: $result"
    return "OK"
}