// FIR_IDENTICAL
/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Real literals separeted by comments.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = 0./**/<!ILLEGAL_SELECTOR!>99901<!>

// TESTCASE NUMBER: 2
konst konstue_2 = 2./** some doc */<!ILLEGAL_SELECTOR!>1<!>

// TESTCASE NUMBER: 3
konst konstue_3 = 9999./** some doc *//**/<!ILLEGAL_SELECTOR!>1<!>

// TESTCASE NUMBER: 4
konst konstue_4 = 9999./** some /** some doc */ doc */<!ILLEGAL_SELECTOR!>1<!>

// TESTCASE NUMBER: 5
konst konstue_5 = 9999./**/
<!ILLEGAL_SELECTOR!>1<!>

// TESTCASE NUMBER: 6
konst konstue_6 = 1000000.//0
<!ILLEGAL_SELECTOR!>0<!>
