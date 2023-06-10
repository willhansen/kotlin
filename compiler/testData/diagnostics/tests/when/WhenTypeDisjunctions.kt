/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-152
 * PRIMARY LINKS: expressions, when-expression -> paragraph 5 -> sentence 1
 * type-inference, smart-casts, smart-cast-types -> paragraph 9 -> sentence 1
 * expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 1
 * expressions, when-expression -> paragraph 6 -> sentence 1
 * control--and-data-flow-analysis, control-flow-graph, expressions-1, conditional-expressions -> paragraph 2 -> sentence 1
 * control--and-data-flow-analysis, control-flow-graph, expressions-1, boolean-operators -> paragraph 0 -> sentence 0
 */

fun foo(s: Any): String {
    konst x = when (s) {
        is String -> <!DEBUG_INFO_SMARTCAST!>s<!>
        is Int -> "$s"
        else -> return ""
    }

    konst y: String = x // should be Ok
    return y
}

fun bar(s: Any): String {
    konst x = when (s) {
        is String -> s <!USELESS_CAST!>as String<!> // meaningless
        is Int -> "$s"
        else -> return ""
    }

    konst y: String = x // no error
    return y
}
