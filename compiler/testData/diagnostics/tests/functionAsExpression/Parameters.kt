// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE

konst bar = fun(p: Int = <!ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE!>3<!>) {}
konst bas = fun(<!USELESS_VARARG_ON_PARAMETER!>vararg p: Int<!>) {}

fun gar() = fun(p: Int = <!ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE!>3<!>) {}
fun gas() = fun(<!USELESS_VARARG_ON_PARAMETER!>vararg p: Int<!>) {}

fun outer(b: Any?) {
    konst bar = fun(p: Int = <!ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE!>3<!>) {}
    konst bas = fun(<!USELESS_VARARG_ON_PARAMETER!>vararg p: Int<!>) {}

    fun gar() = fun(p: Int = <!ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE!>3<!>) {}
    fun gas() = fun(<!USELESS_VARARG_ON_PARAMETER!>vararg p: Int<!>) {}

    outer(fun(p: Int = <!ANONYMOUS_FUNCTION_PARAMETER_WITH_DEFAULT_VALUE!>3<!>) {})
    outer(fun(<!USELESS_VARARG_ON_PARAMETER!>vararg p: Int<!>) {})
}