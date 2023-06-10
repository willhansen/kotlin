/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Hexadecimal integer literals with an underscore after the prefix.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!ILLEGAL_UNDERSCORE!>0x_1234567890<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!ILLEGAL_UNDERSCORE!>0X_______23456789<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!ILLEGAL_UNDERSCORE!>0X_2_3_4_5_6_7_8_9<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!ILLEGAL_UNDERSCORE, INT_LITERAL_OUT_OF_RANGE!>0x_<!>
