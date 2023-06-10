/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, the-types-for-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Various integer literals with a long literal mark in not allowed places.
 */

konst konstue = 0x0123456L789abcdef
konst konstue = 0lXal
konst konstue = 0xL0L
konst konstue = 0X4_______5_______d_______L7l
konst konstue = 0xl_l
konst konstue = 0b10101010101L0
konst konstue = 0bL000000009l
konst konstue = 0LB1___L____0____l___1____L___0
konst konstue = 0Bl1______________0
konst konstue = 0L0L
konst konstue = 1L0L
