// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 11
 * NUMBER: 2
 * DESCRIPTION: Exhaustive when using nullable enum konstues.
 * HELPERS: enumClasses
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass?): String = when (konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
    null -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: EnumClassSingle?): String = when (konstue_1) {
    EnumClassSingle.EVERYTHING -> ""
    null -> ""
}

/*
 * TESTCASE NUMBER: 3
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26044
 */
fun case_3(konstue_1: EnumClassEmpty?): String = <!NO_ELSE_IN_WHEN!>when<!>(konstue_1) {
    null -> ""
}
