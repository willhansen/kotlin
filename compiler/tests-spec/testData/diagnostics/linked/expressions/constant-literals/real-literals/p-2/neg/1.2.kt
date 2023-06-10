/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, constant-literals, real-literals -> paragraph 2 -> sentence 1
 * NUMBER: 2
 * DESCRIPTION: Real literals with a not allowed exponent mark at the beginning.
 */

// TESTCASE NUMBER: 1
konst konstue_1 = <!UNRESOLVED_REFERENCE!>E0<!>

// TESTCASE NUMBER: 2
konst konstue_2 = <!UNRESOLVED_REFERENCE!>e000<!>

// TESTCASE NUMBER: 3
konst konstue_3 = <!UNRESOLVED_REFERENCE!>E<!><!DEBUG_INFO_MISSING_UNRESOLVED!>+<!>0

// TESTCASE NUMBER: 4
konst konstue_4 = <!UNRESOLVED_REFERENCE!>e00<!>

// TESTCASE NUMBER: 5
konst konstue_5 = <!UNRESOLVED_REFERENCE!>e<!><!DEBUG_INFO_MISSING_UNRESOLVED!>+<!>1

// TESTCASE NUMBER: 6
konst konstue_6 = <!UNRESOLVED_REFERENCE!>e22<!>

// TESTCASE NUMBER: 7
konst konstue_7 = <!UNRESOLVED_REFERENCE!>E<!><!DEBUG_INFO_MISSING_UNRESOLVED!>-<!>333

// TESTCASE NUMBER: 8
konst konstue_8 = <!UNRESOLVED_REFERENCE!>e4444<!>

// TESTCASE NUMBER: 9
konst konstue_9 = <!UNRESOLVED_REFERENCE!>e<!><!DEBUG_INFO_MISSING_UNRESOLVED!>-<!>55555

// TESTCASE NUMBER: 10
konst konstue_10 = <!UNRESOLVED_REFERENCE!>e666666<!>

// TESTCASE NUMBER: 11
konst konstue_11 = <!UNRESOLVED_REFERENCE!>E7777777<!>

// TESTCASE NUMBER: 12
konst konstue_12 = <!UNRESOLVED_REFERENCE!>e<!><!DEBUG_INFO_MISSING_UNRESOLVED!>-<!>88888888

// TESTCASE NUMBER: 13
konst konstue_13 = <!UNRESOLVED_REFERENCE!>E<!><!DEBUG_INFO_MISSING_UNRESOLVED!>+<!>999999999
