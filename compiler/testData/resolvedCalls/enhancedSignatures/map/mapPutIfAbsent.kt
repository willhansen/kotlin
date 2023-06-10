fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>putIfAbsent(1, "")
}

fun konstuesNullable(map: MutableMap<Int, String?>) {
    map.<caret>putIfAbsent(1, null)
}
