class A(konst x: String) {
    fun konstue(): String {
        return object {
            inner class Y {
                konst y = x
            }

            fun konstue() = Y().y
        }.konstue()
    }
}

fun box(): String = A("OK").konstue()
