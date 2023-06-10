// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: TypesProvider) {
    when {
        return -> return
        return == return -> return
        return return return -> return
        return != 10L -> return
        return || return && return -> return
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: TypesProvider) {
    when {
        throw Exception() -> return
        (throw Exception()) == (throw Exception()) -> return
        (throw Exception()) && (throw Exception()) || (throw Exception()) -> return
        (throw Exception()) == 10L -> return
        throw throw throw throw Exception() -> return
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: TypesProvider) {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    break@loop1 == break@loop2 -> return
                    break@loop2 || break@loop1 && break@loop3 -> return
                    break@loop2 != 10L -> return
                }
            }
        }
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: TypesProvider): String {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    continue@loop1 == continue@loop2 -> return ""
                    continue@loop2 || continue@loop1 && continue@loop3 -> return ""
                    continue@loop2 != 10L -> return ""
                }
            }
        }
    }
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Nothing, konstue_2: TypesProvider): String {
    when {
        konstue_1 -> return ""
        konstue_2.getNothing() -> return ""
        getNothing() -> return ""
        konstue_1 && (getNothing() == konstue_2.getNothing()) -> return ""
    }

    return ""
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: TypesProvider, konstue_2: Nothing) {
    loop1@ while (true) {
        loop2@ while (true) {
            loop3@ while (true) {
                when {
                    continue@loop1 == throw throw throw throw Exception() -> return
                    (return return return return) || break@loop1 && break@loop3 -> return
                    continue@loop1 != 10L && (return return) == continue@loop1 -> return
                    return continue@loop1 -> return
                    (throw break@loop1) && break@loop3 -> return
                    (throw getNothing()) && konstue_1.getNothing() -> return
                    return return return konstue_2 -> return
                    getNothing() != 10L && (return return) == konstue_2 -> return
                }
            }
        }
    }
}
