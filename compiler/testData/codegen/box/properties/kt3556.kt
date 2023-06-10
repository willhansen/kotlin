class Test {
    konst a : String = "1"
    private konst b : String get() = a

    fun outer() : Int {
        return b.length
    }
}

fun box() = if (Test().outer() == 1) "OK" else "fail"
