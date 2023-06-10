// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun <T> T?.case_1(konstue_1: Int?) {
    contract { returns() implies (this@case_1 != null && this@case_1 is String && konstue_1 != null) }
    if (!(this@case_1 != null && this@case_1 is String && konstue_1 != null)) throw Exception()
}

// TESTCASE NUMBER: 2
fun <T : Number?> T.case_2(konstue_2: Any?) {
    contract { returns() implies (this@case_2 is Int && <!SENSELESS_COMPARISON!>this@case_2 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) }
    if (!(this@case_2 is Int && <!SENSELESS_COMPARISON!>this@case_2 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>)) throw Exception()
}

// TESTCASE NUMBER: 3
fun <T : Any?> T?.case_3(konstue_2: Any?) {
    contract { returns() implies (this@case_3 is Number && this@case_3 is Int && <!SENSELESS_COMPARISON!>this@case_3 != null<!> && konstue_2 != null) }
    if (!(this@case_3 is Number && this@case_3 is Int && <!SENSELESS_COMPARISON!>this@case_3 != null<!> && konstue_2 != null)) throw Exception()
}

// TESTCASE NUMBER: 4
inline fun <reified T : Any?> T?.case_4(konstue_2: Number, konstue_3: Any?, konstue_4: String?) {
    contract { returns() implies ((this@case_4 is Number || this@case_4 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    if (!((this@case_4 is Number || this@case_4 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null)) throw Exception()
}

// TESTCASE NUMBER: 5
fun <T> T?.case_5_1(konstue_1: Int?): Boolean {
    contract { returns(true) implies (this@case_5_1 != null && this@case_5_1 is String && konstue_1 != null) }
    return this@case_5_1 != null && this@case_5_1 is String && konstue_1 != null
}
fun <T> T?.case_5_2(konstue_1: Int?): Boolean {
    contract { returns(false) implies (this@case_5_2 != null && this@case_5_2 is String && konstue_1 != null) }
    return !(this@case_5_2 != null && this@case_5_2 is String && konstue_1 != null)
}
fun <T> T?.case_5_3(konstue_1: Int?): Boolean? {
    contract { returnsNotNull() implies (this@case_5_3 != null && this@case_5_3 is String && konstue_1 != null) }
    return if (this@case_5_3 != null && this@case_5_3 is String && konstue_1 != null) true else null
}
fun <T> T?.case_5_4(konstue_1: Int?): Boolean? {
    contract { returns(null) implies (this@case_5_4 != null && this@case_5_4 is String && konstue_1 != null) }
    return if (this@case_5_4 != null && this@case_5_4 is String && konstue_1 != null) null else true
}

// TESTCASE NUMBER: 6
fun <T : Number?> T.case_6_1(konstue_2: Any?): Boolean {
    contract { returns(true) implies (this@case_6_1 is Int && <!SENSELESS_COMPARISON!>this@case_6_1 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) }
    return this@case_6_1 is Int && <!SENSELESS_COMPARISON!>this@case_6_1 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>
}
fun <T : Number?> T.case_6_2(konstue_2: Any?): Boolean {
    contract { returns(false) implies (this@case_6_2 is Int && <!SENSELESS_COMPARISON!>this@case_6_2 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) }
    return !(this@case_6_2 is Int && <!SENSELESS_COMPARISON!>this@case_6_2 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>)
}
fun <T : Number?> T.case_6_3(konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (this@case_6_3 is Int && <!SENSELESS_COMPARISON!>this@case_6_3 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) }
    return if (this@case_6_3 is Int && <!SENSELESS_COMPARISON!>this@case_6_3 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) true else null
}
fun <T : Number?> T.case_6_4(konstue_2: Any?): Boolean? {
    contract { returns(null) implies (this@case_6_4 is Int && <!SENSELESS_COMPARISON!>this@case_6_4 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) }
    return if (this@case_6_4 is Int && <!SENSELESS_COMPARISON!>this@case_6_4 != null<!> && konstue_2 is Number && <!SENSELESS_COMPARISON!>konstue_2 != null<!>) null else true
}

// TESTCASE NUMBER: 7
fun <T : Any?> T?.case_7_1(konstue_2: Any?): Boolean {
    contract { returns(true) implies (this@case_7_1 is Number && this@case_7_1 is Int && <!SENSELESS_COMPARISON!>this@case_7_1 != null<!> && konstue_2 != null) }
    return this@case_7_1 is Number && this@case_7_1 is Int && <!SENSELESS_COMPARISON!>this@case_7_1 != null<!> && konstue_2 != null
}
fun <T : Any?> T?.case_7_2(konstue_2: Any?): Boolean {
    contract { returns(true) implies (this@case_7_2 is Number && this@case_7_2 is Int && <!SENSELESS_COMPARISON!>this@case_7_2 != null<!> && konstue_2 != null) }
    return this@case_7_2 is Number && this@case_7_2 is Int && <!SENSELESS_COMPARISON!>this@case_7_2 != null<!> && konstue_2 != null
}
fun <T : Any?> T?.case_7_3(konstue_2: Any?): Boolean? {
    contract { returnsNotNull() implies (this@case_7_3 is Number && this@case_7_3 is Int && <!SENSELESS_COMPARISON!>this@case_7_3 != null<!> && konstue_2 != null) }
    return if (this@case_7_3 is Number && this@case_7_3 is Int && <!SENSELESS_COMPARISON!>this@case_7_3 != null<!> && konstue_2 != null) true else null
}
fun <T : Any?> T?.case_7_4(konstue_2: Any?): Boolean? {
    contract { returns(null) implies (this@case_7_4 is Number && this@case_7_4 is Int && <!SENSELESS_COMPARISON!>this@case_7_4 != null<!> && konstue_2 != null) }
    return if (this@case_7_4 is Number && this@case_7_4 is Int && <!SENSELESS_COMPARISON!>this@case_7_4 != null<!> && konstue_2 != null) null else true
}

// TESTCASE NUMBER: 8
inline fun <reified T : Any?> T?.case_8_1(konstue_2: Number, konstue_3: Any?, konstue_4: String?): Boolean {
    contract { returns(true) implies ((this@case_8_1 is Number || this@case_8_1 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    return (this@case_8_1 is Number || this@case_8_1 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null
}
inline fun <reified T : Any?> T?.case_8_2(konstue_2: Number, konstue_3: Any?, konstue_4: String?): Boolean {
    contract { returns(false) implies ((this@case_8_2 is Number || this@case_8_2 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    return !((this@case_8_2 is Number || this@case_8_2 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null)
}
inline fun <reified T : Any?> T?.case_8_3(konstue_2: Number, konstue_3: Any?, konstue_4: String?): Boolean? {
    contract { returnsNotNull() implies ((this@case_8_3 is Number || this@case_8_3 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    return if ((this@case_8_3 is Number || this@case_8_3 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) true else null
}
inline fun <reified T : Any?> T?.case_8_4(konstue_2: Number, konstue_3: Any?, konstue_4: String?): Boolean? {
    contract { returns(null) implies ((this@case_8_4 is Number || this@case_8_4 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    return if ((this@case_8_4 is Number || this@case_8_4 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) null else true
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?, konstue_2: Int?) {
    konstue_1.case_1(konstue_2)
    println(konstue_1.length)
    println(konstue_2.inv())
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Number?, konstue_2: Any?) {
    konstue_1.case_2(konstue_2)
    println(konstue_1.inv())
    println(konstue_2.toByte())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Any?, konstue_2: String?) {
    konstue_1.case_3(konstue_2)
    println(konstue_1.inv())
    println(konstue_2.length)
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?, konstue_2: Number, konstue_3: Any?, konstue_4: String?) {
    konstue_1.case_4(konstue_2, konstue_3, konstue_4)
    println(konstue_2.inv())
    println(konstue_3.toByte())
    println(konstue_4.length)
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?, konstue_2: Int?, konstue_3: Any?, konstue_4: Int?, konstue_5: Any?, konstue_6: Int?) {
    when {
        konstue_1.case_5_1(konstue_2) -> {
            println(konstue_1.length)
            println(konstue_2.inv())
        }
    }
    when {
        !konstue_3.case_5_2(konstue_4) -> {
            println(konstue_3.length)
            println(konstue_4.inv())
        }
    }
    when {
        konstue_5.case_5_3(konstue_6) != null -> {
            println(konstue_5.length)
            println(konstue_6.inv())
        }
    }
    when {
        konstue_5.case_5_4(konstue_6) == null -> {
            println(konstue_5.length)
            println(konstue_6.inv())
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Number?, konstue_2: Any?, konstue_3: Number?, konstue_4: Any?, konstue_5: Number?, konstue_6: Any?) {
    if (konstue_1.case_6_1(konstue_2)) {
        println(konstue_1.inv())
        println(konstue_2.toByte())
    }
    if (!konstue_3.case_6_2(konstue_4)) {
        println(konstue_3.inv())
        println(konstue_4.toByte())
    }
    if (konstue_5.case_6_3(konstue_6) != null) {
        println(konstue_5.inv())
        println(konstue_6.toByte())
    }
    if (konstue_5.case_6_4(konstue_6) == null) {
        println(konstue_5.inv())
        println(konstue_6.toByte())
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Any?, konstue_2: String?, konstue_3: Any?, konstue_4: String?, konstue_5: Any?, konstue_6: String?) {
    if (konstue_1.case_7_1(konstue_2)) {
        println(konstue_1.inv())
        println(konstue_2.length)
    }
    if (konstue_3.case_7_2(konstue_4)) {
        println(konstue_3.inv())
        println(konstue_4.length)
    }
    if (konstue_5.case_7_3(konstue_6) != null) {
        println(konstue_5.inv())
        println(konstue_6.length)
    }
    if (konstue_5.case_7_4(konstue_6) == null) {
        println(konstue_5.inv())
        println(konstue_6.length)
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Any?, konstue_2: Number, konstue_3: Any?, konstue_4: String?, konstue_5: Any?, konstue_6: Number, konstue_7: Any?, konstue_8: String?) {
    when { konstue_1.case_8_1(konstue_2, konstue_3, konstue_4) -> println(konstue_2.inv()) }
    when { konstue_1.case_8_1(konstue_2, konstue_3, konstue_4) -> println(konstue_3.toByte()) }
    when { konstue_1.case_8_1(konstue_2, konstue_3, konstue_4) -> println(konstue_4.length) }
    when { !konstue_5.case_8_2(konstue_6, konstue_7, konstue_8) -> println(konstue_6.inv()) }
    when { !konstue_5.case_8_2(konstue_6, konstue_7, konstue_8) -> println(konstue_7.toByte()) }
    when { !konstue_5.case_8_2(konstue_6, konstue_7, konstue_8) -> println(konstue_8.length) }
    when { konstue_5.case_8_3(konstue_6, konstue_7, konstue_8) != null -> println(konstue_6.inv()) }
    when { konstue_5.case_8_3(konstue_6, konstue_7, konstue_8) != null -> println(konstue_7.toByte()) }
    when { konstue_5.case_8_3(konstue_6, konstue_7, konstue_8) != null -> println(konstue_8.length) }
    when { konstue_5.case_8_4(konstue_6, konstue_7, konstue_8) == null -> println(konstue_6.inv()) }
    when { konstue_5.case_8_4(konstue_6, konstue_7, konstue_8) == null -> println(konstue_7.toByte()) }
    when { konstue_5.case_8_4(konstue_6, konstue_7, konstue_8) == null -> println(konstue_8.length) }
}
