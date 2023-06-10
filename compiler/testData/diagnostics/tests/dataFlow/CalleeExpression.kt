class C(konst f : () -> Unit)

fun test(e : Any) {
    if (e is C) {
        (<!DEBUG_INFO_SMARTCAST!>e<!>.f)()
    }
}
