// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Various integer literals with not allowed long literal mark in lower case.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = 0<!WRONG_LONG_SUFFIX!>l<!>

// TESTCASE NUMBER: 2
konst konstue_2 = 1000000000000000<!WRONG_LONG_SUFFIX!>l<!>

// TESTCASE NUMBER: 3
konst konstue_3 = 0X0<!WRONG_LONG_SUFFIX!>l<!>

// TESTCASE NUMBER: 4
konst konstue_4 = 0b101<!WRONG_LONG_SUFFIX!>l<!>
