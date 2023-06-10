fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>forEach { k, v -> }
}

fun <T> konstuesT(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>forEach { k, v -> }
}
