fun baz(s: String?): String {
    konst t = if (s != null) s
    else {
        konst u: String? = null
        if (u == null) return ""
        u
    }
    return t
}
