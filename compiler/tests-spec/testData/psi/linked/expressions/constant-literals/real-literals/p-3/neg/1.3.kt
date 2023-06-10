/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals suffixed by f/F (float suffix) with omitted whole-number part and not allowed several exponent marks.
 */

konst konstue = .0ee0
konst konstue = .0E-e-00F
konst konstue = .0eE+000
konst konstue = .0e+e0000f

konst konstue = .0E++e0f
konst konstue = .00e+E-00F
konst konstue = .000e-e000

konst konstue = .0ee+1
konst konstue = .00eE22
konst konstue = .000ee-333f
konst konstue = .0000e+E+e4444
konst konstue = .0eE-55555
konst konstue = .00eeeeeeeee666666F
konst konstue = .000e+E+e+E+e7777777
konst konstue = .0000eEeEeEe-88888888
konst konstue = .0EEEEEEEE+999999999

konst konstue = .0ee+F
konst konstue = .00eE
konst konstue = .000ee-f
konst konstue = .0000e+E+
konst konstue = .0eE-f
konst konstue = .00eeeeeeeef
konst konstue = .000e+E+e+E+e
konst konstue = .0000eEeEeEe-
konst konstue = .0EEEEEEEE+F
