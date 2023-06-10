// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    funWithReturns(konstue_1 !is String)
    <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    funWithReturnsAndInvertCondition(konstue_1 != null)
    println(konstue_1.inc()) // inc resolves to compiler/tests-spec/testData/diagnostics/helpers/classes.kt which accepts `Class?`
    println(konstue_1<!UNSAFE_CALL!>.<!>unaryPlus())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?) {
    funWithReturns(konstue_1 == null)
    println(konstue_1.inc()) // inc resolves to compiler/tests-spec/testData/diagnostics/helpers/classes.kt which accepts `Class?`
    println(konstue_1<!UNSAFE_CALL!>.<!>unaryPlus())
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    funWithReturnsAndInvertTypeCheck(konstue_1)
    <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: String?) {
    funWithReturnsAndNullCheck(konstue_1)
    println(konstue_1<!UNSAFE_CALL!>.<!>length)
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: String?) {
    funWithReturnsAndNullCheck(konstue_1)
    println(konstue_1<!UNSAFE_CALL!>.<!>length)
}

// TESTCASE NUMBER: 7
object case_7_object {
    konst prop_1: Int? = 10
}
fun case_7() {
    funWithReturns(case_7_object.prop_1 == null)
    case_7_object.prop_1.inc() // inc resolves to compiler/tests-spec/testData/diagnostics/helpers/classes.kt which accepts `Class?`
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?) {
    if (!funWithReturnsTrue(konstue_1 is String)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 !is String)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalse(konstue_1 is String)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalseAndInvertCondition(konstue_1 !is String)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsNotNull(konstue_1 is String) == null) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNotNull(konstue_1 is String) != null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNull(konstue_1 is String) == null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsNull(konstue_1 is String) != null) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: String?) {
    if (!funWithReturnsTrue(konstue_1 != null)) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 == null)) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsFalse(konstue_1 != null)) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsFalseAndInvertCondition(konstue_1 == null)) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsNotNull(konstue_1 != null) == null) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsNotNullAndInvertCondition(konstue_1 == null) == null) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsNull(konstue_1 != null) != null) println(konstue_1<!UNSAFE_CALL!>.<!>length)
    if (funWithReturnsNullAndInvertCondition(konstue_1 == null) != null) println(konstue_1<!UNSAFE_CALL!>.<!>length)
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Any?) {
    if (!funWithReturnsTrueAndTypeCheck(konstue_1)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!!funWithReturnsFalseAndTypeCheck(konstue_1)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNotNullAndTypeCheck(konstue_1) != null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!!(funWithReturnsNotNullAndTypeCheck(konstue_1) == null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!!(funWithReturnsNullAndTypeCheck(konstue_1) != null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNullAndTypeCheck(konstue_1) == null)) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Number?) {
    if (!funWithReturnsTrueAndNotNullCheck(konstue_1)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (!funWithReturnsTrueAndNullCheck(konstue_1)) println(konstue_1)
    if (funWithReturnsFalseAndNotNullCheck(konstue_1)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (funWithReturnsFalseAndNullCheck(konstue_1)) println(konstue_1)
    if ((funWithReturnsNotNullAndNotNullCheck(konstue_1) == null)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (!!!(funWithReturnsNotNullAndNotNullCheck(konstue_1) != null)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (!!(funWithReturnsNotNullAndNullCheck(konstue_1) == null)) println(konstue_1)
    if (!(funWithReturnsNullAndNotNullCheck(konstue_1) == null)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (!!(funWithReturnsNullAndNotNullCheck(konstue_1) != null)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (!!!(funWithReturnsNullAndNullCheck(konstue_1) == null)) println(konstue_1)
}
