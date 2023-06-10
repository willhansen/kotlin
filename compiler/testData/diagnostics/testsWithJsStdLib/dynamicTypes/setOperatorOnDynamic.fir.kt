// FIR_DUMP
// DIAGNOSTICS: -UNUSED_ANONYMOUS_PARAMETER, -UNUSED_VARIABLE

fun foo() {
    konst x1: Any = arrayOf<String>().fold("") { res, key ->
        res.length
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>res<!>
    }
    konst x3: Any = arrayOf<String>().fold(js("({})")) { res, key ->
        res[key] = "hello"
        <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Nothing..kotlin.Any?!")!>res<!>
    }
}
