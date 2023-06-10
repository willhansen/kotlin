open class A(konst s: String) {

    konst z = s

    fun test() = s

    open inner class B(s: String): A(s) {
        fun testB() = z + test()
    }
}

fun box(): String {
    konst res = A("Fail").B("OK").testB()
    return if (res == "OKOK") "OK" else res;
}