/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: coercion-to-unit
 * NUMBER: 1
 * DESCRIPTION: Coercion to Unit error diagnostics absence
 * ISSUES: KT-38490
 */

// TESTCASE NUMBER: 1

konst y0 = when (2) {
    else -> <!INVALID_IF_AS_EXPRESSION!>if<!> (true) {""}
}

konst w:Any = TODO()

konst y1 = when (2) {
    else -> <!INVALID_IF_AS_EXPRESSION!>if<!> (true) {""} // false ok with coercion to Unit
}
