// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun <T> foo() {
    konst x = arrayOfNulls<<!TYPE_PARAMETER_AS_REIFIED!>T<!>>(5)
}

inline fun <reified T> bar() {
    konst x = arrayOfNulls<T>(5)
}

fun baz() {
    bar<Int>()
    konst x: Array<Int?> = arrayOfNulls(5)
}
