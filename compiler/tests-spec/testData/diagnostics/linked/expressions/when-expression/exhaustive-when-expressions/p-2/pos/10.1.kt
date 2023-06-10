// FIR_IDENTICAL
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 10
 * NUMBER: 1
 * DESCRIPTION: Exhaustive when using enum konstues.
 * HELPERS: enumClasses
 */

// TESTCASE NUMBER: 1
fun case_1(dir: EnumClass): String = when (dir) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: EnumClassSingle): String = when (konstue_1) {
    EnumClassSingle.EVERYTHING -> ""
}
