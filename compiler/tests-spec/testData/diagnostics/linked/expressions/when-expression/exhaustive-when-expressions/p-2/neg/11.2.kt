// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 11
 * NUMBER: 2
 * DESCRIPTION: Non-exhaustive when using subclasses of the nullable sealed class.
 * HELPERS: sealedClasses
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: SealedClass?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
    is SealedChild3 -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    SealedMixedChildObject1 -> ""
    null -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    null, is SealedMixedChild1, is SealedMixedChild2, SealedMixedChildObject1 -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
    SealedMixedChildObject1 -> ""
    SealedMixedChildObject2 -> ""
    SealedMixedChildObject3 -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: SealedClassMixed?): Int = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {}<!>

// TESTCASE NUMBER: 7
fun case_7(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2-> ""
    is SealedMixedChild3 -> ""
    null -> ""
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: SealedClassMixed?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    SealedMixedChildObject1 -> ""
}

/*
 * TESTCASE NUMBER: 9
 * DISCUSSION: maybe make exhaustive without else?
 */
fun case_9(konstue_1: Any?): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
    is Any -> ""
    null -> ""
}

/*
 * TESTCASE NUMBER: 10
 * DISCUSSION
 * ISSUES: KT-26044
 */
fun case_10(konstue: SealedClassEmpty): String = <!TYPE_MISMATCH!><!NO_ELSE_IN_WHEN!>when<!> (konstue) {}<!>
