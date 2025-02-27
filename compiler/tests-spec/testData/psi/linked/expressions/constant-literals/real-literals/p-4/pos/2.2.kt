/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 4 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Real literals with underscores after an exponent mark.
 */

konst konstue = 0e___0
konst konstue = .0e_0
konst konstue = 0.1e__2
konst konstue = 2.1e_2
konst konstue = 1E-_2F
konst konstue = 3e+_0f
konst konstue = 5E_-0
konst konstue = 5e_-0F
konst konstue = 5e_00000000

konst konstue = 0.0_0e_1_0f
konst konstue = 0.0_0E____-0__0_0F

konst konstue = .0_0E__0_0
konst konstue = 00_______________00.0_0e+__0_0

konst konstue = .0e-__1_0F
konst konstue = 33__3.0e_10__0
konst konstue = .0E_0______00F
konst konstue = 666_666.0__________________________________________________1E+_2___________________________________________________________________0F
konst konstue = 8888888_8.000e_____0f
konst konstue = 9_______9______9_____9____9___9__9_9.0E__-1

konst konstue = 0_0_0_0_0_0_0_0_0_0.12345678e+__90F
konst konstue = 1_2_3_4_5_6_7_8_9.2_3_4_5_6_7_8_9e_-_0
konst konstue = .345______________6e_____7_______8f
konst konstue = .45_6E_7f
konst konstue = 6_54.765e-_4
konst konstue = 7_6543.8E____7654_3
konst konstue = .9E+_______0_____________8765432f

konst konstue = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000___0.000000000000000000000000e_000000000000000000000000000000000000000000000000000000000000000_0F
konst konstue = 0_000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0E-_0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
konst konstue = 9999999999999999999999999999999999999999999_______________999999999999999999999999999999999999999999999.33333333333333333333333333333333333333333333333_333333333333333333333333333333333333333e___3_3f
