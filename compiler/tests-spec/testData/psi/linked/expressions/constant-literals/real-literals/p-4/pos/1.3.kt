/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with an exponent mark and underscores in a whole-number part, a fraction part and an exponent part.
 */

konst konstue = 0.0_0e1_0f
konst konstue = 0.0__0e-0___0
konst konstue = 0.0_0E-0__0_0F

konst konstue = 0__0.0e0f
konst konstue = 0_0_0.0_0E0_0
konst konstue = 00_______________00.0_0e+0_0

konst konstue = 2_2.0e1_0F
konst konstue = 33__3.0e10__0
konst konstue = 4_44____4.0E0______00F
konst konstue = 5_________555_________5.0e-9
konst konstue = 666_666.0__________________________________________________1E+2___________________________________________________________________0F
konst konstue = 7777777.0_0e3_0
konst konstue = 8888888_8.000e0f
konst konstue = 9_______9______9_____9____9___9__9_9.0E-1

konst konstue = 0_0_0_0_0_0_0_0_0_0.12345678e+90F
konst konstue = 1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9e-0
konst konstue = 234_5_678.345______________6e7_______8f
konst konstue = 3_456_7.45_6E7f
konst konstue = 456.5e0_6
konst konstue = 5.6_0E+05F
konst konstue = 6_54.76_5e-4
konst konstue = 7_6543.8E7654_3
konst konstue = 876543_____________2.9E+0_____________8765432f
konst konstue = 9_____________87654321.0e-9_8765432_____________1F

konst konstue = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000e000000000000000000000000000000000000000000000000000000000000000_0F
konst konstue = 0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0E-0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
konst konstue = 9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e3_3f
