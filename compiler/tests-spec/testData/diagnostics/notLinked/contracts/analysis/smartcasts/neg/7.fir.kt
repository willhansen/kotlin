// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1_1(konstue_1: Int?) {
    contract { returns() implies (konstue_1 == null) }
    if (!(konstue_1 == null)) throw Exception()
}
fun case_1_2(konstue_1: Int?) {
    contract { returns() implies (konstue_1 != null) }
    if (!(konstue_1 != null)) throw Exception()
}

// TESTCASE NUMBER: 2
fun case_2_1(konstue_1: Number?) {
    contract { returns() implies (konstue_1 !is Float) }
    if (!(konstue_1 !is Float)) throw Exception()
}
fun case_2_2(konstue_1: Number?) {
    contract { returns() implies (konstue_1 !is Int) }
    if (!(konstue_1 !is Int)) throw Exception()
}

// TESTCASE NUMBER: 3
fun case_3_1(konstue_1: Any?) {
    contract { returns() implies (konstue_1 !is String) }
    if (!(konstue_1 !is String)) throw Exception()
}
fun case_3_2(konstue_1: Any?) {
    contract { returns() implies (konstue_1 is String) }
    if (!(konstue_1 is String)) throw Exception()
}

// TESTCASE NUMBER: 4
fun case_4_1(konstue_1: Any?) {
    contract { returns() implies (konstue_1 !is Number?) }
    if (!(konstue_1 !is Number?)) throw Exception()
}
fun case_4_2(konstue_1: Number?) {
    contract { returns() implies (konstue_1 == null) }
    if (!(konstue_1 == null)) throw Exception()
}
fun case_4_3(konstue_1: Number) {
    contract { returns() implies (konstue_1 !is Int) }
    if (!(konstue_1 !is Int)) throw Exception()
}

// TESTCASE NUMBER: 5
fun case_5_1(konstue_1: Int?): Boolean {
    contract { returns(true) implies (konstue_1 == null) }
    return konstue_1 == null
}
fun case_5_2(konstue_1: Int?): Boolean {
    contract { returns(true) implies (konstue_1 != null) }
    return konstue_1 != null
}
fun case_5_3(konstue_1: Int?): Boolean {
    contract { returns(false) implies (konstue_1 == null) }
    return !(konstue_1 == null)
}
fun case_5_4(konstue_1: Int?): Boolean {
    contract { returns(false) implies (konstue_1 != null) }
    return !(konstue_1 != null)
}
fun case_5_5(konstue_1: Int?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 == null) }
    return if (konstue_1 == null) true else null
}
fun case_5_6(konstue_1: Int?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 != null) }
    return if (konstue_1 != null) true else null
}
fun case_5_7(konstue_1: Int?): Boolean? {
    contract { returns(null) implies (konstue_1 == null) }
    return if (konstue_1 == null) null else true
}
fun case_5_8(konstue_1: Int?): Boolean? {
    contract { returns(null) implies (konstue_1 != null) }
    return if (konstue_1 != null) null else true
}

// TESTCASE NUMBER: 6
fun case_6_1(konstue_1: Number?): Boolean {
    contract { returns(true) implies (konstue_1 !is Float) }
    return konstue_1 !is Float
}
fun case_6_2(konstue_1: Number?): Boolean {
    contract { returns(true) implies (konstue_1 !is Int) }
    return konstue_1 !is Int
}
fun case_6_3(konstue_1: Number?): Boolean {
    contract { returns(false) implies (konstue_1 !is Float) }
    return !(konstue_1 !is Float)
}
fun case_6_4(konstue_1: Number?): Boolean {
    contract { returns(false) implies (konstue_1 !is Int) }
    return !(konstue_1 !is Int)
}
fun case_6_5(konstue_1: Number?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is Float) }
    return if (konstue_1 !is Float) true else null
}
fun case_6_6(konstue_1: Number?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is Int) }
    return if (konstue_1 !is Int) true else null
}
fun case_6_7(konstue_1: Number?): Boolean? {
    contract { returns(null) implies (konstue_1 !is Float) }
    return if (konstue_1 !is Float) null else true
}
fun case_6_8(konstue_1: Number?): Boolean? {
    contract { returns(null) implies (konstue_1 !is Int) }
    return if (konstue_1 !is Int) null else true
}

// TESTCASE NUMBER: 7
fun case_7_1(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is String) }
    return konstue_1 !is String
}
fun case_7_2(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is String) }
    return konstue_1 is String
}
fun case_7_3(konstue_1: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is String) }
    return !(konstue_1 !is String)
}
fun case_7_4(konstue_1: Any?): Boolean {
    contract { returns(false) implies (konstue_1 is String) }
    return !(konstue_1 is String)
}
fun case_7_5(konstue_1: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is String) }
    return if (konstue_1 !is String) true else null
}
fun case_7_6(konstue_1: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is String) }
    return if (konstue_1 is String) true else null
}
fun case_7_7(konstue_1: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 !is String) }
    return if (konstue_1 !is String) null else true
}
fun case_7_8(konstue_1: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is String) }
    return if (konstue_1 is String) null else true
}

// TESTCASE NUMBER: 8
fun case_8_1(konstue_1: Any?): Boolean {
    contract { returns(true) implies (konstue_1 !is Number?) }
    return konstue_1 !is Number?
}
fun case_8_2(konstue_1: Number?): Boolean {
    contract { returns(true) implies (konstue_1 == null) }
    return konstue_1 == null
}
fun case_8_3(konstue_1: Number): Boolean {
    contract { returns(true) implies (konstue_1 !is Int) }
    return konstue_1 !is Int
}
fun case_8_4(konstue_1: Any?): Boolean {
    contract { returns(false) implies (konstue_1 !is Number?) }
    return !(konstue_1 !is Number?)
}
fun case_8_5(konstue_1: Number?): Boolean {
    contract { returns(false) implies (konstue_1 == null) }
    return !(konstue_1 == null)
}
fun case_8_6(konstue_1: Number): Boolean {
    contract { returns(false) implies (konstue_1 !is Int) }
    return !(konstue_1 !is Int)
}
fun case_8_7(konstue_1: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is Number?) }
    return if (konstue_1 is Number?) true else null
}
fun case_8_8(konstue_1: Number?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 == null) }
    return if (konstue_1 == null) true else null
}
fun case_8_9(konstue_1: Number): Boolean? {
    contract { returnsNotNull() implies (konstue_1 !is Int) }
    return if (konstue_1 !is Int) true else null
}
fun case_8_10(konstue_1: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is Number?) }
    return if (konstue_1 is Number?) null else true
}
fun case_8_11(konstue_1: Number?): Boolean? {
    contract { returns(null) implies (konstue_1 == null) }
    return if (konstue_1 == null) null else true
}
fun case_8_12(konstue_1: Number): Boolean? {
    contract { returns(null) implies (konstue_1 !is Int) }
    return if (konstue_1 !is Int) null else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    case_1_1(konstue_1)
    konstue_1<!UNSAFE_CALL!>.<!>inv()
    case_1_2(konstue_1)
    konstue_1.inv()
    case_1_1(konstue_1)
    konstue_1.inv()
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Number?) {
    case_2_1(konstue_1)
    konstue_1<!UNSAFE_CALL!>.<!>toByte()
    case_2_2(konstue_1)
    konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>()
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?) {
    case_3_1(konstue_1)
    konstue_1.<!UNRESOLVED_REFERENCE!>length<!>
    case_3_2(konstue_1)
    konstue_1.length
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    case_4_1(konstue_1)
    konstue_1<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>()
    case_4_2(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>)
    konstue_1.toByte()
    case_4_3(konstue_1)
    konstue_1.inv()
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int?) {
    if (case_5_1(konstue_1)) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
        if (case_5_2(konstue_1)) {
            konstue_1.inv()
            case_5_1(konstue_1)
            konstue_1.inv()
        }
    }
    if (!case_5_3(konstue_1)) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
        if (!case_5_4(konstue_1)) {
            konstue_1.inv()
            case_5_1(konstue_1)
            konstue_1.inv()
        }
    }
    if (case_5_5(konstue_1) != null) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
        if (case_5_6(konstue_1) != null) {
            konstue_1.inv()
            case_5_1(konstue_1)
            konstue_1.inv()
        }
    }
    if (case_5_7(konstue_1) == null) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
        if (case_5_8(konstue_1) == null) {
            konstue_1.inv()
            case_5_1(konstue_1)
            konstue_1.inv()
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Number?) {
    when {
        case_6_1(konstue_1) -> {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            when { case_6_2(konstue_1) -> konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>() }
        }
    }
    when {
        !case_6_3(konstue_1) -> {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            when { !case_6_4(konstue_1) -> konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>() }
        }
    }
    when {
        case_6_5(konstue_1) != null -> {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            when { case_6_6(konstue_1) != null -> konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>() }
        }
    }
    when {
        case_6_7(konstue_1) == null -> {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            when { case_6_8(konstue_1) == null -> konstue_1.<!UNRESOLVED_REFERENCE!>inv<!>() }
        }
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Any?) {
    if (case_7_1(konstue_1)) {
        konstue_1.<!UNRESOLVED_REFERENCE!>length<!>
        if (case_7_2(konstue_1)) konstue_1.length
    }
    if (!case_7_3(konstue_1)) {
        konstue_1.<!UNRESOLVED_REFERENCE!>length<!>
        if (!case_7_4(konstue_1)) konstue_1.length
    }
    if (case_7_5(konstue_1) != null) {
        konstue_1.<!UNRESOLVED_REFERENCE!>length<!>
        if (case_7_6(konstue_1) != null) konstue_1.length
    }
    if (case_7_7(konstue_1) == null) {
        konstue_1.<!UNRESOLVED_REFERENCE!>length<!>
        if (case_7_8(konstue_1) == null) konstue_1.length
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?) {
    if (case_8_1(konstue_1)) {
        konstue_1<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>()
        if (case_8_2(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>)) {
            konstue_1.toByte()
            if (case_8_3(konstue_1)) konstue_1.inv()
        }
    }
    if (!case_8_4(konstue_1)) {
        konstue_1<!UNNECESSARY_SAFE_CALL!>?.<!><!UNRESOLVED_REFERENCE!>toByte<!>()
        if (!case_8_5(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>)) {
            konstue_1.toByte()
            if (!case_8_6(konstue_1)) konstue_1.inv()
        }
    }
    if (case_8_7(konstue_1) == null) {
        konstue_1?.<!UNRESOLVED_REFERENCE!>toByte<!>()
        if (case_8_8(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>) != null) {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            if (case_8_9(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>) != null) konstue_1<!UNSAFE_CALL!>.<!>inv()
        }
    }
    if (case_8_10(konstue_1) != null) {
        konstue_1?.<!UNRESOLVED_REFERENCE!>toByte<!>()
        if (case_8_11(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>) == null) {
            konstue_1<!UNSAFE_CALL!>.<!>toByte()
            if (case_8_12(<!ARGUMENT_TYPE_MISMATCH!>konstue_1<!>) == null) konstue_1<!UNSAFE_CALL!>.<!>inv()
        }
    }
}
