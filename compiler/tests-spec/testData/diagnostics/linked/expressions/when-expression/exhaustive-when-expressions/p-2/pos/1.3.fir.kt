// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass): String = when (konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
    else -> ""
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: EnumClass?): String = when (konstue_1) {
    EnumClass.EAST -> ""
    EnumClass.NORTH -> ""
    EnumClass.SOUTH -> ""
    EnumClass.WEST -> ""
    null -> ""
    else -> ""
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Boolean): String = when (konstue_1) {
    true -> ""
    false -> ""
    else -> ""
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Boolean?): String = when (konstue_1) {
    true -> ""
    false -> ""
    null -> ""
    else -> ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: SealedClass): String = when (konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
    is SealedChild3 -> ""
    else -> ""
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: SealedClass?): String = when (konstue_1) {
    is SealedChild1 -> ""
    is SealedChild2 -> ""
    is SealedChild3 -> ""
    null -> ""
    else -> ""
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: SealedClassSingle): String = when (konstue_1) {
    <!USELESS_IS_CHECK!>is SealedClassSingle<!> -> ""
    else -> ""
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: SealedClassSingle?): String = when (konstue_1) {
    is SealedClassSingle -> ""
    null -> ""
    else -> ""
}
