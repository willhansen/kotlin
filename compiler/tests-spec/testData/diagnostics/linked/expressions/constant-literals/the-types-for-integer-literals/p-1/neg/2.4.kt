// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 4
 * DESCRIPTION: Type checking (comparison with inkonstid types) of too a big integers.
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
fun case_1() {
    checkSubtype<Long>(<!TYPE_MISMATCH!>-<!INT_LITERAL_OUT_OF_RANGE!>9223372036854775808L<!><!>)
    -<!INT_LITERAL_OUT_OF_RANGE!>9223372036854775808L<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }

    checkSubtype<Long>(<!INT_LITERAL_OUT_OF_RANGE, TYPE_MISMATCH!>9223372036854775808L<!>)
    <!INT_LITERAL_OUT_OF_RANGE!>9223372036854775808L<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }
}

// TESTCASE NUMBER: 2
fun case_2() {
    checkSubtype<Long>(<!INT_LITERAL_OUT_OF_RANGE, TYPE_MISMATCH!>100000000000000000000000000000000L<!>)
    <!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000L<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }

    checkSubtype<Long>(<!INT_LITERAL_OUT_OF_RANGE, TYPE_MISMATCH!>100000000000000000000000000000000l<!>)
    <!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000l<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }

    checkSubtype<Long>(<!TYPE_MISMATCH!>-<!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000L<!><!>)
    -<!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000L<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }

    checkSubtype<Long>(<!TYPE_MISMATCH!>-<!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000l<!><!>)
    -<!INT_LITERAL_OUT_OF_RANGE!>100000000000000000000000000000000l<!> checkType { <!NONE_APPLICABLE!>check<!><Long>() }
}
