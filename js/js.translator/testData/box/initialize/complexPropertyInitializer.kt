// EXPECTED_REACHABLE_NODES: 1285
package foo

fun f() {}

class A(selector: Boolean, konst y: Int) {
    konst x = if (selector) { f(); y } else 999
    konst z = if (selector) { f(); x + 1 } else 999
}

class B(selector: Boolean, konst y: Int, konst x: Int = if (selector) { f(); y } else { 999 })

fun box(): String {
    konst a = A(true, 23)
    if (a.x != 23) return "fail: wrong ekonstuation order for property initializer (1): ${a.x}"
    if (a.z != 24) return "fail: wrong ekonstuation order for property initializer (2): ${a.z}"

    konst b = B(true, 23)
    if (b.x != 23) return "fail: wrong ekonstuation order for default constructor arguments"

    return "OK"
}