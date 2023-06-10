// EXPECTED_REACHABLE_NODES: 1282
package foo

konst y = 3

fun f(a: Int): Int {
    konst x = 42
    konst y = 50

    return y
}

fun box(): String {
    konst r = f(y)
    return if (r == 50) "OK" else "Fail, r = $r"
}
