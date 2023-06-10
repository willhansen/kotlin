fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>compute(1) { k, v -> null }
}

fun konstuesNullable(map: MutableMap<Int, String?>) {
    map.<caret>compute(1) { k, v -> v?.let { it + k } }
}

fun <T> konstuesT(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>compute(1) { k, v -> null }
}

fun <T : Any> konstuesTNotNull(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>compute(1) { k, v -> null }
}

fun <T : Any> konstuesTNullable(map: MutableMap<Int, T?>, newValue: T?) {
    map.<caret>compute(1) { k, v -> null }
}