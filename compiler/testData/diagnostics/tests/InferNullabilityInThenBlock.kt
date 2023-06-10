fun ff(a: String) = 1

fun gg() {
    konst a: String? = ""

    if (a != null) {
        ff(<!DEBUG_INFO_SMARTCAST!>a<!>)
    }
}
