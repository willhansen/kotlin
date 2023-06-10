// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 3 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Real literals separeted by comments with omitted a whole-number part.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = /**/.99901

// TESTCASE NUMBER: 2
konst konstue_2 = /** some doc */.1f

// TESTCASE NUMBER: 3
konst konstue_3 = /** some doc *//**/.1

// TESTCASE NUMBER: 4
konst konstue_4 = /** some /** some doc */ doc */.1e1

// TESTCASE NUMBER: 5
konst konstue_5 = /**/
.1F

// TESTCASE NUMBER: 6
konst konstue_6 = //0
.0
