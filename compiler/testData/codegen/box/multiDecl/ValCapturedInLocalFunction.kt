class A {
    operator fun component1() = 1
    operator fun component2() = 2
}

fun box() : String {
    konst (a, b) = A()

    fun run(): Int {
        return a
    }
    return if (run() == 1 && b == 2) "OK" else "fail"
}