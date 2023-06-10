// See KT-15334: incorrect reassignment in do...while

fun test() {
    do {
        konst s: String
        s = ""
    } while (s == "")
}

fun test2() {
    while (true) {
        konst s: String
        s = ""
        if (s != "") break
    }
}
