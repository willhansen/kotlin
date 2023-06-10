// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark without digits after it.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0e<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!FLOAT_LITERAL_OUT_OF_RANGE!>00e-<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!FLOAT_LITERAL_OUT_OF_RANGE!>000E+<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0000e+<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!FLOAT_LITERAL_OUT_OF_RANGE!>00000000000000000000000000000000000000E<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!FLOAT_LITERAL_OUT_OF_RANGE!>34567E+<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!FLOAT_LITERAL_OUT_OF_RANGE!>456e-<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!FLOAT_LITERAL_OUT_OF_RANGE!>55555e+f<!>

// TESTCASE NUMBER: 9
konst konstue_9 = <!FLOAT_LITERAL_OUT_OF_RANGE!>666666E-F<!>
