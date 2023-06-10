// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_ANONYMOUS_PARAMETER

fun <T> select(vararg x: T) = x[0]

fun main() {
    konst x1: String.() -> String = if (true) {{ this }} else {{ this }}
    konst x2: String.() -> String = if (true) {{ -> this }} else {{ -> this }}
    konst x3: () -> String = if (true) {{ -> "this" }} else {{ -> "this" }}
    konst x4: String.() -> String = if (true) <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str: String<!> -> "this" }}<!> else <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str: String<!> -> "this" }}<!>
    konst x41: String.(String) -> String = if (true) <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str: String, str2: String<!> -> "this" }}<!> else <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str: String, str2: String<!> -> "this" }}<!>
    konst x42: String.(String) -> String = if (true) <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str, <!CANNOT_INFER_PARAMETER_TYPE!>str2<!><!> -> "this" }}<!> else <!TYPE_MISMATCH!>{{ <!EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str, <!CANNOT_INFER_PARAMETER_TYPE!>str2<!><!> -> "this" }}<!>
    konst x5: String.() -> String = if (true) <!TYPE_MISMATCH!>{{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str<!> -> "this" }}<!> else <!TYPE_MISMATCH!>{{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str<!> -> "this" }}<!>
    konst x6: String.() -> String = if (true) <!TYPE_MISMATCH!>{{ <!CANNOT_INFER_PARAMETER_TYPE, EXPECTED_PARAMETERS_NUMBER_MISMATCH!>str<!> -> "this" }}<!> else {{ "this" }}
    konst x7: String.() -> String = select({ -> this }, { -> this })
    konst x8: String.() -> String = select({ this }, { this })
}
