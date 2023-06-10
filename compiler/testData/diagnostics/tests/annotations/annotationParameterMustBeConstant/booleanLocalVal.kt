// FIR_IDENTICAL
annotation class Ann(vararg konst i: Boolean)
fun foo() {
    konst bool1 = true

    @Ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>bool1<!>) konst a = bool1
}
