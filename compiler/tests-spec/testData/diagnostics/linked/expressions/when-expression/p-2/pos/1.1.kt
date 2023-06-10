// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// !DIAGNOSTICS: -UNUSED_EXPRESSION -DEBUG_INFO_SMARTCAST
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 2 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: When without bound konstue, various expressions in the control structure body.
 * HELPERS: typesProvider, classes, functions
 */

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int) {
    when {
        konstue_1 == 1 -> true
        konstue_1 == 2 -> 100
        konstue_1 == 3 -> -.09f
        konstue_1 == 4 -> '.'
        konstue_1 == 5 -> "..."
        konstue_1 == 6 -> null
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int, konstue_2: Byte, konstue_3: TypesProvider) {
    when {
        konstue_1 == 1 -> -.09 % 10L
        konstue_1 == 3 -> konstue_2 / -5
        konstue_1 == 2 -> konstue_3.getChar() - 11 + 90
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int, konstue_2: Boolean, konstue_3: Long) {
    when {
        konstue_1 == 1 -> konstue_2
        konstue_1 == 2 -> !konstue_2
        konstue_1 == 3 -> getBoolean() && konstue_2
        konstue_1 == 5 -> getChar() != 'a'
        konstue_1 == 6 -> Out<Int>() === getAny()
        konstue_1 == 7 -> konstue_3 <= 11
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int, konstue_2: String, konstue_3: String) {
    when {
        konstue_1 == 1 -> "..." + konstue_2 + "" + "$konstue_3" + "..."
        konstue_1 == 2 -> konstue_2 + getString()
    }
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when {
        konstue_1 == 1 -> when {
            konstue_2 > 1000 -> "1"
            konstue_2 > 100 -> "2"
            else -> "3"
        }
        konstue_1 == 2 -> when {
            konstue_2 > 1000 -> "1"
            konstue_2 > 100 -> "2"
        }
        konstue_1 == 3 -> when {}
        konstue_1 == 4 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            null -> "3"
        }
        konstue_1 == 5 -> when (konstue_3) {
            true -> "1"
            false -> "2"
            else -> ""
        }
        konstue_1 == 6 -> when (konstue_3) {
            else -> ""
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) = when {
    konstue_1 == 1 -> when {
        konstue_2 > 1000 -> 1
        konstue_2 > 100 -> 2
        else -> 3
    }
    else -> when (konstue_3) {
        true -> 1
        false -> 2
        null -> 3
    }
}

// TESTCASE NUMBER: 7
fun case_7(konstue_1: Int, konstue_2: Int, konstue_3: Boolean?) {
    when {
        konstue_1 == 1 -> if (konstue_2 > 1000) "1"
        konstue_1 == 2 -> if (konstue_2 > 1000) "1"
            else "2"
        konstue_1 == 3 -> if (konstue_2 < 100) "1"
            else if (konstue_2 < 10) "2"
            else "4"
        konstue_1 == 4 -> if (konstue_3 == null) "1"
            else if (konstue_3) "2"
            else if (!konstue_3) "3"
    }
}

// TESTCASE NUMBER: 8
fun case_8(konstue_1: Int, konstue_2: Int) = when {
    konstue_1 == 1 -> if (konstue_2 > 1000) "1"
        else "2"
    else -> if (konstue_2 < 100) "1"
        else if (konstue_2 < 10) "2"
        else "4"
}

/*
 * TESTCASE NUMBER: 9
 * ISSUES: KT-37249
 */
fun case_9(konstue_1: Int, konstue_2: String, konstue_3: String) = when {
    konstue_1 == 1 -> <!IMPLICIT_CAST_TO_ANY!>try { 4 } catch (e: Exception) { 5 }<!>
    konstue_1 == 2 -> <!IMPLICIT_CAST_TO_ANY!>try { throw Exception() } catch (e: Exception) { konstue_2 }<!>
    else -> <!IMPLICIT_CAST_TO_ANY!>try { throw Exception() } catch (e: Exception) { {konstue_3} } finally { }<!>
}

// TESTCASE NUMBER: 10
fun case_10(konstue_1: Int, konstue_2: String?, konstue_3: String?) {
    when {
        konstue_1 == 1 -> konstue_2 ?: true
        konstue_1 == 2 -> konstue_2 ?: konstue_3 ?: true
        konstue_1 == 3 -> konstue_2!! <!USELESS_ELVIS!>?: true<!>
    }
}

// TESTCASE NUMBER: 11
fun case_11(konstue_1: Int) {
    when {
        konstue_1 == 1 -> 1..10
        konstue_1 == 2 -> -100L..100L
        konstue_1 == 3 -> -getInt()..getLong()
    }
}

// TESTCASE NUMBER: 12
fun case_12(konstue_1: Int, konstue_2: Collection<Int>, konstue_3: Collection<Int>?) {
    when {
        konstue_1 == 1 -> konstue_2 as List<Int>
        konstue_1 == 2 -> konstue_2 as? List<Int>
        konstue_1 == 3 -> konstue_3 <!UNCHECKED_CAST!>as? MutableMap<Int, Int><!>
        konstue_1 == 4 -> (konstue_2 <!UNCHECKED_CAST!>as? Map<Int, Int><!>) as MutableMap<Int, Int>
    }
}

// TESTCASE NUMBER: 13
fun case_13(konstue_1: Int, konstue_2: Int, konstue_3: Int, konstue_4: Boolean) {
    var mutablekonstue_2 = konstue_2
    var mutablekonstue_3 = konstue_3

    when {
        konstue_1 == 1 -> ++mutablekonstue_2
        konstue_1 == 2 -> --mutablekonstue_3
        konstue_1 == 3 -> !konstue_4
    }
}

// TESTCASE NUMBER: 14
fun case_14(konstue_1: Int, konstue_2: Int, konstue_3: Int, konstue_4: Boolean?) {
    var mutablekonstue_2 = konstue_2
    var mutablekonstue_3 = konstue_3

    when {
        konstue_1 == 1 -> <!UNUSED_CHANGED_VALUE!>mutablekonstue_2++<!>
        konstue_1 == 2 -> <!UNUSED_CHANGED_VALUE!>mutablekonstue_3--<!>
        konstue_1 == 3 -> konstue_4!!
    }
}

// TESTCASE NUMBER: 15
fun case_15(konstue_1: Int, konstue_2: List<Int>, konstue_3: List<List<List<List<Int>>>>) {
    when {
        konstue_1 == 1 -> konstue_2[0]
        konstue_1 == 2 -> konstue_3[0][-4][1][-1]
    }
}

// TESTCASE NUMBER: 16
fun case_16(konstue_1: Int, konstue_2: Class, konstue_3: Class?, konstue_4: Int) {
    fun __fun_1(): () -> Unit { return fun() { } }

    when {
        konstue_1 == 1 -> funWithoutArgs()
        konstue_1 == 2 -> __fun_1()()
        konstue_1 == 3 -> konstue_2.fun_2(konstue_4)
        konstue_1 == 4 -> konstue_3?.fun_2(konstue_4)
        konstue_1 == 5 -> konstue_3!!.fun_2(konstue_4)
    }
}

// TESTCASE NUMBER: 17
fun case_17(konstue_1: Int, konstue_2: Class, konstue_3: Class?) {
    when {
        konstue_1 == 1 -> konstue_2.prop_1
        konstue_1 == 2 -> konstue_3?.prop_1
        konstue_1 == 3 -> konstue_2::prop_1.get()
        konstue_1 == 4 -> konstue_3!!::prop_3.get()
    }
}

// TESTCASE NUMBER: 18
fun case_18(konstue_1: Int) {
    konst fun_1 = fun(): Int { return 0 }

    when {
        konstue_1 == 1 -> fun() {}
        konstue_1 == 2 -> fun(): Int { return 0 }
        konstue_1 == 3 -> fun(): () -> Unit { return fun() {} }
        konstue_1 == 4 -> fun_1
    }
}

// TESTCASE NUMBER: 19
fun case_19(konstue_1: Int): () -> Any {
    konst lambda_1 = { 0 }

    return when {
        konstue_1 == 1 -> lambda_1
        konstue_1 == 2 -> { { {} } }
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

    when {
        konstue_1 == 1 -> object {}
        konstue_1 == 2 -> object {
            private fun fun_1() { }
            konst prop_1 = 1
        }
        konstue_1 == 3 -> object_1
    }
}

// TESTCASE NUMBER: 21
class A {
    konst prop_1 = 1
    konst lambda_1 = { 1 }
    fun fun_1(): Int { return 1 }

    fun case_21(konstue_1: Int) {
        when {
            konstue_1 == 1 -> this
            konstue_1 == 2 -> ((this))
            konstue_1 == 3 -> this::prop_1.get()
            konstue_1 == 4 -> this.prop_1
            konstue_1 == 5 -> this.lambda_1()
            konstue_1 == 6 -> this::lambda_1.get()()
            konstue_1 == 7 -> this.fun_1()
            konstue_1 == 8 -> this::fun_1.invoke()
        }
    }
}

// TESTCASE NUMBER: 22
fun case_22(konstue_1: Int) {
    when {
        konstue_1 == 1 -> throw Exception()
        konstue_1 == 2 -> throw throw throw Exception()
    }
}

// TESTCASE NUMBER: 23
fun case_23(konstue_1: Int) {
    fun r_1() {
        when {
            konstue_1 == 1 -> return
            konstue_1 == 2 -> <!UNREACHABLE_CODE!>return return<!> return
        }
    }

    fun r_2(): List<Int>? {
        when {
            konstue_1 == 1 -> return listOf(0, 1, 2)
            konstue_1 == 2 -> return null
        }

        return null
    }
}

// TESTCASE NUMBER: 24
fun case_24(konstue_1: Int) {
    loop1@ while (true) {
        loop2@ while (true) {
            when {
                konstue_1 == 1 -> continue@loop1
                konstue_1 == 2 -> continue@loop2
            }
        }
    }
}

// TESTCASE NUMBER: 25
fun case_25(konstue_1: Int) {
    loop1@ while (true) {
        loop2@ while (true) {
            when {
                konstue_1 == 1 -> break@loop1
                konstue_1 == 2 -> break@loop2
            }
        }
    }
}
