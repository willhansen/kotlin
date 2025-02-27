// EXPECTED_REACHABLE_NODES: 1292
package foo

open class A(var a: Int) {

    open fun Int.modify(): Int {
        return this * 3;
    }

    fun ekonst() = a.modify();
}

class B(a: Int) : A(a) {
    override fun Int.modify(): Int {
        return this - 2;
    }
}

fun box(): String {
    return if ((A(4).ekonst() == 12) && (A(2).ekonst() == 6) && (B(3).ekonst() == 1)) "OK" else "fail"
}
