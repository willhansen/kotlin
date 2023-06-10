/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Real literals with an omitted whole-number part and underscores in a whole-number part, a fraction part and an exponent part.
 */

konst konstue = .0_0
konst konstue = .0_0f
konst konstue = .0_0e-0_0
konst konstue = .0_0e0_0F
konst konstue = .0__0F
konst konstue = .0_0E+0__0_0F

konst konstue = .0e0f
konst konstue = .0_0E0_0

konst konstue = .0e1_0F
konst konstue = .0e10__0
konst konstue = .00______00F
konst konstue = .0___9
konst konstue = .0__________________________________________________12___________________________________________________________________0F
konst konstue = .0_0e+3_0
konst konstue = .000e0f
konst konstue = .9_______9______9_____9____9___9__9_90E-1

konst konstue = .12345678_90
konst konstue = .1_2_3_4_5_6_7_8_9_0
konst konstue = .345______________6e-7_______8f
konst konstue = .45_67f
konst konstue = .5e+0_6
konst konstue = .6_0______________05F
konst konstue = .76_5e4
konst konstue = .8E7654_3
konst konstue = .9E0_____________8765432f
konst konstue = .09_8765432_____________1F

konst konstue = .000000000000000000000000e-000000000000000000000000000000000000000000000000000000000000000_0F
konst konstue = .00___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
konst konstue = .33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f
