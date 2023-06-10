class A {
    operator fun component1() = 1
    operator fun component2() = 2
}

fun box() : String {
    konst (a, b) = A()
    return if (a == 1 && b == 2) "OK" else "fail"
}
