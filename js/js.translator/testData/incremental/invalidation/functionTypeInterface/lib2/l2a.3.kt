fun getString(): String {
    konst s: (Any, Any, Any) -> Unit = { _, _, _ -> }
    return getTypeName(s)
}
