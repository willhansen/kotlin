/*
 * KOTLIN PSI SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals separeted by comments.
 */

konst konstue = 0/* comment */.000001
konst konstue = 9999/** some doc */.1

konst konstue = 4/**//** some doc */.1

konst konstue = 0/*
    ...
*/.000001
konst konstue = 9999/**
 some doc
 */.1
konst konstue = 9999// comment
.1
konst konstue = 9999/***/
.1

konst konstue = 1000/***/000.0
konst konstue = 1000/*.*/000.0

konst konstue = 4/** some/**/ doc */.1
konst konstue = 4/* some/***/ doc */.19999
