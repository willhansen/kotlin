/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Real literals without digits after an exponent mark and with undescores in the different pisitions.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.0_0e-<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0_0___0E+<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.0_0e-F<!>

// TESTCASE NUMBER: 4
konst konstue_4 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0__________________________0E+<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0_00e<!>

// TESTCASE NUMBER: 6
konst konstue_6 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.0_0__0___0E<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0_1e+f<!>

// TESTCASE NUMBER: 8
konst konstue_8 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.0_0E-<!>

// TESTCASE NUMBER: 9
konst konstue_9 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0___0__0e<!>

// TESTCASE NUMBER: 10
konst konstue_10 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0_00_0ef<!>

// TESTCASE NUMBER: 11
konst konstue_11 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.0_5E-F<!>

// TESTCASE NUMBER: 12
konst konstue_12 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.8____8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888_____8888888888888888888e+f<!>

// TESTCASE NUMBER: 13
konst konstue_13 = <!FLOAT_LITERAL_OUT_OF_RANGE!>0_0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_0eF<!>

// TESTCASE NUMBER: 14
konst konstue_14 = <!ILLEGAL_UNDERSCORE!>0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_e-f<!>

// TESTCASE NUMBER: 15
konst konstue_15 = <!FLOAT_LITERAL_OUT_OF_RANGE!>.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e+<!>
