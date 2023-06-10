/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals with dots at the beginning and exponent mark/float suffix right after it.
 */

konst konstue = .f
konst konstue = ..F
konst konstue = .e10
konst konstue = .+e1

konst konstue = ..-E10F
konst konstue = ...+e000000F
konst konstue = ..e1f1
konst konstue = ...E0000000000
