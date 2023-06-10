/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 4
 * DESCRIPTION: Real literals with omitted whole-number part and an exponent mark followed by a float suffix.
 */

konst konstue = .0fe10
konst konstue = .0F-e00
konst konstue = .0fEe+000
konst konstue = .0Fe+0000

konst konstue = .0fe+0
konst konstue = .00Fe00
konst konstue = .000fFEe-000

konst konstue = .0Fe+1
konst konstue = .00ffee22
konst konstue = .000Fe-0
konst konstue = .0000fFe4444
konst konstue = .0Fee-55555
konst konstue = .00FeE+666666
