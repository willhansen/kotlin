/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 1
 * DESCRIPTION: Simple real literals with omitted a fraction part, suffixed by f/F (float suffix).
 */

konst konstue = 0F
konst konstue = 00F
konst konstue = 000f
konst konstue = 0000F
konst konstue = 00000000000000000000000000000000000000f

konst konstue = 1f
konst konstue = 22f
konst konstue = 333F
konst konstue = 4444f
konst konstue = 55555F
konst konstue = 666666f
konst konstue = 7777777f
konst konstue = 88888888F
konst konstue = 999999999F

konst konstue = 123456789f
konst konstue = 2345678F
konst konstue = 34567F
konst konstue = 456F
konst konstue = 5f
konst konstue = 654F
konst konstue = 76543F
konst konstue = 8765432f
konst konstue = 987654321F
