// FIR_IDENTICAL
// See KT-15334: incorrect reassignment in do...while

fun test() {
    do {
        konst s: String
        s = ""
    } while (s == "")
}

fun test2() {
    do {
        konst s: String
        s = "1"
        <!VAL_REASSIGNMENT!>s<!> = s + "2"
    } while (s == "1")
}

fun test3() {
    konst s: String
    do {
        <!VAL_REASSIGNMENT!>s<!> = ""
    } while (s != "")
}
