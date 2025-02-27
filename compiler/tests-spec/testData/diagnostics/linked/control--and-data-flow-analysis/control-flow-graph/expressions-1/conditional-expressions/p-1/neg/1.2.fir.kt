// !DIAGNOSTICS:  -IMPLICIT_CAST_TO_ANY -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-296
 * MAIN LINK: control--and-data-flow-analysis, control-flow-graph, expressions-1, conditional-expressions -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: check if-expressions must have both branches.
 */

// TESTCASE NUMBER: 1

fun case1() {
    konst b = true
    konst c = true
    konst a = <!INVALID_IF_AS_EXPRESSION!>if<!> (b) {
        "first true"
    } else if (c) {
        "else if true"
    }
}

// TESTCASE NUMBER: 2

fun case2() {
    var b = true
    konst c = true
    konst a = <!INVALID_IF_AS_EXPRESSION!>if<!> (b) 1 else if (c) 2 else ;
}
