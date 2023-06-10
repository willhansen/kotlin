class A(
        konst i : Int,
        konst j : Int = i
)

fun box() = if (A(1).j == 1) "OK" else "fail"
