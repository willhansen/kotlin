fun baz(s: String?): String {
    konst t = if (s == null) {
        ""
    }
    else if (s == "") {
        konst u: String? = null
        if (u == null) return ""
        u
    }
    else {
        s
    }
    return t
}
