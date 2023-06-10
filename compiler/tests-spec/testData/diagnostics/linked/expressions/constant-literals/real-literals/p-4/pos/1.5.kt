/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: Real literals with an omitted fraction part and underscores in a whole-number part, a fraction part and an exponent part.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = 0_0F

// TESTCASE NUMBER: 2
konst konstue_2 = 0_0E-0_0F

// TESTCASE NUMBER: 3
konst konstue_3 = 0_0E-0_0

// TESTCASE NUMBER: 4
konst konstue_4 = 0_0____0f

// TESTCASE NUMBER: 5
konst konstue_5 = 0_0____0e-0f

// TESTCASE NUMBER: 6
konst konstue_6 = 0_0_0_0F

// TESTCASE NUMBER: 7
konst konstue_7 = 0_0_0_0E-0_0_0_0F

// TESTCASE NUMBER: 8
konst konstue_8 = 0000000000000000000_______________0000000000000000000f

// TESTCASE NUMBER: 9
konst konstue_9 = 0000000000000000000_______________0000000000000000000e+0f

// TESTCASE NUMBER: 10
konst konstue_10 = 0000000000000000000_______________0000000000000000000E-0

// TESTCASE NUMBER: 11
konst konstue_11 = 2___2e-2___2f

// TESTCASE NUMBER: 12
konst konstue_12 = 33_3E0_0F

// TESTCASE NUMBER: 13
konst konstue_13 = <!FLOAT_LITERAL_CONFORMS_ZERO!>4_444E-4_444f<!>

// TESTCASE NUMBER: 14
konst konstue_14 = 55_5_55F

// TESTCASE NUMBER: 15
konst konstue_15 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666f

// TESTCASE NUMBER: 16
konst konstue_16 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666E-10

// TESTCASE NUMBER: 17
konst konstue_17 = 7_7_7_7_7_7_7f

// TESTCASE NUMBER: 18
konst konstue_18 = <!FLOAT_LITERAL_CONFORMS_ZERO!>8888888________8e-9000000_0<!>

// TESTCASE NUMBER: 19
konst konstue_19 = 9________9_______9______9_____9____9___9__9_9F

// TESTCASE NUMBER: 20
konst konstue_20 = 1__2_3__4____5_____6__7_89f

// TESTCASE NUMBER: 21
konst konstue_21 = 2__34567e8

// TESTCASE NUMBER: 22
konst konstue_22 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>345_6E+9_7F<!>

// TESTCASE NUMBER: 23
konst konstue_23 = <!FLOAT_LITERAL_CONFORMS_ZERO!>45_____________________________________________________________6E-12313413_4<!>

// TESTCASE NUMBER: 24
konst konstue_24 = 5_______________________________________________________________________________________________________________________________________________________________________________________5f

// TESTCASE NUMBER: 25
konst konstue_25 = 6__________________________________________________54F

// TESTCASE NUMBER: 26
konst konstue_26 = <!FLOAT_LITERAL_CONFORMS_INFINITY!>76_5___4e3___________33333333<!>

// TESTCASE NUMBER: 27
konst konstue_27 = 876543_____________________________________________________________2f

// TESTCASE NUMBER: 28
konst konstue_28 = 9_8__7654__3_21F

// TESTCASE NUMBER: 29
konst konstue_29 = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0__0F

// TESTCASE NUMBER: 30
konst konstue_30 = 0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f

// TESTCASE NUMBER: 31
konst konstue_31 = 33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E-1_0_0
