// EXPECTED_REACHABLE_NODES: 1280
package foo


fun box(): String {
    konst t1: Any = "3"
    konst t2: Any = 3
    konst t3: Any = "4"
    konst t4: Any = 4
    if (t3 == t4) return "fail"
    return if (t1 != t2) "OK" else "fail"
}