// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-435
 * MAIN LINK: expressions, when-expression -> paragraph 6 -> sentence 5
 * NUMBER: 2
 * DESCRIPTION: 'When' with different variants of the arithmetic expressions (additive expression and multiplicative expression) in 'when condition'.
 * HELPERS: typesProvider, classes, functions
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    when (konstue_1) {
        true, 100, -.09f -> {}
        '.', "...", null -> {}
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Number, konstue_2: Int) {
    when (konstue_1) {
        -.09 % 10L, konstue_2 / -5, getByte() - 11 + 90 -> {}
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: String, konstue_2: String, konstue_3: String) {
    when (konstue_1) {
        "..." + konstue_2 + "" + "$konstue_3" + "...", konstue_2 + getString() -> {}
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when (konstue_1) {
        when {
            konstue_2 > 1000 -> 1
            konstue_2 > 100 -> 2
            else -> 3
        }, when (konstue_3) {
            true -> 1
            false -> 2
            null -> 3
        }, when (konstue_3!!) {
            true -> 1
            false -> 2
        } -> {}
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Int, konstue_2: Int) {
    when (konstue_1) {
        if (konstue_2 > 1000) 1 else 2, if (konstue_2 < 100) 1 else if (konstue_2 < 10) 2 else 3 -> {}
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Any, konstue_2: String, konstue_3: String) {
    when (konstue_1) {
        try { 4 } catch (e: Exception) { 5 }, try { throw Exception() } catch (e: Exception) { konstue_2 }, try { throw Exception() } catch (e: Exception) { {konstue_3} } finally { } -> {}
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Int, konstue_2: Int?, konstue_3: Int?) {
    when (konstue_1) {
        konstue_2 ?: 0, konstue_2 ?: konstue_3 ?: 0, konstue_2!! <!USELESS_ELVIS!>?: 0<!> -> {}
    }
}

// TESTCASE NUMBER: 9
fun case_9(konstue_1: Any) {
    when (konstue_1) {
        1..10, -100L..100L, -getInt()..getLong() -> {}
    }
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Collection<Int>, konstue_2: Collection<Int>, konstue_3: Collection<Int>?) {
    when (konstue_1) {
        konstue_2 as List<Int>, konstue_2 <!USELESS_CAST!>as? List<Int><!> -> {}
        konstue_3 <!UNCHECKED_CAST!>as? MutableMap<Int, Int><!>, (konstue_2 <!UNCHECKED_CAST!>as? Map<Int, Int><!>) as MutableMap<Int, Int> -> {}
    }
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Any, konstue_2: Int, konstue_3: Int, konstue_4: Boolean) {
    var mutableValue1 = konstue_2
    var mutableValue2 = konstue_3

    when (konstue_1) {
        ++mutableValue1, --mutableValue2, !konstue_4 -> {}
    }
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: Int, konstue_2: Int, konstue_3: Int, konstue_4: Int?) {
    var mutableValue1 = konstue_2
    var mutableValue2 = konstue_3

    when (konstue_1) {
        <!UNUSED_CHANGED_VALUE!>mutableValue1++<!>, <!UNUSED_CHANGED_VALUE!>mutableValue2--<!>, konstue_4!! -> {}
    }
}

// TESTCASE NUMBER: 13
fun case_13(konstue_1: Int, konstue_2: List<Int>, konstue_3: List<List<List<List<Int>>>>) {
    when (konstue_1) {
        konstue_2[0], konstue_3[0][-4][1][-1] -> {}
    }
}

// TESTCASE NUMBER: 14
fun case_14(konstue_1: Any, konstue_2: Class, konstue_3: Class?, konstue_4: Int) {
    fun __fun_1(): () -> Unit { return fun() { } }

    when (konstue_1) {
        funWithoutArgs(), __fun_1()(), konstue_2.fun_2(konstue_4) -> {}
        konstue_3?.fun_2(konstue_4), konstue_3!!.fun_2(konstue_4) -> {}
    }
}

// TESTCASE NUMBER: 15
fun case_15(konstue_1: Int, konstue_2: Class, konstue_3: Class?) {
    when (konstue_1) {
        konstue_2.prop_1, konstue_3?.prop_2 -> {}
        konstue_2::prop_1.get(), konstue_3!!::prop_3.get() -> {}
    }
}

// TESTCASE NUMBER: 16
fun case_16(konstue_1: () -> Any): Any {
    konst fun_1 = fun() { return }

    return when (konstue_1) {
        fun() {}, fun() { return }, fun(): () -> Unit { return fun() {} }, fun_1 -> {}
        else -> {}
    }
}

// TESTCASE NUMBER: 17
fun case_17(konstue_1: () -> Any) {
    konst lambda_1 = { 0 }

    when (konstue_1) {
        lambda_1, { { {} } }, { -> (Int)
            { arg: Int -> { { println(arg) } } } } -> {}
    }
}

// TESTCASE NUMBER: 18
fun case_18(konstue_1: Any) {
    konst object_1 = object {
        konst prop_1 = 1
    }

    when (konstue_1) {
        object {}, object {
            private fun fun_1() { }
            konst prop_1 = 1
        }, object_1 -> {}
    }
}

// TESTCASE NUMBER: 19
class A {
    konst prop_1 = 1
    konst lambda_1 = { 1 }
    fun fun_1(): Int { return 1 }

    fun case_19(konstue_1: Any) {
        when (konstue_1) {
            this, ((this)), this::prop_1.get() -> {}
            this.prop_1, this.lambda_1() -> {}
            this::lambda_1.get()(), this.fun_1(), this::fun_1.invoke() -> {}
        }
    }
}

// TESTCASE NUMBER: 20
fun case_20(konstue_1: Nothing) {
    when (konstue_1) {
        <!UNREACHABLE_CODE!>throw Exception(), throw throw throw Exception() -> {}<!>
    }
}

// TESTCASE NUMBER: 21
fun case_21(konstue_1: Nothing) {
    fun f1() {
        when (konstue_1) {
            <!UNREACHABLE_CODE!>return, return return return -> 2<!>
        }
    }

    fun f2(): List<Int>? {
        when (konstue_1) {
            <!UNREACHABLE_CODE!>return listOf(0, 1, 2), return null -> 2<!>
        }
    }
}

// TESTCASE NUMBER: 22
fun case_22(konstue_1: Nothing) {
    loop1@ while (true) {
        loop2@ while (true) {
            when (konstue_1) {
                <!UNREACHABLE_CODE!>continue@loop1, continue@loop2 -> 2<!>
            }
        }
    }
}

// TESTCASE NUMBER: 23
fun case_23(konstue_1: Nothing) {
    loop1@ while (true) {
        loop2@ while (true) {
            when (konstue_1) {
                <!UNREACHABLE_CODE!>break@loop1, break@loop2 -> 2<!>
            }
        }
    }
}

// TESTCASE NUMBER: 24
fun case_24(konstue_1: Nothing?) = when (<!DEBUG_INFO_CONSTANT!>konstue_1<!>) {
    throw Exception()<!UNREACHABLE_CODE!><!>, <!UNREACHABLE_CODE!>return ""<!> -> <!UNREACHABLE_CODE!>""<!>
    <!UNREACHABLE_CODE!>null, return return return "", throw throw throw Exception() -> ""<!>
    <!UNREACHABLE_CODE!>else -> ""<!>
}

/*
 * TESTCASE NUMBER: 25
 * DISCUSSION
 * ISSUES: KT-25948
 */
fun case_25(konstue_1: Boolean) = when (konstue_1) {
    true -> {}
    throw Exception()<!UNREACHABLE_CODE!><!>, <!UNREACHABLE_CODE!>return<!> -> <!UNREACHABLE_CODE!>{}<!>
    <!UNREACHABLE_CODE!>false, return return return, throw throw throw Exception() -> {}<!>
}

/*
 * TESTCASE NUMBER: 26
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26045
 */
fun case_26(konstue_1: Int?, konstue_2: Class, konstue_3: Class?) {
    when (konstue_1) {
        konstue_2.prop_1, <!DUPLICATE_LABEL_IN_WHEN!>konstue_3?.prop_1<!> -> {}
        10 -> {}
    }
}
