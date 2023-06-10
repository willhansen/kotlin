/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Binary integer literals with underscore breaking the prefix (in it).
 */

konst konstue = 0_b1_1_0_1_0_1
konst konstue = 0_B_______1_______0_______1_______0
konst konstue = 0_0B1_1_1_0_1_0_1_0
konst konstue = 0_0B000000000
konst konstue = 0_0000000000B
konst konstue = 0_0b
konst konstue = 0____________0b
konst konstue = 0_0_b_0
konst konstue = 0_b_0
konst konstue = 0_b
konst konstue = 0_b_
konst konstue = 0_b_0_
