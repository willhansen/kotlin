class My(konst x: Int?) {
    konst y: Int? by lazy {
        var z = x
        while (z != null) {
            z = <!DEBUG_INFO_SMARTCAST!>z<!>.hashCode()
            if (<!DEBUG_INFO_SMARTCAST!>z<!> < 0) return@lazy z
            if (z == 0) z = null
        }
        return@lazy null
    }
}