/*
 * KOTLIN PSI SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 6
 * DESCRIPTION: Real literals with omitted a whole-number part, suffixed by f/F (float suffix) followed by an exponent mark without digits.
 */

konst konstue = .0EF
konst konstue = .0ef
konst konstue = .00e-f
konst konstue = .000E+F

konst konstue = .0eF
konst konstue = .00E+f
konst konstue = .000ef
konst konstue = .0000Ef
konst konstue = .0e+F
konst konstue = .00E-F
konst konstue = .000eF
konst konstue = .0000ef
konst konstue = .0E-F

konst konstue = .888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888e+f
konst konstue = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000eF
konst konstue = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e-f
konst konstue = .000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e+F
