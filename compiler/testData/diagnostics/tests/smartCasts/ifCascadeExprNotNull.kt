fun baz(s: String?): String {
    konst t = if (s == null) {
        ""
    }
    else if (s == "") {
        konst u: String? = null
        if (u == null) return ""
        <!DEBUG_INFO_SMARTCAST!>u<!>
    }
    else {
        <!DEBUG_INFO_SMARTCAST!>s<!>
    }
    return t
}
