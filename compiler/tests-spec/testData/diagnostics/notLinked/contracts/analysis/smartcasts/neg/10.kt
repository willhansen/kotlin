// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 10
 * DESCRIPTION: Check smartcasts using double negation (returnsFalse/invert type checking/not operator).
 * ISSUES: KT-26176
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(x: Any?): Boolean {
    contract { returns(true) implies (x !is Number) }
    return x !is Number
}

// TESTCASE NUMBER: 2
fun case_2(x: Any?): Boolean {
    contract { returns(true) implies (x !is Number?) }
    return x !is Number?
}

// TESTCASE NUMBER: 15
fun case_15_1(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is String || konstue_2 !is Number) }
    return konstue_1 !is String || konstue_2 !is Number
}
fun case_15_2(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is String || konstue_2 !is Number) }
    return !(konstue_1 !is String || konstue_2 !is Number)
}
fun case_15_3(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is String || konstue_2 !is Number) }
    return if (konstue_1 !is String || konstue_2 !is Number) true else null
}
fun case_15_4(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 !is String || konstue_2 !is Number) }
    return if (konstue_1 !is String || konstue_2 !is Number) null else true
}

// TESTCASE NUMBER: 16
fun case_16_1(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is String || konstue_2 != null) }
    return konstue_1 !is String || konstue_2 != null
}
fun case_16_2(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is String || konstue_2 != null) }
    return !(konstue_1 !is String || konstue_2 != null)
}
fun case_16_3(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is String || konstue_2 != null) }
    return if (konstue_1 !is String || konstue_2 != null) true else null
}
fun case_16_4(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 !is String || konstue_2 != null) }
    return if (konstue_1 !is String || konstue_2 != null) null else true
}

// TESTCASE NUMBER: 17
fun case_17_1(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) }
    return konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null
}
fun case_17_2(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) }
    return !(konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null)
}
fun case_17_3(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) }
    return if (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) true else null
}
fun case_17_4(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) }
    return if (konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || konstue_3 == null || konstue_4 == null) null else true
}

// TESTCASE NUMBER: 18
fun <T> T.case_18_1(): Boolean {
    contract { returns(true) implies (this@case_18_1 !is String) }
    return this@case_18_1 !is String
}
fun <T> T.case_18_2(): Boolean {
    contract { returns(false) implies (this@case_18_2 is String) }
    return !(this@case_18_2 is String)
}
fun <T> T.case_18_3(): Boolean? {
    contract { returnsNotNull() implies (this@case_18_3 is String) }
    return if (this@case_18_3 is String) true else null
}
fun <T> T.case_18_4(): Boolean? {
    contract { returns(null) implies (this@case_18_4 is String) }
    return if (this@case_18_4 is String) null else true
}

// TESTCASE NUMBER: 19
fun <T : Number> T.case_19_1(): Boolean {
    contract { returns(true) implies (this@case_19_1 !is Int) }
    return this@case_19_1 !is Int
}
fun <T : Number> T.case_19_2(): Boolean {
    contract { returns(false) implies (this@case_19_2 is Int) }
    return !(this@case_19_2 is Int)
}
fun <T : Number> T.case_19_3(): Boolean? {
    contract { returnsNotNull() implies (this@case_19_3 is Int) }
    return if (this@case_19_3 is Int) true else null
}
fun <T : Number> T.case_19_4(): Boolean? {
    contract { returns(null) implies (this@case_19_4 is Int) }
    return if (this@case_19_4 is Int) null else true
}

// TESTCASE NUMBER: 20
fun <T : <!FINAL_UPPER_BOUND!>String<!>> T?.case_20_1(): Boolean {
    contract { returns(true) implies (this@case_20_1 != null) }
    return this@case_20_1 != null
}
fun <T : <!FINAL_UPPER_BOUND!>String<!>> T?.case_20_2(): Boolean {
    contract { returns(true) implies (this@case_20_2 == null) }
    return this@case_20_2 == null
}
fun <T : <!FINAL_UPPER_BOUND!>String<!>> T?.case_20_3(): Boolean {
    contract { returns(false) implies (this@case_20_3 != null) }
    return !(this@case_20_3 != null)
}

// TESTCASE NUMBER: 21
fun <T : String?> T.case_21_1(): Boolean {
    contract { returns(true) implies (this@case_21_1 != null) }
    return this@case_21_1 != null
}
fun <T : String?> T.case_21_2(): Boolean {
    contract { returns(true) implies (this@case_21_2 == null) }
    return this@case_21_2 == null
}
fun <T : <!FINAL_UPPER_BOUND!>String<!>> T?.case_21_5(): Boolean? {
    contract { returnsNotNull() implies (this@case_21_5 != null) }
    return if (this@case_21_5 != null) true else null
}
fun <T : <!FINAL_UPPER_BOUND!>String<!>> T?.case_21_7(): Boolean? {
    contract { returns(null) implies (this@case_21_7 != null) }
    return if (this@case_21_7 != null) null else true
}

// TESTCASE NUMBER: 22
fun <T> T?.case_22_1(): Boolean {
    contract { returns(false) implies (this@case_22_1 == null || this@case_22_1 !is String) }
    return !(this@case_22_1 == null || this@case_22_1 !is String)
}
fun <T> T?.case_22_2(): Boolean? {
    contract { returnsNotNull() implies (this@case_22_2 == null || this@case_22_2 !is String) }
    return if (this@case_22_2 == null || this@case_22_2 !is String) true else null
}
fun <T> T?.case_22_3(): Boolean? {
    contract { returns(null) implies (this@case_22_3 == null || this@case_22_3 !is String) }
    return if (this@case_22_3 == null || this@case_22_3 !is String) null else true
}

// TESTCASE NUMBER: 23
fun <T : Number?> T.case_23_1(): Boolean {
    contract { returns(false) implies (this@case_23_1 !is Int || <!SENSELESS_COMPARISON!>this@case_23_1 == null<!>) }
    return !(this@case_23_1 !is Int || <!SENSELESS_COMPARISON!>this@case_23_1 == null<!>)
}
fun <T : Number?> T.case_23_2(): Boolean? {
    contract { returnsNotNull() implies (this@case_23_2 !is Int || <!SENSELESS_COMPARISON!>this@case_23_2 == null<!>) }
    return if (this@case_23_2 !is Int || <!SENSELESS_COMPARISON!>this@case_23_2 == null<!>) true else null
}
fun <T : Number?> T.case_23_3(): Boolean? {
    contract { returns(null) implies (this@case_23_3 !is Int || <!SENSELESS_COMPARISON!>this@case_23_3 == null<!>) }
    return if (this@case_23_3 !is Int || <!SENSELESS_COMPARISON!>this@case_23_3 == null<!>) null else true
}

// TESTCASE NUMBER: 24
inline fun <reified T : Any?> T?.case_24_1(): Boolean {
    contract { returns(false) implies (this@case_24_1 !is Number || this@case_24_1 !is Int || <!SENSELESS_COMPARISON!>this@case_24_1 == null<!>) }
    return !(this@case_24_1 !is Number || this@case_24_1 !is Int || <!SENSELESS_COMPARISON!>this@case_24_1 == null<!>)
}
inline fun <reified T : Any?> T?.case_24_2(): Boolean? {
    contract { returnsNotNull() implies (this@case_24_2 !is Number || this@case_24_2 !is Int || <!SENSELESS_COMPARISON!>this@case_24_2 == null<!>) }
    return if (this@case_24_2 !is Number || this@case_24_2 !is Int || <!SENSELESS_COMPARISON!>this@case_24_2 == null<!>) true else null
}
inline fun <reified T : Any?> T?.case_24_3(): Boolean? {
    contract { returns(null) implies (this@case_24_3 !is Number || this@case_24_3 !is Int || <!SENSELESS_COMPARISON!>this@case_24_3 == null<!>) }
    return if (this@case_24_3 !is Number || this@case_24_3 !is Int || <!SENSELESS_COMPARISON!>this@case_24_3 == null<!>) null else true
}

// TESTCASE NUMBER: 25
fun <T> T?.case_25_1(konstue_1: Int?): Boolean {
    contract { returns(false) implies (this@case_25_1 == null || this@case_25_1 !is String || konstue_1 == null) }
    return !(this@case_25_1 == null || this@case_25_1 !is String || konstue_1 == null)
}
fun <T> T?.case_25_2(konstue_1: Int?): Boolean? {
    contract { returnsNotNull() implies (this@case_25_2 == null || this@case_25_2 !is String || konstue_1 == null) }
    return if (this@case_25_2 == null || this@case_25_2 !is String || konstue_1 == null) true else null
}
fun <T> T?.case_25_3(konstue_1: Int?): Boolean? {
    contract { returns(null) implies (this@case_25_3 == null || this@case_25_3 !is String || konstue_1 == null) }
    return if (this@case_25_3 == null || this@case_25_3 !is String || konstue_1 == null) null else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    if (!contracts.case_1(konstue_1)) println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?) {
    if (!contracts.case_2(konstue_1)) println(konstue_1?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
}

// TESTCASE NUMBER: 3
fun case_3(number: Int?) {
    if (!funWithReturnsTrueAndNullCheck(number)) number<!UNSAFE_CALL!>.<!>inc()
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    if (!funWithReturnsTrue(konstue_1 !is String)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?) {
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 is String)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalse(konstue_1 !is String)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalseAndInvertCondition(konstue_1 is String)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNotNullAndInvertCondition(konstue_1 !is String) != null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (!(funWithReturnsNullAndInvertCondition(konstue_1 !is String) == null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any?) {
    if (!funWithReturnsTrue(konstue_1 == null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Any?) {
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 != null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalse(konstue_1 == null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalseAndInvertCondition(konstue_1 != null)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?) {
    if (!funWithReturnsTrueAndInvertTypeCheck(konstue_1)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (funWithReturnsFalseAndInvertTypeCheck(konstue_1)) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Number?) {
    if (!funWithReturnsTrueAndNullCheck(konstue_1)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (funWithReturnsFalseAndNullCheck(konstue_1)) println(konstue_1<!UNSAFE_CALL!>.<!>toByte())
    if (funWithReturnsFalseAndNotNullCheck(konstue_1)) println(konstue_1)
    if (!(funWithReturnsNotNullAndNullCheck(konstue_1) != null)) println(konstue_1)
    if (!(funWithReturnsNullAndNullCheck(konstue_1) == null)) println(konstue_1)
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Any?, konstue_2: Any?) {
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 is String && konstue_2 is Number)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Any?, konstue_2: Any?) {
    if (!funWithReturnsTrue(konstue_1 !is String || konstue_2 !is Number)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsFalse(konstue_1 !is String || konstue_2 !is Number)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: Any?, konstue_2: Any?) {
    if (!funWithReturnsTrue(konstue_1 !is String || konstue_2 != null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsFalse(konstue_1 !is Float? || konstue_1 == null || konstue_2 == null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNotNull(konstue_1 !is String || konstue_2 !is Number) == null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNull(konstue_1 !is String || konstue_2 !is Number) != null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 13
fun case_13(konstue_1: Any?, konstue_2: Any?) {
    if (!funWithReturnsTrueAndInvertCondition(konstue_1 is Float? && konstue_1 != null && konstue_2 != null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsFalseAndInvertCondition(konstue_1 is String && konstue_2 is Number)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsFalseAndInvertCondition(konstue_1 is String && konstue_2 == null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNotNullAndInvertCondition(konstue_1 is String && konstue_2 is Number) == null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNotNullAndInvertCondition(konstue_1 is String && konstue_2 == null) == null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNotNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null) == null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNullAndInvertCondition(konstue_1 is String && konstue_2 is Number) != null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNullAndInvertCondition(konstue_1 is String && konstue_2 == null) != null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (funWithReturnsNull(konstue_1 is Float? && konstue_1 != null && konstue_2 != null) != null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 14
class case_14_class {
    konst prop_1: Int? = 10

    fun case_14(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_14_class()
        if (!funWithReturnsTrueAndInvertCondition(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null)) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (funWithReturnsFalse(konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || o.prop_1 == null || this.prop_1 == null)) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (funWithReturnsNotNull(konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || o.prop_1 == null || this.prop_1 == null) == null) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (funWithReturnsNull(konstue_1 !is Float? || konstue_1 == null || konstue_2 == null || o.prop_1 == null || this.prop_1 == null) != null) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
    }
}

// TESTCASE NUMBER: 15
fun case_15(konstue_1: Any?, konstue_2: Any?) {
    if (!contracts.case_15_1(konstue_1, konstue_2)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (contracts.case_15_2(konstue_1, konstue_2)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (!(contracts.case_15_3(konstue_1, konstue_2) != null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (!(contracts.case_15_4(konstue_1, konstue_2) == null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 16
fun case_16(konstue_1: Any?, konstue_2: Any?) {
    if (!contracts.case_16_1(konstue_1, konstue_2)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (contracts.case_16_2(konstue_1, konstue_2)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (contracts.case_16_3(konstue_1, konstue_2) == null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
    if (contracts.case_16_4(konstue_1, konstue_2) != null) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
        println(konstue_2?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}

// TESTCASE NUMBER: 17
class case_17_class {
    konst prop_1: Int? = 10

    fun case_17(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_17_class()
        if (contracts.case_17_1(konstue_1, konstue_2, o.prop_1, this.prop_1)) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
            println(this.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (contracts.case_17_2(konstue_1, konstue_2, o.prop_1, this.prop_1)) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
            println(this.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (contracts.case_17_3(konstue_1, konstue_2, o.prop_1, this.prop_1) == null) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
            println(this.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
        if (contracts.case_17_4(konstue_1, konstue_2, o.prop_1, this.prop_1) != null) {
            println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>dec<!>())
            println(konstue_2?.toByte())
            println(o.prop_1<!UNSAFE_CALL!>.<!>plus(3))
            println(this.prop_1<!UNSAFE_CALL!>.<!>plus(3))
        }
    }
}

// TESTCASE NUMBER: 18
fun case_18(konstue_1: Any?) {
    if (!konstue_1.case_18_1()) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (konstue_1.case_18_2()) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (konstue_1.case_18_3() == null) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    if (konstue_1.case_18_4() != null) println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
}

// TESTCASE NUMBER: 19
fun case_19(konstue_1: Number) {
    when { !konstue_1.case_19_1() -> println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>()) }
    when { konstue_1.case_19_2() -> println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>()) }
    when { konstue_1.case_19_3() == null -> println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>()) }
    when { konstue_1.case_19_4() != null -> println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>()) }
}

// TESTCASE NUMBER: 20
fun case_20(konstue_1: String?, konstue_2: String?, konstue_3: String?, konstue_4: String?) {
    if (!konstue_1.case_20_1()) println(konstue_1)
    if (!konstue_2.case_20_2()) println(konstue_2<!UNSAFE_CALL!>.<!>length)
    when (konstue_3.case_20_3()) {
        true -> println(konstue_4<!UNSAFE_CALL!>.<!>length)
        false -> println(konstue_3)
    }
}

// TESTCASE NUMBER: 21
fun case_21(konstue_1: String?) {
    when { !konstue_1.case_21_1() -> println(konstue_1) }
    when { !konstue_1.case_21_2() -> println(konstue_1<!UNSAFE_CALL!>.<!>length) }
    when {
        konstue_1.case_21_5() == null ->  println(konstue_1<!UNSAFE_CALL!>.<!>length)
        konstue_1.case_21_5() != null ->  println(konstue_1)
    }
    when {
        konstue_1.case_21_7() != null ->  println(konstue_1<!UNSAFE_CALL!>.<!>length)
        konstue_1.case_21_7() == null ->  println(konstue_1)
    }
}

// TESTCASE NUMBER: 22
fun case_22(konstue_1: Any?, konstue_2: Any?) {
    when { konstue_1.case_22_1() -> println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>) }
    when { konstue_2.case_22_2() == null -> println(konstue_2.<!UNRESOLVED_REFERENCE!>length<!>) }
    when { konstue_2.case_22_3() != null -> println(konstue_2.<!UNRESOLVED_REFERENCE!>length<!>) }
}

// TESTCASE NUMBER: 23
fun case_23(konstue_1: Number?, konstue_2: Number?) {
    if (konstue_1.case_23_1()) println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
    if (konstue_2.case_23_2() == null) println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
    if (konstue_2.case_23_3() != null) println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
}

// TESTCASE NUMBER: 24
fun case_24(konstue_1: Any?, konstue_2: Any?) {
    if (konstue_1.case_24_1()) println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
    if (konstue_2.case_24_2() != null) println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
    if (konstue_2.case_24_3() == null) println(konstue_2.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inv<!>())
}

// TESTCASE NUMBER: 25
fun case_25(konstue_1: Any?, konstue_2: Int?, konstue_3: Any?, konstue_4: Int?) {
    when {
        konstue_1.case_25_1(konstue_2) -> {
            println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
            println(konstue_2<!UNSAFE_CALL!>.<!>inv())
        }
    }
    when {
        konstue_3.case_25_2(konstue_4) == null -> {
            println(konstue_3.<!UNRESOLVED_REFERENCE!>length<!>)
            println(konstue_4<!UNSAFE_CALL!>.<!>inv())
        }
    }
    when {
        konstue_3.case_25_3(konstue_4) != null -> {
            println(konstue_3.<!UNRESOLVED_REFERENCE!>length<!>)
            println(konstue_4<!UNSAFE_CALL!>.<!>inv())
        }
    }
}
