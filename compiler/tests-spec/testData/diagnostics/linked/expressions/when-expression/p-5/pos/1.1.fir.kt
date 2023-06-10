// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int) {
    when (konstue_1) {
        1 -> true
        2 -> 100
        3 -> -.09f
        4 -> '.'
        5 -> "..."
        6 -> null
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int, konstue_2: Byte, konstue_3: TypesProvider) {
    when (konstue_1) {
        1 -> -.09 % 10L
        3 -> konstue_2 / -5
        2 -> konstue_3.getChar() - 11 + 90
        4 -> 100
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int, konstue_2: Boolean, konstue_3: Long) {
    when (konstue_1) {
        1 -> konstue_2
        2 -> !konstue_2
        3 -> getBoolean() && konstue_2
        5 -> getChar() != 'a'
        6 -> getList() === getAny()
        7 -> konstue_3 <= 11
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int, konstue_2: String, konstue_3: String) {
    when (konstue_1) {
        1 -> "..." + konstue_2 + "" + "$konstue_3" + "..."
        2 -> konstue_2 + getString()
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when (konstue_1) {
        1 -> "3"
        2 -> ""
        3 -> when (konstue_3) {
            else -> ""
        }
        4 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            null -> "3"
            else -> ""
        }
        5 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            else -> ""
        }
        6 -> when (konstue_3) {
            else -> ""
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) = when (konstue_1) {
    1 -> 3
    else -> when (konstue_3) {
        true -> 1
        false -> 2
        null -> 3
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when (konstue_1) {
        1 -> if (konstue_2 > 1000) "1"
        2 -> if (konstue_2 > 1000) "1"
            else "2"
        3 -> if (konstue_2 < 100) "1"
            else if (konstue_2 < 10) "2"
            else "4"
        4 -> if (konstue_3 == null) "1"
            else if (konstue_3) "2"
            else if (!konstue_3) "3"
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Int, konstue_2: Int) = when (konstue_1) {
    1 -> if (konstue_2 > 1000) "1"
    else "2"
    else -> if (konstue_2 < 100) "1"
    else if (konstue_2 < 10) "2"
    else "4"
}

/*
 * TESTCASE NUMBER: 9
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-37249
 */
fun case_9(konstue_1: Int, konstue_2: String, konstue_3: String): Any {
    return when (konstue_1) {
        1 -> try { 4 } catch (e: Exception) { 5 }
        2 -> try { throw Exception() } catch (e: Exception) { konstue_2 }
        else -> try { throw Exception() } catch (e: Exception) { {konstue_3} } finally { }
    }
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Int, konstue_2: String?, konstue_3: String?) {
    when (konstue_1) {
        1 -> konstue_2 ?: true
        2 -> konstue_2 ?: konstue_3 ?: true
        3 -> konstue_2!! <!USELESS_ELVIS!>?: true<!>
    }
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Int) {
    when (konstue_1) {
        1 -> 1..10
        2 -> -100L..100L
        3 -> -getInt()..getLong()
    }
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: Int, konstue_2: Collection<Int>, konstue_3: Collection<Int>?) {
    when (konstue_1) {
        1 -> konstue_2 as List<Int>
        2 -> konstue_2 as? List<Int>
        3 -> konstue_3 <!UNCHECKED_CAST!>as? MutableMap<Int, Int><!>
        4 -> (konstue_2 <!UNCHECKED_CAST!>as? Map<Int, Int><!>) as MutableMap<Int, Int>
    }
}

// TESTCASE NUMBER: 13
fun case_13(konstue_1: Int, konstue_2: Int, konstue_3: Int, konstue_4: Boolean) {
    var mutableValue1 = konstue_2
    var mutableValue2 = konstue_3

    when (konstue_1) {
        1 -> ++mutableValue1
        2 -> --mutableValue2
        3 -> !konstue_4
    }
}

// TESTCASE NUMBER: 14
fun case_14(konstue_1: Int, konstue_2: Int, konstue_3: Int, konstue_4: Boolean?) {
    var mutableValue1 = konstue_2
    var mutableValue2 = konstue_3

    when (konstue_1) {
        1 -> mutableValue1++
        2 -> mutableValue2--
        3 -> konstue_4!!
    }
}

// TESTCASE NUMBER: 15
fun case_15(konstue_1: Int, konstue_2: List<Int>, konstue_3: List<List<List<List<Int>>>>) {
    when (konstue_1) {
        1 -> konstue_2[0]
        2 -> konstue_3[0][-4][1][-1]
    }
}

// TESTCASE NUMBER: 16
fun case_16(konstue_1: Int, konstue_2: Class, konstue_3: Class?, konstue_4: Int) {
    fun __fun_1(): () -> Unit { return fun() { } }

    when (konstue_1) {
        1 -> funWithoutArgs()
        2 -> __fun_1()()
        3 -> konstue_2.fun_2(konstue_4)
        4 -> konstue_3?.fun_2(konstue_4)
        5 -> konstue_3!!.fun_2(konstue_4)
    }
}

// TESTCASE NUMBER: 17
fun case_17(konstue_1: Int, konstue_2: Class, konstue_3: Class?) {
    when (konstue_1) {
        1 -> konstue_2.prop_1
        2 -> konstue_3?.prop_1
        3 -> konstue_2::prop_1.get()
        4 -> konstue_3!!::prop_3.get()
    }
}

// TESTCASE NUMBER: 18
fun case_18(konstue_1: Int) {
    konst fun_1 = fun(): Int { return 0 }

    when (konstue_1) {
        1 -> fun() {}
        2 -> fun(): Int { return 1 }
        3 -> fun(): () -> Unit { return fun() {} }
        4 -> fun_1
    }
}

// TESTCASE NUMBER: 19
fun case_19(konstue_1: Int): Any {
    konst lambda_1 = { 0 }

    return when (konstue_1) {
        1 -> lambda_1
        2 -> { { {} } }
        else -> { -> (Int)
            { arg: Int -> { { println(arg) } } }
        }
    }
}

// TESTCASE NUMBER: 20
fun case_20(konstue_1: Int) {
    konst object_1 = object {
        konst prop_1 = 1
    }

    when (konstue_1) {
        1 -> object {}
        2 -> object {
            private fun fun_1() { }
            konst prop_1 = 1
        }
        3 -> object_1
    }
}

// TESTCASE NUMBER: 21
class A {
    konst prop_1 = 1
    konst lambda_1 = { 1 }
    fun fun_1(): Int { return 1 }

    fun case_21(konstue_1: Int) {
        when (konstue_1) {
            1 -> this
            2 -> ((this))
            3 -> this::prop_1.get()
            4 -> this.prop_1
            5 -> this.lambda_1()
            6 -> this::lambda_1.get()()
            7 -> this.fun_1()
            8 -> this::fun_1.invoke()
        }
    }
}

// TESTCASE NUMBER: 22
fun case_22(konstue_1: Int) {
    when (konstue_1) {
        1 -> throw Exception()
        2 -> throw throw throw Exception()
    }
}

// TESTCASE NUMBER: 23
fun case_23(konstue_1: Int) {
    fun r_1() {
        when (konstue_1) {
            1 -> return
            2 -> return return return
        }
    }

    fun r_2(): List<Int>? {
        when (konstue_1) {
            1 -> return listOf(0, 1, 2)
            2 -> return null
        }

        return null
    }
}

// TESTCASE NUMBER: 24
fun case_24(konstue_1: Int) {
    loop1@ while (true) {
        loop2@ while (true) {
            when (konstue_1) {
                1 -> continue@loop1
                2 -> continue@loop2
            }
        }
    }
}

// TESTCASE NUMBER: 25
fun case_25(konstue_1: Int) {
    loop1@ while (true) {
        loop2@ while (true) {
            when (konstue_1) {
                1 -> break@loop1
                2 -> break@loop2
            }
        }
    }
}
