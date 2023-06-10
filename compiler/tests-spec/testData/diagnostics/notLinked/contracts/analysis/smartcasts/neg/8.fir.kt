// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 3
fun <T> T?.case_3(konstue_1: Int?, konstue_2: Boolean): Boolean {
    <!WRONG_IMPLIES_CONDITION, WRONG_IMPLIES_CONDITION!>contract {
        returns(true) implies (konstue_1 != null)
        returns(false) implies (konstue_1 == null && !konstue_2)
        returns(null) implies (konstue_1 == null && konstue_2)
    }<!>

    return konstue_1 == null
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Number, block: (() -> Unit)?): Boolean? {
    <!WRONG_IMPLIES_CONDITION, WRONG_IMPLIES_CONDITION!>contract {
        returns(true) implies (block != null)
        returns(false) implies (konstue_1 is Int)
        returns(null) implies (block == null)
    }<!>

    return <!SENSELESS_COMPARISON!>konstue_1 == null<!>
}

// TESTCASE NUMBER: 5
fun String?.case_5(konstue_1: Number?): Boolean? {
    <!WRONG_IMPLIES_CONDITION, WRONG_IMPLIES_CONDITION!>contract {
        returns(true) implies (konstue_1 == null)
        returns(false) implies (this@case_5 == null)
        returnsNotNull() implies (konstue_1 is Int)
    }<!>

    return konstue_1 == null
}

// TESTCASE NUMBER: 6
fun <T> T?.case_6(konstue_1: Number, konstue_2: String?): Boolean? {
    <!WRONG_IMPLIES_CONDITION, WRONG_IMPLIES_CONDITION, WRONG_IMPLIES_CONDITION!>contract {
        returns(true) implies (this@case_6 == null)
        returns(false) implies (konstue_1 is Int)
        returns(null) implies (this@case_6 is String)
        returnsNotNull() implies (konstue_2 == null)
    }<!>

    return <!SENSELESS_COMPARISON!>konstue_1 == null<!>
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    funWithReturns(konstue_1 !is Number?)
    <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>())
    if (funWithReturnsTrue(konstue_1 !is Number)) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>toByte<!>())
        if (funWithReturnsNotNull(konstue_1 is Int) == null) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>())
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?) {
    if (!funWithReturnsFalse(konstue_1 !is Number?)) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>())
        funWithReturns(konstue_1 !is Number)
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>toByte<!>())
        if (funWithReturnsNull(konstue_1 !is Int) == null) <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>())
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?, konstue_2: Any?) {
    if (!konstue_1.case_3(konstue_1, konstue_2 is Number?)) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>())
        println(konstue_1)
    } else if (konstue_1.case_3(konstue_1, konstue_2 is Number?)) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2?.<!UNRESOLVED_REFERENCE!>toByte<!>())
    } else {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>())
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Number, konstue_2: (() -> Unit)?) {
    if (contracts.case_4(konstue_1, konstue_2) == true) {
        konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>()
    } else if (contracts.case_4(konstue_1, konstue_2) == false) {
        println(konstue_2)
    } else if (contracts.case_4(konstue_1, konstue_2) == null) {
        <!UNSAFE_IMPLICIT_INVOKE_CALL!>konstue_2<!>()
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Number?, konstue_2: String?) {
    <!NO_ELSE_IN_WHEN!>when<!> (konstue_2.case_5(konstue_1)) {
        true -> {
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
            println(konstue_1.toByte())
        }
        false -> {
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
            println(konstue_1.inv())
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Number, konstue_2: String?, konstue_3: Any?) {
    when (konstue_3.case_6(konstue_1, konstue_2)) {
        true -> {
            println(konstue_3.equals("")) // OK because `konstue_3` is `Nothing?` and `equals` resolves to `kotlin/text/equals`, which has `String?` as receiver type.
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
        }
        false -> {
            <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_3.<!UNRESOLVED_REFERENCE!>length<!>)
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
        }
        null -> {
            <!OVERLOAD_RESOLUTION_AMBIGUITY!>println<!>(konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>())
        }
    }
}
