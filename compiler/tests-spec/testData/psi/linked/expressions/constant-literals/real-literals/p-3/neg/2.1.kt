/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Real literals with omitted fraction part and digits followed by a float suffix.
 */

konst konstue = 1F0
konst konstue = 22f1019230912904
konst konstue = 333e-00000000000F12903490
konst konstue = 4444E-99999999999999999f000000000000000000
konst konstue = 7777777e09090909090F0
konst konstue = 88888888e-1f1
konst konstue = 999999999EF0

konst konstue = 123456789e987654321F999999999999999999999
konst konstue = 2345678E0f0
konst konstue = 5e50501f011
konst konstue = 654e5F10
konst konstue = 76543f00000
konst konstue = 8765432F010
konst konstue = 987654321f100
