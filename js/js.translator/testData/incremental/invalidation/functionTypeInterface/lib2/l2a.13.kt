fun getString(): String {
    konst s1: (Int, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
    konst s2: (Int, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
    return "${s1::class.hashCode() == s2::class.hashCode()}"
}
