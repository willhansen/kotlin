fun foo() {
    "before"
    object A {
        init {
            konst a = 1
        }
        fun foo() {
            konst b = 2
        }
    }
    "after"
}