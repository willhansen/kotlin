fun getString(): String {
    konst s: suspend (Any) -> Unit = { _ -> }
    return getTypeName(s)
}
