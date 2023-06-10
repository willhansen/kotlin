// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
inline fun case_1(konstue_1: Int?, block: () -> Unit): Boolean {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        returns(true) implies (konstue_1 != null)
    }
    block()
    return konstue_1 != null
}

// TESTCASE NUMBER: 2
inline fun <T> T?.case_2(konstue_1: Int?, konstue_2: Any?, block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        returns(true) implies (konstue_1 != null && this@case_2 != null && konstue_2 is Boolean?)
        returns(false) implies (konstue_2 !is Boolean?)
        returns(null) implies ((konstue_1 == null || this@case_2 == null) && konstue_2 is Boolean?)
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
    konst konstue_3: Int
    if (contracts.case_1(konstue_1) { konstue_3 = 10 }) {
        konstue_1.inv()
        println(konstue_3)
    } else {
        println(konstue_3)
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?, konstue_2: Int?, konstue_3: Any?) {
    konst konstue_4: Int
    when (konstue_1.case_2(konstue_2, konstue_3) { konstue_4 = 10 }) {
        true -> {
            println(konstue_3?.xor(true))
            println(konstue_4)
            println(konstue_1.inv())
            println(konstue_2.inv())
        }
        false -> {
            println(konstue_4)
            println(konstue_1)
            println(konstue_2)
        }
        null -> {
            println(konstue_3?.xor(true))
            println(konstue_4)
            println(konstue_1)
            println(konstue_2)
        }
    }
    println(konstue_4)
}
