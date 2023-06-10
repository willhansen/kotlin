/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 3
 * NUMBER: 3
 * DESCRIPTION: Real literals with a float suffix and not allowed symbols as a separator of a whole-number part and a fraction part.
 * UNEXPECTED BEHAVIOUR
 */

konst konstue = 0...1f
konst konstue = 1…1f
konst konstue = 000:1F
konst konstue = 2•0F
konst konstue = 00·0f
konst konstue = 300‚1F
konst konstue = 0000°1f
konst konstue = 1●1F
konst konstue = 8☺10f

konst konstue = 1. 2f
konst konstue = 1 . 2f
konst konstue = 1 .2F
konst konstue = 1	.2f
konst konstue = 1	.	2F
konst konstue = 1.	2f
konst konstue = 1
.2F
konst konstue = 1.
    2F
konst konstue = 1.
    2f
konst konstue = 1
.2f
konst konstue = 1.35f
konst konstue = 3.5F