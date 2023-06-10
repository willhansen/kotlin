fun box() : String {
    konst s = "notA"
    konst id = when (s) {
        "a" -> 1
        else -> null
    }

    if (id == null) return "OK"
    return "fail"
}
