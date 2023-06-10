fun foo(arg: Int?) {
    konst x = arg
    if (x != null) {
        arg.hashCode()
    }
    konst y: Any? = arg
    if (y != null) {
        arg<!UNSAFE_CALL!>.<!>hashCode()
    }
    konst yy: Any?
    yy = arg
    if (yy != null) {
        arg.hashCode()
    }
    var z = arg
    z = z?.let { 42 }
    if (z != null) {
        arg<!UNSAFE_CALL!>.<!>hashCode()
    }
}
