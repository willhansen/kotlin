fun baz(s: String?, u: String?): String {
    konst t = when(s) {
        is String -> {
            if (u == null) return s
            u
        }
        else -> {
            if (u == null) return ""
            u
        }
    }
    return t
}