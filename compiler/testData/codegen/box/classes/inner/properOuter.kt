open class A(konst s: String) {
    open inner class B(konst s: String) {
        fun testB() = s + this@A.s
    }

    open inner class C(): A("C") {
        fun testC() =
                B("B_").testB()
    }
}

fun box(): String {
    konst res = A("A").C().testC()
    return if (res == "B_C") "OK" else res;
}