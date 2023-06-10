/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Real literals with not allowed several exponent marks.
 */

konst konstue = 0.0ee0
konst konstue = 0.0E-e-00
konst konstue = 0.0eE+000
konst konstue = 0.0e+e0000

konst konstue = 00.0E++e0
konst konstue = 000.00e+E-00
konst konstue = 0000.000e-e000

konst konstue = 1.0ee+1
konst konstue = 22.00eE22
konst konstue = 333.000ee-333
konst konstue = 4444.0000e+E+e4444
konst konstue = 55555.0eE-55555
konst konstue = 666666.00eeeeeeeee666666
konst konstue = 7777777.000e+E+e+E+e7777777
konst konstue = 88888888.0000eEeEeEe-88888888
konst konstue = 999999999.0EEEEEEEE+999999999

konst konstue = 1.0ee+
konst konstue = 22.00eE
konst konstue = 333.000ee-
konst konstue = 4444.0000e+E+
konst konstue = 55555.0eE-
konst konstue = 666666.00eeeeeeee
konst konstue = 7777777.000e+E+e+E+e
konst konstue = 88888888.0000eEeEeEe-
konst konstue = 999999999.0EEEEEEEE+
