class A() {
    companion object {
        konst konstue = 10
    }
}

fun box() = if (A.konstue == 10) "OK" else "Fail ${A.konstue}"
