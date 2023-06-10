// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    konst a = 10
    konst b = 3
    when {
        a > b -> return "OK"
        b > a -> return "b"
        else -> return "else"
    }
}