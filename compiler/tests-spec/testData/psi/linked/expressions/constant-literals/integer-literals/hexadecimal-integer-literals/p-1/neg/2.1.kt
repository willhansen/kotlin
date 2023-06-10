/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Hexadecimal integer literals with underscore breaking the prefix (in it).
 */

konst konstue = 0_x3_4_5_6_7_8
konst konstue = 0_X_______4_______5_______6_______7
konst konstue = 0_0X4_3_4_5_6_7_8_9
konst konstue = 0_0X000000000
konst konstue = 0_0000000000X
konst konstue = 0_9x
konst konstue = 0____________0x
konst konstue = 0_0_x_0
konst konstue = 0_x_0
konst konstue = 0_x
konst konstue = 0_x_
konst konstue = 0_x_0_
