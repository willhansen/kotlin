// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    funWithReturns(konstue_1 is String)
    println(konstue_1.length)
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    funWithReturns(konstue_1 != null)
    println(konstue_1.inc())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?) {
    funWithReturns(konstue_1 == null)
    println(konstue_1)
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    funWithReturnsAndTypeCheck(konstue_1)
    println(konstue_1.length)
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: String?) {
    funWithReturnsAndNotNullCheck(konstue_1)
    println(konstue_1.length)
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: String?) {
    funWithReturnsAndNullCheck(konstue_1)
    println(konstue_1)
}

// TESTCASE NUMBER: 7
object case_7_object {
    konst prop_1: Int? = 10
}
fun case_7() {
    funWithReturnsAndInvertCondition(case_7_object.prop_1 == null)
    case_7_object.prop_1.inc()
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?) {
    if (funWithReturnsTrue(konstue_1 is String)) println(konstue_1.length)
    if (funWithReturnsTrueAndInvertCondition(konstue_1 !is String)) println(konstue_1.length)
    if (!funWithReturnsFalse(konstue_1 is String)) println(konstue_1.length)
    if (!funWithReturnsFalseAndInvertCondition(konstue_1 !is String)) println(konstue_1.length)
    if (funWithReturnsNotNull(konstue_1 is String) != null) println(konstue_1.length)
    if (!(funWithReturnsNotNull(konstue_1 is String) == null)) println(konstue_1.length)
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: String?) {
    if (funWithReturnsTrue(konstue_1 != null)) println(konstue_1.length)
    if (funWithReturnsTrueAndInvertCondition(konstue_1 == null)) println(konstue_1.length)
    if (!funWithReturnsFalse(konstue_1 != null)) println(konstue_1.length)
    if (!funWithReturnsFalseAndInvertCondition(konstue_1 == null)) println(konstue_1.length)
    if (funWithReturnsNotNull(konstue_1 != null) != null) println(konstue_1.length)
    if (!(funWithReturnsNotNull(konstue_1 != null) == null)) println(konstue_1.length)
    if (!(funWithReturnsNotNullAndInvertCondition(konstue_1 == null) == null)) println(konstue_1.length)
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Any?) {
    if (funWithReturnsTrueAndTypeCheck(konstue_1)) println(konstue_1.length)
    if (!funWithReturnsFalseAndTypeCheck(konstue_1)) println(konstue_1.length)
    if (funWithReturnsNotNullAndTypeCheck(konstue_1) != null) println(konstue_1.length)
    if (!(funWithReturnsNotNullAndTypeCheck(konstue_1) == null)) println(konstue_1.length)
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Number?, konstue_2: Int?) {
    if (funWithReturnsTrueAndNotNullCheck(konstue_1)) println(konstue_1.toByte())
    if (funWithReturnsTrueAndNullCheck(konstue_1)) println(konstue_1)
    if (!funWithReturnsFalseAndNotNullCheck(konstue_2)) konstue_2.inc()
    if (!funWithReturnsFalseAndNotNullCheck(konstue_1)) println(konstue_1.toByte())
    if (!funWithReturnsFalseAndNullCheck(konstue_1)) println(konstue_1)
    if (!(funWithReturnsNotNullAndNotNullCheck(konstue_1) == null)) println(konstue_1.toByte())
    if (funWithReturnsNotNullAndNotNullCheck(konstue_1) != null) println(konstue_1.toByte())
    if (funWithReturnsNotNullAndNullCheck(konstue_1) != null) println(konstue_1)
}
