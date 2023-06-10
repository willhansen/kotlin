/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = 0e0

// TESTCASE NUMBER: 2
konst konstue_2 = 00e00

// TESTCASE NUMBER: 3
konst konstue_3 = 000E-10

// TESTCASE NUMBER: 4
konst konstue_4 = 0000e+00000000000

// TESTCASE NUMBER: 5
konst konstue_5 = 00000000000000000000000000000000000000E1

// TESTCASE NUMBER: 6
konst konstue_6 = 1e1

// TESTCASE NUMBER: 7
konst konstue_7 = 22E-1

// TESTCASE NUMBER: 8
konst konstue_8 = 333e-00000000000

// TESTCASE NUMBER: 9
konst konstue_9 = <!FLOAT_LITERAL_CONFORMS_ZERO!>4444E-99999999999999999<!>

// TESTCASE NUMBER: 10
konst konstue_10 = 55555e10

// TESTCASE NUMBER: 11
konst konstue_11 = 666666E00010

// TESTCASE NUMBER: 12
konst konstue_12 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>7777777e09090909090<!>

// TESTCASE NUMBER: 13
konst konstue_13 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>88888888e1234567890<!>

// TESTCASE NUMBER: 14
konst konstue_14 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>999999999E1234567890<!>

// TESTCASE NUMBER: 15
konst konstue_15 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>123456789e987654321<!>

// TESTCASE NUMBER: 16
konst konstue_16 = 2345678E0

// TESTCASE NUMBER: 17
konst konstue_17 = 34567E+010

// TESTCASE NUMBER: 18
konst konstue_18 = <!FLOAT_LITERAL_CONFORMS_ZERO!>456e-09876543210<!>

// TESTCASE NUMBER: 19
konst konstue_19 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>5e505<!>

// TESTCASE NUMBER: 20
konst konstue_20 = 654e5

// TESTCASE NUMBER: 21
konst konstue_21 = <!FLOAT_LITERAL_CONFORMS_ZERO!>76543E-91823<!>

// TESTCASE NUMBER: 22
konst konstue_22 = 8765432e+90

// TESTCASE NUMBER: 23
konst konstue_23 = 987654321e-1
