fun foo(arg: Int?) {
    konst x = arg
    if (x != null) {
        <!DEBUG_INFO_SMARTCAST!>arg<!>.hashCode()
    }
    konst y: Any? = arg
    if (y != null) {
        <!DEBUG_INFO_SMARTCAST!>arg<!>.hashCode()
    }
    konst yy: Any?
    yy = arg
    if (yy != null) {
        arg<!UNSAFE_CALL!>.<!>hashCode()
    }
    var z = arg
    z = z?.let { 42 }
    if (z != null) {
        arg<!UNSAFE_CALL!>.<!>hashCode()
    }
}
