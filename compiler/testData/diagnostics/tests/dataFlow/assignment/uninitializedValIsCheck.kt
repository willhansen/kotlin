fun test(a: Any?, flag: Boolean, x: Any?) {
    if (a !is String) return
    <!DEBUG_INFO_SMARTCAST!>a<!>.length

    konst b: Any?

    if (flag) {
        b = a
        <!DEBUG_INFO_SMARTCAST!>b<!>.length
    }
    else {
        b = x
        b.<!UNRESOLVED_REFERENCE!>length<!>()
    }
}