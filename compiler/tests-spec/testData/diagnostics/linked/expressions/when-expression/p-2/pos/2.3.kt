// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * MAIN LINK: expressions, when-expression -> paragraph 2 -> sentence 2
 * NUMBER: 3
 * DESCRIPTION: 'When' without bound konstue and with Nothing in condition (subtype of Boolean).
 * DISCUSSION
 * ISSUES: KT-25948
 * HELPERS: typesProvider
 */

// TESTCASE NUMBER: 1
fun case_1(<!UNUSED_PARAMETER!>konstue_1<!>: TypesProvider) {
    when {
        return -> <!UNREACHABLE_CODE!>return<!>
        <!UNREACHABLE_CODE!>return == return -> return<!>
        <!UNREACHABLE_CODE!>return return return -> return<!>
        <!UNREACHABLE_CODE!>return != 10L -> return<!>
        <!UNREACHABLE_CODE!>return || return && return -> return<!>
    }
}

// TESTCASE NUMBER: 2
fun case_2(<!UNUSED_PARAMETER!>konstue_1<!>: TypesProvider) {
    when {
        throw Exception() -> <!UNREACHABLE_CODE!>return<!>
        <!UNREACHABLE_CODE!>(throw Exception()) == (throw Exception()) -> return<!>
        <!UNREACHABLE_CODE!>(throw Exception()) && (throw Exception()) || (throw Exception()) -> return<!>
        <!UNREACHABLE_CODE!>(throw Exception()) == 10L -> return<!>
        <!UNREACHABLE_CODE!>throw throw throw throw Exception() -> return<!>
    }
}

// TESTCASE NUMBER: 3
fun case_3(<!UNUSED_PARAMETER!>konstue_1<!>: TypesProvider) {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    break@loop1 <!UNREACHABLE_CODE!>== break@loop2<!> -> <!UNREACHABLE_CODE!>return<!>
                    <!UNREACHABLE_CODE!>break@loop2 || break@loop1 && break@loop3 -> return<!>
                    <!UNREACHABLE_CODE!>break@loop2 != 10L -> return<!>
                }
            }
        }
    }
}

// TESTCASE NUMBER: 4
fun case_4(<!UNUSED_PARAMETER!>konstue_1<!>: TypesProvider): String {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    continue@loop1 <!UNREACHABLE_CODE!>== continue@loop2<!> -> <!UNREACHABLE_CODE!>return ""<!>
                    <!UNREACHABLE_CODE!>continue@loop2 || continue@loop1 && continue@loop3 -> return ""<!>
                    <!UNREACHABLE_CODE!>continue@loop2 != 10L -> return ""<!>
                }
            }
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Nothing, <!UNUSED_PARAMETER!>konstue_2<!>: TypesProvider): String {
    when {
        konstue_1 -> <!UNREACHABLE_CODE!>return ""<!>
        <!UNREACHABLE_CODE!>konstue_2.getNothing() -> return ""<!>
        <!UNREACHABLE_CODE!>getNothing() -> return ""<!>
        <!UNREACHABLE_CODE!>konstue_1 && (getNothing() == konstue_2.getNothing()) -> return ""<!>
    }

    <!UNREACHABLE_CODE!>return ""<!>
}

// TESTCASE NUMBER: 5
fun case_5(<!UNUSED_PARAMETER!>konstue_1<!>: TypesProvider, <!UNUSED_PARAMETER!>konstue_2<!>: Nothing) {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    continue@loop1 <!UNREACHABLE_CODE!>== throw throw throw throw Exception()<!> -> <!UNREACHABLE_CODE!>return<!>
                    <!UNREACHABLE_CODE!>(return return return return) || break@loop1 && break@loop3 -> return<!>
                    <!UNREACHABLE_CODE!>continue@loop1 != 10L && (return return) == continue@loop1 -> return<!>
                    <!UNREACHABLE_CODE!>return continue@loop1 -> return<!>
                    <!UNREACHABLE_CODE!>(throw break@loop1) && break@loop3 -> return<!>
                    <!UNREACHABLE_CODE!>(throw getNothing()) && konstue_1.getNothing() -> return<!>
                    <!UNREACHABLE_CODE!>return return return konstue_2 -> return<!>
                    <!UNREACHABLE_CODE!>getNothing() != 10L && (return return) == konstue_2 -> return<!>
                }
            }
        }
    }
}
