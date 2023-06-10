// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 4
fun <T> T?.case_4(): Boolean {
    contract { returns(true) implies (this@case_4 != null) }
    return this@case_4 != null
}
fun <T> T?.case_4_1(): Boolean {
    contract { returns(false) implies (this@case_4_1 != null) }
    return !(this@case_4_1 != null)
}
fun <T> T?.case_4_2(): Boolean? {
    contract { returns(null) implies (this@case_4_2 is String) }
    return if (this@case_4_2 is String) null else true
}

// TESTCASE NUMBER: 11
fun <T> T?.case_11_1(): Boolean {
    contract { returns(false) implies (this@case_11_1 != null) }
    return !(this@case_11_1 != null)
}
fun <T> T?.case_11_2(): Boolean? {
    contract { returns(null) implies (this@case_11_2 is String) }
    return if (this@case_11_2 is String) null else true
}

// TESTCASE NUMBER: 12
fun <T> T?.case_12(): Boolean {
    contract { returns(false) implies (this@case_12 is String) }
    return if (this@case_12 is String) false else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    if (funWithReturnsTrue(konstue_1 is String) && funWithReturnsTrueAndNotNullCheck(konstue_1)) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?) {
    if (!funWithReturnsFalse(konstue_1 is String) && !funWithReturnsTrueAndNullCheck(konstue_1)) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?) {
    if (funWithReturnsNull(konstue_1 is String?) == null && funWithReturnsTrue(konstue_1 != null)) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    if (!konstue_1.case_4_1() && konstue_1.case_4_2() == null) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?, konstue_2: Boolean) {
    if (!funWithReturnsFalse(konstue_1 is String) && konstue_2) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any?, konstue_2: Boolean?) {
    if (funWithReturnsNull(konstue_1 is String) == null && konstue_2 != null && konstue_2) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: String?) {
    if (funWithReturnsTrueAndNotNullCheck(konstue_1) && true) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?) {
    if (funWithReturnsTrueAndNullCheck(konstue_1) && false) {
        println(konstue_1)
    }
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Any?) {
    if (funWithReturnsFalse(konstue_1 is String) || funWithReturnsFalse(konstue_1 is Int)) {

    } else {
        println(konstue_1.length)
        println(konstue_1.inv())
    }
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Any?) {
    if (funWithReturnsFalse(konstue_1 is String) || getBoolean()) {

    } else {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Any?) {
    if (!(konstue_1.case_11_1() || konstue_1.case_11_2() != null)) {
        println(konstue_1.length)
    }
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: Any?) {
    if (!konstue_1.case_12() || !konstue_1.case_12()) {
        println(konstue_1.length)
    }
    if (!konstue_1.case_12() && !konstue_1.case_12()) {
        println(konstue_1.length)
    }
}
