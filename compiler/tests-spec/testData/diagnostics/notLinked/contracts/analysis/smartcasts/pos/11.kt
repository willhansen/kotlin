// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 11
 * DESCRIPTION: Check smartcasts with passing same fields of instances of the same class in contract function with conjunction not-null condition.
 * ISSUES: KT-26300
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?) {
    contract { returns() implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    if (!(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null)) throw Exception()
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?, konstue_4: Any?): Boolean {
    contract { returns(true) implies (konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null) }
    return konstue_1 is Float? && konstue_1 != null && konstue_2 != null && konstue_3 != null && konstue_4 != null
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
class case_1 {
    konst prop_1: Int? = 10
    fun case_1(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_1()
        funWithReturns(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null)
        println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        println(<!DEBUG_INFO_SMARTCAST!>this.prop_1<!>.plus(3))
    }
}

// TESTCASE NUMBER: 2
class case_2 {
    konst prop_1: Int? = 10
    fun case_2(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_2()
        if (funWithReturnsTrue(konstue_1 is Float? && konstue_1 != null && konstue_2 != null && o.prop_1 != null && this.prop_1 != null)) {
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
            println(<!DEBUG_INFO_SMARTCAST!>this.prop_1<!>.plus(3))
        }
    }
}

// TESTCASE NUMBER: 3
class case_3 {
    konst prop_1: Int? = 10
    fun case_3(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_3()
        contracts.case_3(konstue_1, konstue_2, o.prop_1, this.prop_1)
        println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
        println(<!DEBUG_INFO_SMARTCAST!>this.prop_1<!>.plus(3))
    }
}

// TESTCASE NUMBER: 4
class case_4 {
    konst prop_1: Int? = 10
    fun case_4(konstue_1: Any?, konstue_2: Number?) {
        konst o = case_4()
        if (contracts.case_4(konstue_1, konstue_2, o.prop_1, this.prop_1)) {
            println(<!DEBUG_INFO_SMARTCAST!>o.prop_1<!>.plus(3))
            println(<!DEBUG_INFO_SMARTCAST!>this.prop_1<!>.plus(3))
        }
    }
}
