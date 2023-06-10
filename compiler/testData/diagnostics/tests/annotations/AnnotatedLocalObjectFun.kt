// FIR_IDENTICAL
annotation class My

fun foo() {
    konst s = object {
        @My fun bar() {}
    }
    s.bar()
}
