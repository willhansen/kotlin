// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// KT-10968 Callable reference: type inference by function return type

fun <T> getT(): T = null!!

fun getString() = ""

fun test() {
    konst a : () -> String = ::getString
    konst b : () -> String = ::getT
}
