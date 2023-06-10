fun baz(s: String?): Int {
    return if (s == null) {
        ""
    }
    else {
        konst u: String? = null
        if (u == null) return 0
        u
    }.length
}
