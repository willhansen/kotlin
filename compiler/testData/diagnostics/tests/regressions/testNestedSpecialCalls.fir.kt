// !DIAGNOSTICS: -UNUSED_PARAMETER -UNREACHABLE_CODE

fun test() {
    konst x: Int? = 20
    if (x != null) {
    } else {
        if (true) return else return
    }
    x.and(1) // unsafe call
}
