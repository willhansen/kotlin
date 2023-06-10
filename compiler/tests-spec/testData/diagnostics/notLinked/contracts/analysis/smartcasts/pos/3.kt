// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 3
 * DESCRIPTION: Smartcasts using Returns effects with complex (conjunction/disjunction) type checking and not-null conditions inside contract.
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?, konstue_2: Any?) {
    contract { returns() implies (konstue_1 is String && konstue_2 is Number) }
    if (!(konstue_1 is String && konstue_2 is Number)) throw Exception()
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?, konstue_2: Any?) {
    contract { returns() implies (konstue_1 is String && konstue_2 == null) }
    if (!(konstue_1 is String && konstue_2 == null)) throw Exception()
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?) {
    contract { returns() implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    if (!(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null)) throw Exception()
}

// TESTCASE NUMBER: 4
fun case_4_1(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is String && konstue_2 is Number) }
    return konstue_1 is String && konstue_2 is Number
}
fun case_4_2(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(false) implies (konstue_1 is String && konstue_2 is Number) }
    return !(konstue_1 is String && konstue_2 is Number)
}
fun case_4_3(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is String && konstue_2 is Number) }
    return if (konstue_1 is String && konstue_2 is Number) true else null
}
fun case_4_4(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is String && konstue_2 is Number) }
    return if (konstue_1 is String && konstue_2 is Number) null else true
}

// TESTCASE NUMBER: 5
fun case_5_1(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is String && konstue_2 == null) }
    return konstue_1 is String && konstue_2 == null
}
fun case_5_2(konstue_1: Any?, konstue_2: Any?): Boolean {
    contract { returns(false) implies (konstue_1 is String && konstue_2 == null) }
    return !(konstue_1 is String && konstue_2 == null)
}
fun case_5_3(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is String && konstue_2 == null) }
    return if (konstue_1 is String && konstue_2 == null) true else null
}
fun case_5_4(konstue_1: Any?, konstue_2: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is String && konstue_2 == null) }
    return if (konstue_1 is String && konstue_2 == null) null else true
}

// TESTCASE NUMBER: 6
fun case_6_1(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    return konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null
}
fun case_6_2(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean {
    contract { returns(false) implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    return !(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null)
}
fun case_6_3(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean? {
    contract { returnsNotNull() implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    return if (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) true else null
}
fun case_6_4(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean? {
    contract { returns(null) implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    return if (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) null else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?, konstue_2: Any?) {
    contracts.case_1(konstue_1, konstue_2)
    println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
    println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>.toByte())
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?, konstue_2: Any?) {
    contracts.case_2(konstue_1, konstue_2)
    println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
    println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>?.toByte())
}

// TESTCASE NUMBER: 3
class case_3_class {
    konst prop_1: Int? = 10
    fun case_3(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_3_class()
        contracts.case_3(konstue_1, konstue_2, o.prop_1, this.prop_1)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.dec())
        println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
        println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?, konstue_2: Any?) {
    if (contracts.case_4_1(konstue_1, konstue_2)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>.toByte())
    }
    if (!contracts.case_4_2(konstue_1, konstue_2)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>.toByte())
    }
    if (contracts.case_4_3(konstue_1, konstue_2) != null) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>.toByte())
    }
    if (contracts.case_4_4(konstue_1, konstue_2) == null) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>.toByte())
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?, konstue_2: Any?) {
    if (contracts.case_5_1(konstue_1, konstue_2)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>?.toByte())
    }
    if (!contracts.case_5_2(konstue_1, konstue_2)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>?.toByte())
    }
    if (contracts.case_5_3(konstue_1, konstue_2) != null) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>?.toByte())
    }
    if (contracts.case_5_4(konstue_1, konstue_2) == null) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.length)
        println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>?.toByte())
    }
}

// TESTCASE NUMBER: 6
class case_6_class {
    konst prop_1: Int? = 10
    fun case_6(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_6_class()
        if (contracts.case_6_1(konstue_1, konstue_2, o.prop_1, this.prop_1)) {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        }
        if (!contracts.case_6_2(konstue_1, konstue_2, o.prop_1, this.prop_1)) {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        }
        if (contracts.case_6_3(konstue_1, konstue_2, o.prop_1, this.prop_1) != null) {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        }
        if (contracts.case_6_4(konstue_1, konstue_2, o.prop_1, this.prop_1) == null) {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.dec())
            println(konstue_2<!UNNECESSARY_SAFE_CALL!>?.<!>toByte())
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        }
    }
}
