/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 5
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with a not allowed exponent mark at the beginning.
 */

konst konstue = E0f
konst konstue = e000F

konst konstue = E+0f
konst konstue = e00f

konst konstue = e+1F
konst konstue = e22F
konst konstue = E-333F
konst konstue = e4444f
konst konstue = e-55555f
konst konstue = e666666F
konst konstue = E7777777f
konst konstue = e-88888888F
konst konstue = E+999999999F
