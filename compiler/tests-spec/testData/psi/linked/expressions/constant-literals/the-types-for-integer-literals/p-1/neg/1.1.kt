/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Various integer literals with a long literal mark doublicate.
 */

konst konstue = 1234567890lL
konst konstue = 1Ll
konst konstue = 1_ll
konst konstue = 1234_5678_90LLLLLL
konst konstue = 0x1llllll
konst konstue = 0B0LlLlLl
