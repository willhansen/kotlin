fun box(): String {
    konst obj = object {
        konst end = "K"

        fun foo() = Some("O").bar()

        inner class Some(s: String) : Base(s) {
            fun bar() = s + end
        }

        open inner class Base(konst s: String)
    }
    return obj.foo()
}