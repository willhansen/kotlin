/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark, suffixed by f/F (float suffix).
 */

konst konstue = 0e0f
konst konstue = 00e00F
konst konstue = 000E-10f
konst konstue = 0000e+00000000000f
konst konstue = 00000000000000000000000000000000000000E1F

konst konstue = 1e1F
konst konstue = 22E-1f
konst konstue = 333e-00000000000F
konst konstue = 4444E-99999999999999999f
konst konstue = 55555e10f
konst konstue = 666666E00010F
konst konstue = 7777777e+09090909090F
konst konstue = 88888888e1234567890F
konst konstue = 999999999E1234567890f

konst konstue = 123456789e987654321F
konst konstue = 2345678E0f
konst konstue = 34567E+010f
konst konstue = 456e-09876543210F
konst konstue = 5e505f
konst konstue = 654e5F
konst konstue = 76543E-91823f
konst konstue = 8765432e+90F
konst konstue = 987654321e-1f
