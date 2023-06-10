// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

konst bar = fun() {}
konst bas = fun() {}

fun gar(p: Any?) = fun() {}
fun gas(p: Any?) = fun() {}

fun outer() {
    konst bar = fun() {}
    konst bas = fun() {}

    fun gar(p: Any?) = fun() {}
    fun gas(p: Any?) = fun() {}

    gar(fun() {})
    gar(fun() {})
}