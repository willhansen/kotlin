// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression, exhaustive-when-expressions -> paragraph 2 -> sentence 1
 * NUMBER: 3
 * DESCRIPTION: Exhaustive when, with bound konstue (sealed, enum, boolean), with redundant else branch.
 * HELPERS: enumClasses, sealedClasses
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass): String = when (konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: EnumClass?): String = when (konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
    null -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Boolean): String = when (konstue_1) {
    true -> ""
    false -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Boolean?): String = when (konstue_1) {
    true -> ""
    false -> ""
    null -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: SealedClass): String = when (konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
    is SealedChild3 -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: SealedClass?): String = when (konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
    is SealedChild3 -> ""
    null -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: SealedClassSingle): String = when (konstue_1) {
    <!USELESS_IS_CHECK!>is SealedClassSingle<!> -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: SealedClassSingle?): String = when (konstue_1) {
    is SealedClassSingle -> ""
    null -> ""
    <!REDUNDANT_ELSE_IN_WHEN!>else<!> -> ""
}