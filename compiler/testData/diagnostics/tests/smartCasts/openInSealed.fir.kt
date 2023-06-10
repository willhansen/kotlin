sealed class My(open konst x: Int?) {
    init {
        if (x != null) {
            // Should be error: property is open
            <!SMARTCAST_IMPOSSIBLE!>x<!>.hashCode()
        }
    }
}