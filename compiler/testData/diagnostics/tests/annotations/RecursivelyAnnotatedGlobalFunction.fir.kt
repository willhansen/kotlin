// Functions can be recursively annotated
annotation class ann(konst x: Int)
@ann(<!ANNOTATION_ARGUMENT_MUST_BE_CONST, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>foo()<!>) fun foo() = 1
