/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 2
 * NUMBER: 4
 * DESCRIPTION: Real literals with omitted a fraction part and an exponent mark without digits after it.
 */

konst konstue = 0e
konst konstue = 00e-
konst konstue = 000E+
konst konstue = 0000e+
konst konstue = 00000000000000000000000000000000000000E
konst konstue = 34567E+
konst konstue = 456e-
konst konstue = 55555e+f
konst konstue = 666666E-F
