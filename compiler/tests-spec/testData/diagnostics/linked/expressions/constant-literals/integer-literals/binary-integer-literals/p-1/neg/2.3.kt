/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: Binary integer literals with an underscore in the last position.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!ILLEGAL_UNDERSCORE!>0b0_1_1_0_1_1_____<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!ILLEGAL_UNDERSCORE!>0B1_______1_______0_______1_<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!ILLEGAL_UNDERSCORE!>0B000000000_<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0b_<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0B______________<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!ILLEGAL_UNDERSCORE!>0B0_<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!ILLEGAL_UNDERSCORE!>0B10_<!>
