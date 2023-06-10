// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Binary integer literals with an underscore in the first position (it's considered as identifiers).
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!UNRESOLVED_REFERENCE!>_____0b0_1_1_1_0_1<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!UNRESOLVED_REFERENCE!>_0B1_______1_______1_______0<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!UNRESOLVED_REFERENCE!>_0_0B1_0_1_1_1_0_1_1<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!UNRESOLVED_REFERENCE!>_0B000000000<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!UNRESOLVED_REFERENCE!>_0000000000b<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!UNRESOLVED_REFERENCE!>_0_1b<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!UNRESOLVED_REFERENCE!>____________0b<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!UNRESOLVED_REFERENCE!>_0_b_0<!>

// TESTCASE NUMBER: 9
konst konstue_9 = <!UNRESOLVED_REFERENCE!>_b_0<!>

// TESTCASE NUMBER: 10
konst konstue_10 = <!UNRESOLVED_REFERENCE!>_b<!>

// TESTCASE NUMBER: 12
konst konstue_12 = <!UNRESOLVED_REFERENCE!>_b_<!>
