// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, common
 * NUMBER: 1
 * DESCRIPTION: Analysis by contracts with mixed CallsInPlace and Returns effects.
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
inline fun case_1(konstue_1: Int?, block: () -> Unit): Boolean {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        returns(true) implies (konstue_1 != null)
    }

    block()

    return konstue_1 != null
}

// TESTCASE NUMBER: 2
inline fun <T> T?.case_2(konstue_1: Int?, konstue_2: Any?, block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        returns(true) implies (konstue_1 == null && this@case_2 == null && konstue_2 !is Boolean?)
        returns(false) implies (konstue_2 is Boolean?)
        returns(null) implies ((konstue_1 != null || this@case_2 != null) && konstue_2 !is Boolean?)
    }

    block()

    if (konstue_1 != null && this != null && konstue_2 is Boolean?) return true
    if (konstue_2 !is Boolean?) return false

    return null
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    konst konstue_2: Int
    if (contracts.case_1(konstue_1) { konstue_2 = 10 }) {
        println(<!UNINITIALIZED_VARIABLE!>konstue_2<!>)
    } else {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
        println(konstue_2)
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?, konstue_2: Int?, konstue_3: Any?) {
    konst konstue_4: Int
    when (konstue_1.case_2(konstue_2, konstue_3) { konstue_4 = 10 }) {
        true -> {
            println(konstue_3?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>xor<!>(true))
            println(<!UNINITIALIZED_VARIABLE!>konstue_4<!>)
            println(konstue_1<!UNSAFE_CALL!>.<!>inv())
            println(konstue_2<!UNSAFE_CALL!>.<!>inv())
        }
        false -> {
            println(konstue_4)
            println(konstue_1)
            println(konstue_2)
        }
        null -> {
            println(konstue_3?.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>xor<!>(true))
            println(konstue_4)
            println(konstue_1)
            println(konstue_2)
        }
    }
}
