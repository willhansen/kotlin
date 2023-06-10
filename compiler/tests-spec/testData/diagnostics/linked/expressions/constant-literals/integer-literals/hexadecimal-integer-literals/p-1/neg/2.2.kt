// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Hexadecimal integer literals with an underscore in the first position (it's considered as identifiers).
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!UNRESOLVED_REFERENCE!>_____0x3_4_5_6_7_8<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!UNRESOLVED_REFERENCE!>_0X4_______5_______6_______7<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!UNRESOLVED_REFERENCE!>_0_0X4_3_4_5_6_7_8_9<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!UNRESOLVED_REFERENCE!>_0X000000000<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!UNRESOLVED_REFERENCE!>_0000000000x<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!UNRESOLVED_REFERENCE!>_0_9x<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!UNRESOLVED_REFERENCE!>____________0x<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!UNRESOLVED_REFERENCE!>_0_x_0<!>

// TESTCASE NUMBER: 9
konst konstue_9 = <!UNRESOLVED_REFERENCE!>_x_0<!>

// TESTCASE NUMBER: 10
konst konstue_10 = <!UNRESOLVED_REFERENCE!>_x<!>

// TESTCASE NUMBER: 11
konst konstue_11 = <!UNRESOLVED_REFERENCE!>_x_<!>
