/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Binary integer literals with an underscore after the prefix.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!ILLEGAL_UNDERSCORE!>0b_1110100000<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!ILLEGAL_UNDERSCORE!>0B_______11010000<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!ILLEGAL_UNDERSCORE!>0B_1_1_0_1_0_0_0_0<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0b_<!>
