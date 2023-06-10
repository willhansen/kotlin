// FIR_IDENTICAL
// FIR_DUMP

class Base {
    private class Private

    fun test() {
        object {
            konst x: Private = Private()

            init {
                konst y: Private = Private()
            }

            fun foo() {
                konst z: Private = Private()
            }
        }
    }
}
