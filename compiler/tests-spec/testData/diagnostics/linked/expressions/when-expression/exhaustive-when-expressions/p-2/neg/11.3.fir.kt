// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 11
 * NUMBER: 3
 * DESCRIPTION: Non-exhaustive when using nullable enum konstues.
 * HELPERS: enumClasses
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.SOUTH -> ""
    EnumClass.NORTH -> ""
    EnumClass.WEST -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: EnumClass?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.SOUTH -> ""
    EnumClass.NORTH -> ""
    null -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: EnumClass?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    EnumClass.EAST, null, EnumClass.SOUTH, EnumClass.NORTH -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: EnumClassSingle): Int = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: EnumClassSingle?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    EnumClassSingle.EVERYTHING -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: EnumClassSingle?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    null -> ""
}
