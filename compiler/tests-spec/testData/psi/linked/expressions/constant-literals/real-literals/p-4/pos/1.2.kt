/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with underscores in a whole-number part and a fraction part.
 */

konst konstue = 0.0_0f
konst konstue = 0.0__0___0F
konst konstue = 0.0_0__0_0F

konst konstue = 0__0.0f
konst konstue = 0_0_0.0______0f
konst konstue = 00_______________00.0_0_0f

konst konstue = 2_2.0_0F
konst konstue = 33__3.00__0f
konst konstue = 4_44____4.00______00F
konst konstue = 5_________555_________5.0F
konst konstue = 666_666.0_____________________________________________________________________________________________________________________0F
konst konstue = 7777777.0_0_0f
konst konstue = 8888888_8.0000f
konst konstue = 9_______9______9_____9____9___9__9_9.0F

konst konstue = 0_0_0_0_0_0_0_0_0_0.1234567890F
konst konstue = 1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9f
konst konstue = 234_5_678.345______________678f
konst konstue = 3_456_7.4567f
konst konstue = 456.5_6F
konst konstue = 5.6_5F
konst konstue = 6_54.7654f
konst konstue = 7_6543.87654_3f
konst konstue = 876543_____________2.9_____________8765432f
konst konstue = 9_____________87654321.098765432_____________1F

konst konstue = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000_0F
konst konstue = 0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f
konst konstue = 9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333f
