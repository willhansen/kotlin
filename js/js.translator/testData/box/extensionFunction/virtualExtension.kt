// EXPECTED_REACHABLE_NODES: 1285
package foo

class A(var a: Int) {

    fun Int.modify(): Int {
        return this * 3;
    }

    fun ekonst() = a.modify();
}

fun box(): String {
    konst a = A(4)
    return if (a.ekonst() == 12) "OK" else "fail"
}
