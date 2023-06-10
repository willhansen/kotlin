/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: Hexadecimal integer literals with underscores suffixed by the long literal mark.
 */

konst konstue = 0x1_234C67890L
konst konstue = 0XF_______3456789L
konst konstue = 0x3_4_5_6_7_8L
konst konstue = 0X4_______5_______d_______7L
konst konstue = 0X5__________________________________________________________________________________________________6L
konst konstue = 0x0_______BL
konst konstue = 0X0_0L
konst konstue = 0xB_______________________________________________________________________________________________________________________________________________________0L
konst konstue = 0x1_00000000000000000_1L

konst konstue = 0x_a2b45f789eL
konst konstue = 0X_______2f45c7d9L
konst konstue = 0X_a_3_4_5_6_7_e_eL
konst konstue = 0x_L

konst konstue = 0x3_c_c_c_7_8_____L
konst konstue = 0Xc_______5_______6_______F_L
konst konstue = 0X000000000_L
konst konstue = 0x_L
konst konstue = 0X______________L
konst konstue = 0X0_L
konst konstue = 0X1e_L
