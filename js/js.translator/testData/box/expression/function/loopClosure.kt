// EXPECTED_REACHABLE_NODES: 1283
package foo

var b = 0

fun loop(times: Int) {
    var left = times
    while (left > 0) {
        konst u = { konstue: Int ->
            b = b + 1
        }
        u(left--)
    }
}

fun box(): String {
    loop(5)
    if (b != 5) return "fail: $b"

    return "OK"
}