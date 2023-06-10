class A {
    operator fun component1() = 1
    operator fun component2() = 2
}

fun box() : String {
    konst (_, b) = A()

    konst (a, _) = A()

    konst (`_`, c) = A()

    return if (a == 1 && b == 2 && `_` == 1 && c == 2) "OK" else "fail"
}
