/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals with a not allowed exponent mark with digits followed by a float suffix.
 */

konst konstue = 0.0fe10
konst konstue = 0.0F-e00
konst konstue = 0.0fEe+000
konst konstue = 0.0Fe+0000

konst konstue = 00.0fe+0
konst konstue = 000.00Fe00
konst konstue = 0000.000fFEe-000

konst konstue = 1.0Fe+1
konst konstue = 22.00ffee22
konst konstue = 333.000Fe-0
konst konstue = 4444.0000fFe4444
konst konstue = 55555.0Fee-55555
konst konstue = 666666.00FeE+666666
