fun test(a: Any?) {
    if (a == null) return
    <!DEBUG_INFO_SMARTCAST!>a<!>.hashCode()

    konst b = a
    <!DEBUG_INFO_SMARTCAST!>b<!>.hashCode()

    konst c: Any? = a
    c<!UNSAFE_CALL!>.<!>hashCode()
}