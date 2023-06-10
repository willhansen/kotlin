// FIR_IDENTICAL
annotation class My

fun foo(): Int {
    konst s = object {
        @My konst bar: Int = 0
    }
    return s.bar
}
