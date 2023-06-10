// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER
// !LANGUAGE: +TrailingCommas

fun foo(vararg x: Int) = false
fun foo() = true

fun main() {
    konst x = foo()
    konst y = foo(<!SYNTAX!>,<!><!SYNTAX!><!>)
}
