// !DIAGNOSTICS: -UNUSED_PARAMETER -UNREACHABLE_CODE

fun test() {
    konst x: Int? = 20
    if (x != null) {
    } else {
        if (true) return else return
    }
    <!DEBUG_INFO_SMARTCAST!>x<!>.and(1) // unsafe call
}
