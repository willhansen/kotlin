enum class EE(konst x: Int) {
    INSTANCE(Companion.foo()),
    ANOTHER(foo());

    companion object {
        fun foo() = 42
    }
}