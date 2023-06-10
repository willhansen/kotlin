/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals with not allowed symbols as a separator of a whole-number part and a fraction part.
 * UNEXPECTED BEHAVIOUR
 */

konst konstue = 0...1
konst konstue = 1…1
konst konstue = 000:1
konst konstue = 2•0
konst konstue = 00·0
konst konstue = 300‚1
konst konstue = 0000°1
konst konstue = 1●1
konst konstue = 8☺10

konst konstue = 1. 2
konst konstue = 1 . 2
konst konstue = 1 .2
konst konstue = 1	.2
konst konstue = 1	.	2
konst konstue = 1.	2
konst konstue = 1
.2
konst konstue = 1.
2
konst konstue = 1.
2
konst konstue = 1
.2
konst konstue = 1.35
konst konstue = 3.5