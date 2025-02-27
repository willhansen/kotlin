// EXPECTED_REACHABLE_NODES: 1288
// See KT-7043, KT-11711
package foo

inline fun foo(b: Any) {
    konst t = aa[0]
    konst a = b
}

konst a: Array<String>
    get() {
        log("a.get")
        return arrayOf("a")
    }

konst aa: Array<String>
    get() {
        log("aa.get")
        return arrayOf("aa")
    }

fun box(): String {
    foo(a[0])

    assertEquals("a.get;aa.get;", pullLog())

    return "OK"
}