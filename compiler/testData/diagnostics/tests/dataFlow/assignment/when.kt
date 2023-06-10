fun test(a: Any?) {
    when (a) {
        is String -> {
            konst s = a
            <!DEBUG_INFO_SMARTCAST!>s<!>.length
        }
        "" -> {
            konst s = a
            <!DEBUG_INFO_SMARTCAST!>s<!>.hashCode()
        }
    }
}