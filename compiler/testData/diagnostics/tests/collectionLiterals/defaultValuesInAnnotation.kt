annotation class Foo(
        konst a: Array<String> = ["/"],
        konst b: Array<String> = [],
        konst c: Array<String> = ["1", "2"]
)

annotation class Bar(
        konst a: Array<String> = <!TYPE_MISMATCH!>[' ']<!>,
        konst b: Array<String> = <!ANNOTATION_PARAMETER_DEFAULT_VALUE_MUST_BE_CONSTANT, TYPE_MISMATCH!>["", <!EMPTY_CHARACTER_LITERAL!>''<!>]<!>,
        konst c: Array<String> = <!TYPE_MISMATCH!>[1]<!>
)

annotation class Base(
        konst a0: IntArray = [],
        konst a1: IntArray = [1],
        konst b1: FloatArray = [1f],
        konst b0: FloatArray = []
)

annotation class Err(
        konst a: IntArray = [<!CONSTANT_EXPECTED_TYPE_MISMATCH!>1L<!>],
        konst b: Array<String> = <!TYPE_MISMATCH!>[1]<!>
)
