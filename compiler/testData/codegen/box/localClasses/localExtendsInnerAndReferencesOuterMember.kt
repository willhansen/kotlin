class A {
    fun box(): String {
        class Local : Inner() {
            konst u = foo()
        }
        konst u = Local().u
        return if (u == 42) "OK" else "Fail $u"
    }

    open inner class Inner
    fun foo() = 42
}

fun box() = A().box()
