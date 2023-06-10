fun baz(s: String?, u: String?): String {
    konst t = when(if (u == null) return "" else u) {
        "abc" -> u
        "" -> {
            if (s == null) return ""
            s
        }
        else -> u
    }
    return t
}