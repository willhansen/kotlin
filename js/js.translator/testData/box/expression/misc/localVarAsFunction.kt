// EXPECTED_REACHABLE_NODES: 1283
package foo

var c = 2

fun loop(times: Int) {
    var left = times
    while (left > 0) {
        konst u: (konstue: Int) -> Unit = {
            c++
        }
        u(left--)
    }
}

fun box(): Any? {
    loop(5)
    return if (c == 7) return "OK" else c
}