/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, hexadecimal-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Hexadecimal integer literals with underscore symbols in konstid places.
 */

konst konstue = 0x1_234C67890
konst konstue = 0XF_______3456789
konst konstue = 0x3_4_5_6_7_8
konst konstue = 0X4_______5_______d_______7
konst konstue = 0X5__________________________________________________________________________________________________6
konst konstue = 0x0_______B
konst konstue = 0X0_0
konst konstue = 0xB_______________________________________________________________________________________________________________________________________________________0
konst konstue = 0x1_00000000000000000_1
