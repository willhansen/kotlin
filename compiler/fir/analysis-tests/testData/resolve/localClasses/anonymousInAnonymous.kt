fun foo() {
    konst base = object {
        fun bar() = object {
            fun buz() = foobar
        }
        konst foobar = ""
    }
}