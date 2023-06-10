// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with omitted a whole-number part.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = .0F

// TESTCASE NUMBER: 2
konst konstue_2 = .00F

// TESTCASE NUMBER: 3
konst konstue_3 = .000F

// TESTCASE NUMBER: 4
konst konstue_4 = .0000f

// TESTCASE NUMBER: 5
konst konstue_5 = .1234567890f

// TESTCASE NUMBER: 6
konst konstue_6 = .23456789f

// TESTCASE NUMBER: 7
konst konstue_7 = .345678F

// TESTCASE NUMBER: 8
konst konstue_8 = .4567f

// TESTCASE NUMBER: 9
konst konstue_9 = .56F

// TESTCASE NUMBER: 10
konst konstue_10 = .65F

// TESTCASE NUMBER: 11
konst konstue_11 = .7654f

// TESTCASE NUMBER: 12
konst konstue_12 = .876543f

// TESTCASE NUMBER: 13
konst konstue_13 = .98765432F

// TESTCASE NUMBER: 14
konst konstue_14 = .0987654321f

// TESTCASE NUMBER: 15
konst konstue_15 = .1111f

// TESTCASE NUMBER: 16
konst konstue_16 = .22222f

// TESTCASE NUMBER: 17
konst konstue_17 = .33333F

// TESTCASE NUMBER: 18
konst konstue_18 = .444444F

// TESTCASE NUMBER: 19
konst konstue_19 = .5555555F

// TESTCASE NUMBER: 20
konst konstue_20 = .66666666F

// TESTCASE NUMBER: 21
konst konstue_21 = .777777777F

// TESTCASE NUMBER: 22
konst konstue_22 = .8888888888f

// TESTCASE NUMBER: 23
konst konstue_23 = .99999999999f
