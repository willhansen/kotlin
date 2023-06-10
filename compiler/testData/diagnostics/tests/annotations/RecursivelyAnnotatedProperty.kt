// FIR_IDENTICAL
// Properties can be recursively annotated
annotation class ann(konst x: Int)
class My {
    @ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>x<!>) konst x: Int = 1
}