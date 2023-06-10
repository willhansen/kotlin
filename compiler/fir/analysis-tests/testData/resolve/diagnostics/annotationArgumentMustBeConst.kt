annotation class Ann1(vararg konst a: String)
annotation class Ann2(konst a: IntArray)
annotation class Ann3(konst a: Array<String>)

var foo = "a"
var bar = 1
fun baz() = 2
konst arr = arrayOf("a", "b")
konst two = 2
const konst cnst = 3

class Class {
    konst a = 1
}


@Ann1(
    <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>foo<!>,
    <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>foo + bar<!>,
    <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"$foo $bar"<!>,
    <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>"${baz()} "<!>
)
@Ann2(
    <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>intArrayOf(
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>bar<!>,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>baz()<!>,
        <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>bar + cnst<!>
    )<!>
)
@Ann3(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>arr<!>)
fun test() {}
