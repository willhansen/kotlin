// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean, konstue_2: Long): Int {
    return when {
        konstue_1 -> 1
        getBoolean() && konstue_1 -> 2
        getChar() != 'a' -> 3
        Out<Int>() === getAny() -> 4
        konstue_2 <= 11 -> 5
        !konstue_1 -> 6
        else -> 7
    }
}

/*
 * TESTCASE NUMBER: 2
 * NOTE: for a potential analysys of exhaustiveness by enums in whens without a bound konstue.
 */
fun case_2(konstue_1: EnumClass) {
    when {
        konstue_1 == EnumClass.NORTH -> {}
        konstue_1 == EnumClass.SOUTH -> {}
        konstue_1 == EnumClass.WEST -> {}
        konstue_1 == EnumClass.EAST -> {}
    }
}

/*
 * TESTCASE NUMBER: 3
 * NOTE: for a potential analysys of exhaustiveness by enums in whens without a bound konstue.
 */
fun case_3(konstue_1: Boolean) {
    when {
        konstue_1 == true -> return
        konstue_1 == false -> return
    }
}

/*
 * TESTCASE NUMBER: 4
 * NOTE: for a potential mark the code after the true branch as unreacable.
 */
fun case_4(konstue_1: Boolean) {
    when {
        false -> return
        true -> return
        konstue_1 -> return
    }
}

/*
 * TESTCASE NUMBER: 5
 * NOTE: for a potential const propagation.
 */
fun case_5(konstue_1: Boolean) {
    konst konstue_2 = false
    konst konstue_3 = false || !!!false || false

    when {
        konstue_3 -> return
        konstue_2 -> return
        konstue_1 -> return
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any) {
    when {
        konstue_1 is Nothing -> {}
        konstue_1 is Int -> {}
        konstue_1 is Boolean -> {}
        konstue_1 is String -> {}
        konstue_1 is Number -> {}
        konstue_1 is Float -> {}
        <!USELESS_IS_CHECK!>konstue_1 is Any<!> -> {}
    }
}

/*
 * TESTCASE NUMBER: 7
 * NOTE: for a potential analysys of exhaustiveness by enums in whens without a bound konstue.
 */
fun case_7(konstue_1: Any) {
    when {
        konstue_1 !is Number -> {}
        konstue_1 is Float -> {}
        <!USELESS_IS_CHECK!>konstue_1 is Number<!> -> {}
        <!USELESS_IS_CHECK!>konstue_1 is Any<!> -> {}
    }
}

/*
 * TESTCASE NUMBER: 8
 * NOTE: for a potential analysys of exhaustiveness by enums in whens without a bound konstue.
 */
fun case_8(konstue_1: SealedClass) {
    when {
        konstue_1 is SealedChild1 -> {}
        konstue_1 is SealedChild2 -> {}
        konstue_1 is SealedChild3 -> {}
    }
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Int, konstue_2: IntRange) {
    when {
        konstue_1 in -10..100L -> {}
        konstue_1 in konstue_2 -> {}
        konstue_1 !in listOf(0, 1, 2) -> {}
    }
}
