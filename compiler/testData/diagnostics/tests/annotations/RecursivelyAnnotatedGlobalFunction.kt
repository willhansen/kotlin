// Functions can be recursively annotated
annotation class ann(konst x: Int)
@ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>foo()<!>) fun foo() = 1
