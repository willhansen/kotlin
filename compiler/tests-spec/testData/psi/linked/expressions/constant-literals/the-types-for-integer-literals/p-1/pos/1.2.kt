/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Decimal integer literals with underscores suffixed by the long literal mark.
 */

konst konstue = 1234_5678_90L
konst konstue = 1_2_3_4_5_6_7_8_9_0L
konst konstue = 1_2L
konst konstue = 1_00000000000000000_1L
konst konstue = 1_____________2L
konst konstue = 9_____________0000L
konst konstue = 9____________0_0000L
konst konstue = 1_______________________________________________________________________________________________________________________________________________________0L

konst konstue = 1_L
konst konstue = 1_00000000000000000_L
konst konstue = 1_____________L
konst konstue = 9____________0_L
konst konstue = 1_______________________________________________________________________________________________________________________________________________________L
