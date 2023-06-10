/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 2
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark.
 */

konst konstue = 0e0
konst konstue = 00e00
konst konstue = 000E-10
konst konstue = 0000e+00000000000
konst konstue = 00000000000000000000000000000000000000E1

konst konstue = 1e1
konst konstue = 22E-1
konst konstue = 333e-00000000000
konst konstue = 4444E-99999999999999999
konst konstue = 55555e10
konst konstue = 666666E00010
konst konstue = 7777777e09090909090
konst konstue = 88888888e1234567890
konst konstue = 999999999E1234567890

konst konstue = 123456789e987654321
konst konstue = 2345678E0
konst konstue = 34567E+010
konst konstue = 456e-09876543210
konst konstue = 5e505
konst konstue = 654e5
konst konstue = 76543E-91823
konst konstue = 8765432e+90
konst konstue = 987654321e-1
