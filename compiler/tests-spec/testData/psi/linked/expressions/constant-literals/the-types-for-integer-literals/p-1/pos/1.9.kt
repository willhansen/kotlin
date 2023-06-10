/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 9
 * DESCRIPTION: Various integer literals with long literal mark in not allowed lower case (but konstid in opinion the parser).
 */

konst konstue = 1234567890l
konst konstue = 1l
konst konstue = 1_l
konst konstue = 1234_5678_90l
konst konstue = 0x0123456789abcdefl
konst konstue = 0x1l
konst konstue = 0Xal
konst konstue = 0xA0Al
konst konstue = 0xl
konst konstue = 0X4_______5_______d_______7l
konst konstue = 0x_l
konst konstue = 0b10101010101l
konst konstue = 0b000000009l
konst konstue = 0bl
konst konstue = 0B1_______0_______1_______0_l
konst konstue = 0B______________l
