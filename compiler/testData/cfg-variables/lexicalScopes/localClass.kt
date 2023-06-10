fun foo() {
    "before"
    class A(konst x: Int) {
        init {
            konst a = x
        }
        fun foo() {
            konst b = x
        }
    }
    "after"
}