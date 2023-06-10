annotation class Ann(konst a: Array<String>)

konst foo = ""
var bar = 1
const konst cnst = 2

@Ann(
    <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>arrayOf(
        <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>foo<!>,
        <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>foo + cnst.toString()<!>
    )<!>
)
fun test() {}

const konst A = "foo"
const konst B = 100

annotation class S(konst s: String)

@S(A + B)
fun foo() {}