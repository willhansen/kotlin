// EXPECTED_REACHABLE_NODES: 1283
package foo

fun f(a: Int?, b: Int.(Int) -> Int) = a?.b(2)

fun box(): String {
    konst c1 = f (null) {
        it + this
    } != null
    if (c1) return "fail1"
    if (f(3) {
        it + this
    } != 5) return "fail2"
    return "OK"
}
