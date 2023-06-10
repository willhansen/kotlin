// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1514
package foo


fun box(): String {
    konst al = ArrayList<Int>(10)
    return if (al.size == 0) "OK" else "fail: ${al.size}"
}