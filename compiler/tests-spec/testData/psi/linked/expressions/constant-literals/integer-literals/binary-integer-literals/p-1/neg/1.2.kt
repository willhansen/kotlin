/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, integer-literals, binary-integer-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Binary integer literals with not allowed symbols.
 */

konst konstue = 0b101L001
konst konstue = 0bf101L001
konst konstue = 0bb1110010110
konst konstue = 0Bb11001011
konst konstue = 0bB100101
konst konstue = 0B00b10
konst konstue = 0B00B1
konst konstue = 0b10b0
konst konstue = 0bBb0100
konst konstue = 0BG
konst konstue = 0bF1z
konst konstue = 0b100M000
konst konstue = 0BBBB1000001
konst konstue = 0b00000010b
konst konstue = 0bABCDEFBB
konst konstue = 0Babcdefghijklmnopqrstuvwbyz
konst konstue = 0BABCDEFGHIJKLMNOPQRSTUVWBYZ
konst konstue = 0Bа
konst konstue = 0b10С10
konst konstue = 0beeeeеееее
konst konstue = 0bbbbbbb
konst konstue = 0B0BBBBBB
konst konstue = 0B0b0
konst konstue = 0bAF
