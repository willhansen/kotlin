// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
annotation class Ann(vararg konst i: Boolean)
fun foo() {
    konst a1 = 1 > 2
    konst a2 = 1 == 2
    konst a3 = a1 == a2
    konst a4 = a1 > a2

    @Ann(
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a1<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a2<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a3<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a1 > a2<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a1 == a2<!>
    ) konst b = 1
}