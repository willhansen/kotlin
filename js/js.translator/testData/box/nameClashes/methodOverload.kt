// EXPECTED_REACHABLE_NODES: 1287
package foo

class A() {

    fun ekonst() = 3
    fun ekonst(a: Int) = 4
    fun ekonst(a: String) = 5
    fun ekonst(a: String, b: Int) = 6

}

fun box(): String {

    if (A().ekonst() != 3) return "fail1"
    if (A().ekonst(2) != 4) return "fail2"
    if (A().ekonst("3") != 5) return "fail3"
    if (A().ekonst("a", 3) != 6) return "fail4"

    return "OK"
}