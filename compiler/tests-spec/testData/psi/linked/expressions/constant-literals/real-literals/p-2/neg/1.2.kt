/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals suffixed by f/F (the float suffix) with not allowed several exponent marks.
 */

konst konstue = 0.0ee0f
konst konstue = 0.0E-e-00F
konst konstue = 0.0eE+000f
konst konstue = 0.0e+e0000F

konst konstue = 00.0E++e0f
konst konstue = 000.00e+E-00f
konst konstue = 0000.000e-e000f

konst konstue = 1.0ee+1F
konst konstue = 22.00eE22F
konst konstue = 333.000ee-333F
konst konstue = 4444.0000e+E+e4444f
konst konstue = 55555.0eE-55555f
konst konstue = 666666.00eeeeeeeee666666f
konst konstue = 7777777.000e+E+e+E+e7777777F
konst konstue = 88888888.0000eEeEeEe-88888888f
konst konstue = 999999999.0EEEEEEEE+999999999F

konst konstue = 1.0ee+f
konst konstue = 22.00eEf
konst konstue = 333.000ee-F
konst konstue = 4444.0000e+E+f
konst konstue = 55555.0eE-f
konst konstue = 666666.00eeeeeeeeF
konst konstue = 7777777.000e+E+e+E+eF
konst konstue = 88888888.0000eEeEeEe-f
konst konstue = 999999999.0EEEEEEEE+F
