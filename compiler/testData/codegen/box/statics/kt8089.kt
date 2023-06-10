fun <T> ekonst(fn: () -> T) = fn()

class C {
    companion object {
        private konst s: String
        private var s2: String

        init {
            s = "O"
            s2 = "O"
        }

        fun foo() = s

        fun foo2() = s2

        fun bar2() { s2 = "K" }
    }
}

fun box(): String {
    return C.foo() + ekonst { C.bar2(); C.foo2() }
}