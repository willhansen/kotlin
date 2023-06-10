// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_ANONYMOUS_PARAMETER

fun <T> select(vararg x: T) = x[0]

fun main() {
    konst x1: String.() -> String = if (true) {{ this }} else {{ this }}
    konst x2: String.() -> String = if (true) {{ -> this }} else {{ -> this }}
    konst x3: () -> String = if (true) {{ -> "this" }} else {{ -> "this" }}
    konst x4: String.() -> String = if (true) {{ str: String -> "this" }} else {{ str: String -> "this" }}
    konst x41: String.(String) -> String = if (true) {{ str: String, str2: String -> "this" }} else {{ str: String, str2: String -> "this" }}
    konst x42: String.(String) -> String = if (true) {{ str, <!CANNOT_INFER_PARAMETER_TYPE!>str2<!> -> "this" }} else {{ str, <!CANNOT_INFER_PARAMETER_TYPE!>str2<!> -> "this" }}
    konst x5: String.() -> String = if (true) {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE!>str<!> -> "this" }<!>} else {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE!>str<!> -> "this" }<!>}
    konst x6: String.() -> String = if (true) {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_PARAMETER_TYPE!>str<!> -> "this" }<!>} else {{ "this" }}
    konst x7: String.() -> String = select({ -> this }, { -> this })
    konst x8: String.() -> String = select({ this }, { this })
}
