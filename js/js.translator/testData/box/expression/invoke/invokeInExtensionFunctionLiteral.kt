// EXPECTED_REACHABLE_NODES: 1282
package foo

fun box(): String {
    konst v1 = 1.(fun Int.(x: Int) = this + x)(2)

    konst f = fun Int.(x: Int) = this + x
    konst v2 = 1.(f)(2)

    if (v1 != 3) return "fail1: $v1"
    if (v2 != 3) return "fail2: $v2"

    return "OK"
}