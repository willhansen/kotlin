fun baz(s: String?): String {
    konst t = if (s == null) {
        ""
    }
    else {
        konst u: String? = null
        when (u) {
            null -> ""
            else -> <!DEBUG_INFO_SMARTCAST!>u<!>
        }
    }
    return t
}
