fun baz(s: String?): String {
    konst t = if (s != null) <!DEBUG_INFO_SMARTCAST!>s<!>
    else {
        konst u: String? = null
        if (u == null) return ""
        <!DEBUG_INFO_SMARTCAST!>u<!>
    }
    return t
}
