// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 8
 * DESCRIPTION: Smartcasts using some Returns effects.
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 3
fun <T> T?.case_3(konstue_1: Int?, konstue_2: Boolean): Boolean {
    contract {
        returns(true) implies (konstue_1 == null)
        returns(false) implies (konstue_1 != null && konstue_2)
        returns(null) implies (konstue_1 != null && !konstue_2)
    }

    return konstue_1 == null
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Number, block: (() -> Unit)?): Boolean? {
    contract {
        returns(true) implies (konstue_1 is Int)
        returns(false) implies (block == null)
        returns(null) implies (block != null)
    }

    return <!SENSELESS_COMPARISON!>konstue_1 == null<!>
}

// TESTCASE NUMBER: 5
fun String?.case_5(konstue_1: Number?): Boolean? {
    contract {
        returns(true) implies (konstue_1 != null)
        returns(false) implies (konstue_1 is Int)
        returnsNotNull() implies (this@case_5 != null)
    }

    return konstue_1 == null
}

// TESTCASE NUMBER: 6
fun <T> T?.case_6(konstue_1: Number, konstue_2: String?): Boolean? {
    contract {
        returns(true) implies (this@case_6 != null)
        returns(false) implies (this@case_6 is String)
        returns(null) implies (konstue_1 is Int)
        returnsNotNull() implies (konstue_2 != null)
    }

    return <!SENSELESS_COMPARISON!>konstue_1 == null<!>
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    funWithReturns(konstue_1 is Number?)
    println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>?.toByte())
    if (funWithReturnsTrue(konstue_1 is Number)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.toByte())
        if (funWithReturnsNotNull(konstue_1 is Int) != null) println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv())
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?) {
    if (!funWithReturnsFalse(konstue_1 is Number?)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>?.toByte())
        funWithReturns(konstue_1 is Number)
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.toByte())
        if (funWithReturnsNull(konstue_1 is Int) == null) println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv())
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?, konstue_2: Any?) {
    if (!konstue_1.case_3(konstue_1, konstue_2 is Number?)) {
        println(<!DEBUG_INFO_SMARTCAST!>konstue_2<!>?.toByte())
        println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv())
    } else if (konstue_1.case_3(<!DEBUG_INFO_CONSTANT!>konstue_1<!>, konstue_2 is Number?)) {
        println(<!DEBUG_INFO_CONSTANT!>konstue_1<!>)
    } else {
        <!UNREACHABLE_CODE!>println(<!><!DEBUG_INFO_SMARTCAST!>konstue_1<!><!UNREACHABLE_CODE!>.inv())<!>
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Number, konstue_2: (() -> Unit)?) {
    if (contracts.case_4(konstue_1, konstue_2) == true) {
        <!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv()
    } else if (contracts.case_4(konstue_1, konstue_2) == false) {
        println(<!DEBUG_INFO_CONSTANT!>konstue_2<!>)
    } else if (contracts.case_4(konstue_1, konstue_2) == null) {
        <!DEBUG_INFO_SMARTCAST!>konstue_2<!>()
    }
}

/*
 * TESTCASE NUMBER: 5
 * UNEXPECTED BEHAVIOUR: unsafe calls
 * ISSUES: KT-26612
 */
fun case_5(konstue_1: Number?, konstue_2: String?) {
    when (konstue_2.case_5(konstue_1)) {
        true -> {
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.toByte())
        }
        false -> {
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv())
        }
        else -> {}
    }
}

/*
 * TESTCASE NUMBER: 6
 * UNEXPECTED BEHAVIOUR: unsafe calls
 * ISSUES: KT-26612
 */
fun case_6(konstue_1: Number, konstue_2: String?, konstue_3: Any?) {
    when (konstue_3.case_6(konstue_1, konstue_2)) {
        true -> {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_3<!>.equals(""))
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
        }
        false -> {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_3<!>.length)
            println(konstue_2<!UNSAFE_CALL!>.<!>length)
        }
        null -> {
            println(<!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inv())
        }
    }
}
