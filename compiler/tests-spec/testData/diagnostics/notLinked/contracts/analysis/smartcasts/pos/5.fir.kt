// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun <T> T?.case_1() {
    contract { returns() implies (this@case_1 != null && this@case_1 is String) }
    if (!(this@case_1 != null && this@case_1 is String)) throw Exception()
}

// TESTCASE NUMBER: 2
fun <T : Number?> T.case_2() {
    contract { returns() implies (this@case_2 is Int && <!SENSELESS_COMPARISON!>this@case_2 != null<!>) }
    if (!(this@case_2 is Int && <!SENSELESS_COMPARISON!>this@case_2 != null<!>)) throw Exception()
}

// TESTCASE NUMBER: 3
inline fun <reified T : Any?> T?.case_3() {
    contract { returns() implies (this@case_3 is Number && this@case_3 is Int && <!SENSELESS_COMPARISON!>this@case_3 != null<!>) }
    if (!(this@case_3 is Number && this@case_3 is Int && <!SENSELESS_COMPARISON!>this@case_3 != null<!>)) throw Exception()
}

// TESTCASE NUMBER: 4
fun <T> T?.case_4_1(): Boolean {
    contract { returns(true) implies (this@case_4_1 != null && this@case_4_1 is String) }
    return this@case_4_1 != null && this@case_4_1 is String
}
fun <T> T?.case_4_2(): Boolean {
    contract { returns(false) implies (this@case_4_2 != null && this@case_4_2 is String) }
    return !(this@case_4_2 != null && this@case_4_2 is String)
}
fun <T> T?.case_4_3(): Boolean? {
    contract { returnsNotNull() implies (this@case_4_3 != null && this@case_4_3 is String) }
    return if (this@case_4_3 != null && this@case_4_3 is String) true else null
}
fun <T> T?.case_4_4(): Boolean? {
    contract { returns(null) implies (this@case_4_4 != null && this@case_4_4 is String) }
    return if (this@case_4_4 != null && this@case_4_4 is String) null else true
}

// TESTCASE NUMBER: 5
fun <T : Number?> T.case_5_1(): Boolean {
    contract { returns(true) implies (this@case_5_1 is Int && <!SENSELESS_COMPARISON!>this@case_5_1 != null<!>) }
    return this@case_5_1 is Int && <!SENSELESS_COMPARISON!>this@case_5_1 != null<!>
}
fun <T : Number?> T.case_5_2(): Boolean {
    contract { returns(false) implies (this@case_5_2 is Int && <!SENSELESS_COMPARISON!>this@case_5_2 != null<!>) }
    return !(this@case_5_2 is Int && <!SENSELESS_COMPARISON!>this@case_5_2 != null<!>)
}
fun <T : Number?> T.case_5_3(): Boolean? {
    contract { returnsNotNull() implies (this@case_5_3 is Int && <!SENSELESS_COMPARISON!>this@case_5_3 != null<!>) }
    return if (this@case_5_3 is Int && <!SENSELESS_COMPARISON!>this@case_5_3 != null<!>) true else null
}
fun <T : Number?> T.case_5_4(): Boolean? {
    contract { returns(null) implies (this@case_5_4 is Int && <!SENSELESS_COMPARISON!>this@case_5_4 != null<!>) }
    return if (this@case_5_4 is Int && <!SENSELESS_COMPARISON!>this@case_5_4 != null<!>) null else true
}

// TESTCASE NUMBER: 6
inline fun <reified T : Any?> T?.case_6_1(): Boolean {
    contract { returns(true) implies (this@case_6_1 is Number && this@case_6_1 is Int && <!SENSELESS_COMPARISON!>this@case_6_1 != null<!>) }
    return this@case_6_1 is Number && this@case_6_1 is Int && <!SENSELESS_COMPARISON!>this@case_6_1 != null<!>
}
inline fun <reified T : Any?> T?.case_6_2(): Boolean {
    contract { returns(false) implies (this@case_6_2 is Number && this@case_6_2 is Int && <!SENSELESS_COMPARISON!>this@case_6_2 != null<!>) }
    return !(this@case_6_2 is Number && this@case_6_2 is Int && <!SENSELESS_COMPARISON!>this@case_6_2 != null<!>)
}
inline fun <reified T : Any?> T?.case_6_3(): Boolean? {
    contract { returnsNotNull() implies (this@case_6_3 is Number && this@case_6_3 is Int && <!SENSELESS_COMPARISON!>this@case_6_3 != null<!>) }
    return if (this@case_6_3 is Number && this@case_6_3 is Int && <!SENSELESS_COMPARISON!>this@case_6_3 != null<!>) true else null
}
inline fun <reified T : Any?> T?.case_6_4(): Boolean? {
    contract { returns(null) implies (this@case_6_4 is Number && this@case_6_4 is Int && <!SENSELESS_COMPARISON!>this@case_6_4 != null<!>) }
    return if (this@case_6_4 is Number && this@case_6_4 is Int && <!SENSELESS_COMPARISON!>this@case_6_4 != null<!>) null else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    konstue_1.case_1()
    println(konstue_1.length)
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Number?) {
    konstue_1.case_2()
    println(konstue_1.inv())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?) {
    konstue_1.case_3()
    println(konstue_1.inv())
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?) {
    when { konstue_1.case_4_1() -> println(konstue_1.length) }
    when { !konstue_2.case_4_2() -> println(konstue_2.length) }
    when { konstue_3.case_4_3() != null -> println(konstue_3.length) }
    when { konstue_3.case_4_4() == null -> println(konstue_3.length) }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Number?, konstue_2: Number?, konstue_3: Number?) {
    if (konstue_1.case_5_1()) println(konstue_1.inv())
    if (!konstue_2.case_5_2()) println(konstue_2.inv())
    if (konstue_3.case_5_3() != null) println(konstue_3.inv())
    if (konstue_3.case_5_4() == null) println(konstue_3.inv())
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Any?, konstue_2: Any?, konstue_3: Any?) {
    if (konstue_1.case_6_1()) println(konstue_1.inv())
    if (!konstue_2.case_6_2()) println(konstue_2.inv())
    if (konstue_3.case_6_3() != null) println(konstue_3.inv())
    if (konstue_3.case_6_4() == null) println(konstue_3.inv())
}
