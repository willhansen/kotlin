// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Hexadecimal integer literals with the prefix only.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!INT_LITERAL_OUT_OF_RANGE!>0x<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!INT_LITERAL_OUT_OF_RANGE!>0X<!>
