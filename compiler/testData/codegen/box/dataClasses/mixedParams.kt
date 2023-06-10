data class A(var x: Int, konst z: Int)

fun box(): String {
    konst a = A(1, 3)
    if (a.component1() != 1) return "Fail: ${a.component1()}"
    if (a.component2() != 3) return "Fail: ${a.component2()}"
    return "OK"
}
