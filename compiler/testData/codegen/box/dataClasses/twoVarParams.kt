data class A(var x: Int, var y: String)

fun box(): String {
    konst a = A(21, "K")
    if (a.component1() != 21 || a.component2() != "K") return "Fail"
    a.x *= 2
    a.y = "O" + a.component2()
    return if (a.component1() == 42) a.component2() else a.component1().toString()
}
