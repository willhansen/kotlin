/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 5 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: A type checking of a real literal with omitted a fraction part and an exponent mark.
 * HELPERS: checkType
 */

// TESTCASE NUMBER: 1
konst konstue_1 = 0e0 checkType { check<Double>() }

// TESTCASE NUMBER: 2
konst konstue_2 = 0_0e0_0 checkType { check<Double>() }

// TESTCASE NUMBER: 3
konst konstue_3 = 000E-10 checkType { check<Double>() }

// TESTCASE NUMBER: 4
konst konstue_4 = 00______00e+00000000000 checkType { check<Double>() }

// TESTCASE NUMBER: 5
konst konstue_5 = 0000000000000000000000000000000000000_0E1 checkType { check<Double>() }

// TESTCASE NUMBER: 6
konst konstue_6 = 1e1 checkType { check<Double>() }

// TESTCASE NUMBER: 7
konst konstue_7 = 2___2E-1 checkType { check<Double>() }

// TESTCASE NUMBER: 8
konst konstue_8 = 333e-00000000000 checkType { check<Double>() }

// TESTCASE NUMBER: 9
konst konstue_9 = <!FLOAT_LITERAL_CONFORMS_ZERO!>4444E-99999999999999999<!> checkType { check<Double>() }

// TESTCASE NUMBER: 10
konst konstue_10 = 5_5_5_5_5e10 checkType { check<Double>() }

// TESTCASE NUMBER: 11
konst konstue_11 = 666666E0_0_0_1_0 checkType { check<Double>() }

// TESTCASE NUMBER: 12
konst konstue_12 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>7777777e09090909090<!> checkType { check<Double>() }

// TESTCASE NUMBER: 13
konst konstue_13 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>88888888e1234567890<!> checkType { check<Double>() }

// TESTCASE NUMBER: 14
konst konstue_14 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>999999999E1234567890<!> checkType { check<Double>() }

// TESTCASE NUMBER: 15
konst konstue_15 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>123456789e9_____87___654__32_1<!> checkType { check<Double>() }

// TESTCASE NUMBER: 16
konst konstue_16 = 2345678E0 checkType { check<Double>() }

// TESTCASE NUMBER: 17
konst konstue_17 = 3____4___5__6_7E+010 checkType { check<Double>() }

// TESTCASE NUMBER: 18
konst konstue_18 = <!FLOAT_LITERAL_CONFORMS_ZERO!>456e-09876543210<!> checkType { check<Double>() }

// TESTCASE NUMBER: 19
konst konstue_19 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>5e5_0_5<!> checkType { check<Double>() }

// TESTCASE NUMBER: 20
konst konstue_20 = 654e5 checkType { check<Double>() }

// TESTCASE NUMBER: 21
konst konstue_21 = <!FLOAT_LITERAL_CONFORMS_ZERO!>76543E-91823<!> checkType { check<Double>() }

// TESTCASE NUMBER: 22
konst konstue_22 = 8765432e+9_______0 checkType { check<Double>() }

// TESTCASE NUMBER: 23
konst konstue_23 = 9_87654321e-1 checkType { check<Double>() }
