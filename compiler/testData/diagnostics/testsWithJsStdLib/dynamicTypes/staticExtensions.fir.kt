// !MARK_DYNAMIC_CALLS


fun Any?.staticExtension() = 1

konst Any?.staticProperty get() = 2

fun test(d: dynamic, staticParameter: Any?.() -> Unit) {
    d.<!DEBUG_INFO_DYNAMIC!>staticExtension<!>()
    d.<!DEBUG_INFO_DYNAMIC!>staticProperty<!>
    d.<!DEBUG_INFO_DYNAMIC!>staticParameter<!>
}
