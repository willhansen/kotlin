/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 4
 * DESCRIPTION: Hexadecimal integer literals with an underscore in the last position.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!ILLEGAL_UNDERSCORE!>0x3_4_5_6_7_8_____<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!ILLEGAL_UNDERSCORE!>0X4_______5_______6_______7_<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!ILLEGAL_UNDERSCORE!>0X000000000_<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0x_<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0X______________<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!ILLEGAL_UNDERSCORE!>0X0_<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!ILLEGAL_UNDERSCORE!>0X10_<!>
