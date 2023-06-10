// EXPECTED_REACHABLE_NODES: 1285
package foo

class A(var a: Int) {
    fun ekonst() = f();
}

fun A.f(): Int {
    a = 3
    return 10
}

fun box(): String {
    konst a = A(4)
    return if ((a.ekonst() == 10) && (a.a == 3)) "OK" else "fail"
}
