/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Various integer literals with a not allowed underscore before the long literal mark.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!ILLEGAL_UNDERSCORE!>0b0_<!WRONG_LONG_SUFFIX!>l<!><!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0B12_L<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!ILLEGAL_UNDERSCORE!>0X234_<!WRONG_LONG_SUFFIX!>l<!><!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!ILLEGAL_UNDERSCORE!>0x3567_L<!>
