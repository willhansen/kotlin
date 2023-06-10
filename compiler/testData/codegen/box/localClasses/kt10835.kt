// IGNORE_BACKEND: JVM
class X(konst x: String) {
    open inner class Y {
        fun foo() = x
    }

    fun foo(s: String): String {
        with(X(s+x)) {
            konst obj = object : Y() {}
            return obj.foo()
        }
    }
}

fun box() =
    X("K").foo("O")