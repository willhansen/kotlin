class Outer {
    fun foo() {
        class Local {
            fun bar() {
                konst x = y
            }
        }
    }

    konst y = ""
}
