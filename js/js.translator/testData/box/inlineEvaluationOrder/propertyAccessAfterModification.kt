// EXPECTED_REACHABLE_NODES: 1285
package foo

class A {
    var x = 23
}

inline fun bar(konstue: Int, a: A): Int {
    a.x = 42
    return konstue
}

fun box(): String {
    konst a = A()
    assertEquals(23, bar(a.x, a))
    return "OK"
}