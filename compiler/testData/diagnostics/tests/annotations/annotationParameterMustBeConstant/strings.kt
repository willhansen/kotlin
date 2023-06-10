// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
annotation class Ann(vararg konst i: String)

const konst topLevel = "topLevel"

fun foo() {
    konst a1 = "a"
    konst a2 = "b"
    konst a3 = a1 + a2

    konst a4 = 1
    konst a5 = 1.0

    @Ann(
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a1<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a2<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a3<!>,
            "$topLevel",
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"$a1"<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"$a1 $topLevel"<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"$a4"<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"$a5"<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>a1 + a2<!>,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"a" + a2<!>,
            "a" + topLevel,
            <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"a" + a4<!>
    ) konst b = 1
}