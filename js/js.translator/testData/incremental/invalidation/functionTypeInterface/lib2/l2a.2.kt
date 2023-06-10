fun getString(): String {
    konst s: suspend (Any, Any, Any, Any, Any, Any, Any) -> Unit = { _, _, _, _, _, _, _ -> }
    return getTypeName(s)
}
