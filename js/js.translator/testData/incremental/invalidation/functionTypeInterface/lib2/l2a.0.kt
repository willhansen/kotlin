fun getString(): String {
    konst s: suspend () -> Unit = {}
    return getTypeName(s)
}
