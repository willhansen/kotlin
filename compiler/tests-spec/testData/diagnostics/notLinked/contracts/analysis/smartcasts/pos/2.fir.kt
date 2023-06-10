// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?, konstue_2: Any?) {
    funWithReturns(konstue_1 is String && konstue_2 is Number)
    println(konstue_1.length)
    println(konstue_2.toByte())
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?, konstue_2: Any?) {
    funWithReturnsAndInvertCondition(konstue_1 !is String || konstue_2 !is Number)
    println(konstue_1.length)
    println(konstue_2.toByte())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?, konstue_2: Any?) {
    funWithReturnsAndInvertCondition(konstue_1 !is String || konstue_2 != null)
    println(konstue_1.length)
    <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?, konstue_2: Number?) {
    funWithReturns(konstue_1 is Float? && konstue_1 != null && konstue_2 != null)
    println(konstue_1.dec())
    println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
}

// TESTCASE NUMBER: 5
class case_5_class {
    konst prop_1: Int? = 10

    fun case_5(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_5_class()
        funWithReturns(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null)
        println(konstue_1.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
        println(o.prop_1.plus(3))
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any?, konstue_2: Any) {
    if (funWithReturnsTrue(konstue_1 is String && konstue_2 is Number)) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (!funWithReturnsFalse(konstue_1 is String && konstue_2 is Number)) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (funWithReturnsNotNull(konstue_1 is String && konstue_2 is Number) != null) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (funWithReturnsNull(konstue_1 is String && konstue_2 is Number) == null) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Any?, konstue_2: Any?) {
    if (funWithReturnsTrueAndInvertCondition(konstue_1 !is String || konstue_2 !is Number)) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (!funWithReturnsFalseAndInvertCondition(konstue_1 !is String || konstue_2 !is Number)) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (funWithReturnsNotNullAndInvertCondition(konstue_1 !is String || konstue_2 !is Number) != null) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
    if (funWithReturnsNullAndInvertCondition(konstue_1 !is String || konstue_2 !is Number) == null) {
        println(konstue_1.length)
        println(konstue_2.toByte())
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?, konstue_2: Any?) {
    if (funWithReturnsTrueAndInvertCondition(konstue_1 !is String || konstue_2 != null)) {
        println(konstue_1.length)
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
    }
    if (!funWithReturnsFalseAndInvertCondition(konstue_1 !is String || konstue_2 != null)) {
        println(konstue_1.length)
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
    }
    if (funWithReturnsNotNullAndInvertCondition(konstue_1 !is String || konstue_2 != null) != null) {
        println(konstue_1.length)
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
    }
    if (funWithReturnsNullAndInvertCondition(konstue_1 !is String || konstue_2 != null) == null) {
        println(konstue_1.length)
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
    }
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Any?, konstue_2: Number?) {
    if (funWithReturnsTrue(konstue_1 is Float? && konstue_1 != null && konstue_2 != null)) {
        println(konstue_1.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
    }
    if (!funWithReturnsFalse(konstue_1 is Float? && konstue_1 != null && konstue_2 != null)) {
        println(konstue_1.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
    }
    if (funWithReturnsNotNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null) != null) {
        println(konstue_1.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
    }
    if (funWithReturnsNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null) == null) {
        println(konstue_1.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
    }
}

// TESTCASE NUMBER: 10
class case_10_class {
    konst prop_1: Int? = 10

    fun case_10(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_10_class()
        if (funWithReturnsTrue(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null)) {
            println(konstue_1.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(o.prop_1.plus(3))
        }
        if (!funWithReturnsFalse(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null)) {
            println(konstue_1.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(o.prop_1.plus(3))
        }
        if (funWithReturnsNotNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null) != null) {
            println(konstue_1.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(o.prop_1.plus(3))
        }
        if (funWithReturnsNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null) == null) {
            println(konstue_1.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(o.prop_1.plus(3))
        }
    }
}

/*
 * TESTCASE NUMBER: 11
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26747
 */
fun case_11(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?) {
    funWithReturnsAndInvertCondition(konstue_1 !is String || konstue_2 !is Number || <!USELESS_IS_CHECK!>konstue_3 !is Any?<!>)
    println(konstue_1<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.length)
    println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
}
