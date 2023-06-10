/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals with a not allowed exponent mark at the beginning.
 */

konst konstue = E0
konst konstue = e000

konst konstue = E+0
konst konstue = e00

konst konstue = e+1
konst konstue = e22
konst konstue = E-333
konst konstue = e4444
konst konstue = e-55555
konst konstue = e666666
konst konstue = E7777777
konst konstue = e-88888888
konst konstue = E+999999999
