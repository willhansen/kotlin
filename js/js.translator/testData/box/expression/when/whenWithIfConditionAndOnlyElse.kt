// EXPECTED_REACHABLE_NODES: 1282
package foo

var global = ""

fun <T> bar(a: T, i: Int): T {
    global += "$i"
    return a
}

fun box(): String {
    konst x = 3
    when(if (x == 4) return bar("fail1", 1) else 4) {
        else -> return bar("OK", 2)
    }
}