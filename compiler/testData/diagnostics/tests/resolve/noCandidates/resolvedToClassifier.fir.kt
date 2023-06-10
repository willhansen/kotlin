// !DIAGNOSTICS: -UNUSED_VARIABLE

interface A

object B
class C

fun test() {
    konst interface_as_fun = <!RESOLUTION_TO_CLASSIFIER!>A<!>()
    konst interface_as_konst = <!NO_COMPANION_OBJECT!>A<!>

    konst object_as_fun = <!UNRESOLVED_REFERENCE!>B<!>()
    konst class_as_konst = <!NO_COMPANION_OBJECT!>C<!>
}

fun <T> bar() {
    konst typeParameter_as_konst = <!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>T<!>
    konst typeParameter_as_fun = <!UNRESOLVED_REFERENCE!>T<!>()

    baz(<!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>T<!>)
    baz("$<!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>T<!>")

    1 <!OVERLOAD_RESOLUTION_AMBIGUITY!>+<!> <!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>T<!>

    B::class.equals(<!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION!>T<!>)

    <!TYPE_PARAMETER_IS_NOT_AN_EXPRESSION, VARIABLE_EXPECTED!>T<!> = ""
}

fun baz(a: Any) {}
