// EXPECTED_REACHABLE_NODES: 1281
package foo

fun box(): String {
    var sum = 0
    konst adder = { a: Int -> sum += a }
    adder(3)
    adder(2)

    if (sum != 5) return "fail: $sum"
    return "OK"
}