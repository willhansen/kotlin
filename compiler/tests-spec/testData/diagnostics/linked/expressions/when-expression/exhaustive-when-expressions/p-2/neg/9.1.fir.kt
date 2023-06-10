// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 9
 * NUMBER: 1
 * DESCRIPTION: Non-exhaustive when using subclasses of the sealed class.
 * HELPERS: sealedClasses
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: SealedClass): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: SealedClass): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedChild1, is SealedChild2 -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: SealedClassMixed): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    SealedMixedChildObject1 -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: SealedClassMixed): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    SealedMixedChildObject1, is SealedMixedChild2, is SealedMixedChild1 -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: SealedClassMixed): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: SealedClassMixed): Int = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) { }

// TESTCASE NUMBER: 7
fun case_7(konstue_1: SealedClassSingleWithObject): Int = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) { }

// TESTCASE NUMBER: 8
fun case_8(konstue_1: SealedClassEmpty): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) { }

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Number): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
    is Byte -> ""
    is Double -> ""
    is Float -> ""
    is Int -> ""
    is Long -> ""
    is Short -> ""
}

/*
 * TESTCASE NUMBER: 10
 * DISCUSSION: maybe make exhaustive without else?
 */
fun case_10(konstue_1: Any): String = <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
    <!USELESS_IS_CHECK!>is Any<!> -> ""
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: SealedClass): String = <!NO_ELSE_IN_WHEN!>when<!> {
    konstue_1 is SealedChild1 -> ""
    konstue_1 is SealedChild2 -> ""
    konstue_1 is SealedChild3 -> ""
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: SealedClassMixed): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    is SealedMixedChild1 -> ""
    is SealedMixedChild2 -> ""
    is SealedMixedChild3 -> ""
}
