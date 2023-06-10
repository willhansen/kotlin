/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with omitted a whole-number part and an exponent mark.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = .0e0f

// TESTCASE NUMBER: 2
konst konstue_2 = .0e-00F

// TESTCASE NUMBER: 3
konst konstue_3 = .0E000F

// TESTCASE NUMBER: 4
konst konstue_4 = .0E+0000f

// TESTCASE NUMBER: 5
konst konstue_5 = .0e+0f

// TESTCASE NUMBER: 6
konst konstue_6 = .00e00f

// TESTCASE NUMBER: 7
konst konstue_7 = .000E-000F

// TESTCASE NUMBER: 8
konst konstue_8 = .0E+1F

// TESTCASE NUMBER: 9
konst konstue_9 = .00e22F

// TESTCASE NUMBER: 10
konst konstue_10 = .345678e00000000001F

// TESTCASE NUMBER: 11
konst konstue_11 = .56e-0f

// TESTCASE NUMBER: 12
konst konstue_12 = .65e000000000000F

// TESTCASE NUMBER: 13
konst konstue_13 = .7654E+010f

// TESTCASE NUMBER: 14
konst konstue_14 = .876543E1f

// TESTCASE NUMBER: 15
konst konstue_15 = .98765432e-2f

// TESTCASE NUMBER: 16
konst konstue_16 = .0987654321E-3f

// TESTCASE NUMBER: 17
konst konstue_17 = .1111e4f

// TESTCASE NUMBER: 18
konst konstue_18 = .22222E-5F

// TESTCASE NUMBER: 19
konst konstue_19 = .33333e+6F

// TESTCASE NUMBER: 20
konst konstue_20 = .444444E7F

// TESTCASE NUMBER: 21
konst konstue_21 = .5555555e8f

// TESTCASE NUMBER: 22
konst konstue_22 = <!FLOAT_LITERAL_CONFORMS_ZERO!>.777777777E-308f<!>

// TESTCASE NUMBER: 23
konst konstue_23 = <!FLOAT_LITERAL_CONFORMS_ZERO!>.99999999999e-309F<!>

// TESTCASE NUMBER: 24
konst konstue_24 = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f

// TESTCASE NUMBER: 25
konst konstue_25 = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e-000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000F

// TESTCASE NUMBER: 26
konst konstue_26 = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e+000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f
