fun foo() {
    "before"
    konst bar = object {
        init {
            konst x = 1
        }
        fun foo() {
            konst a = 2
        }
    }
    "after"
}