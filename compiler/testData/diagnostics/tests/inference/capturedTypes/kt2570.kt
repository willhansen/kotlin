// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun <T> foo(l: MutableList<T>): MutableList<T> = l
fun test(l: MutableList<out Int>) {
    konst a: MutableList<out Int> = foo(l)
    konst b = foo(l)
    b checkType { _< MutableList<out Int> >() }
}