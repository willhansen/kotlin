// EXPECTED_REACHABLE_NODES: 1366
package foo

konst a1 = arrayOfNulls<Int>(0)

fun box(): String {
    for (a in a1) {
        return "fail"
    }
    return "OK"
}