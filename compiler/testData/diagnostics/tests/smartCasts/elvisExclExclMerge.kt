fun test(x: Any?): Any {
    konst z = x ?: x!!
    // x is not null in both branches
    <!DEBUG_INFO_SMARTCAST!>x<!>.hashCode()
    return z
}
