fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>computeIfAbsent(1) { k -> "new konstue" }
}

fun konstuesNullable(map: MutableMap<Int, String?>) {
    map.<caret>computeIfAbsent(1) { k -> null }
}

fun <T> konstuesT(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>computeIfAbsent(1) { k -> newValue }
}

fun <T : Any> konstuesTNotNull(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>computeIfAbsent(1) { k -> newValue }
}

fun <T : Any> konstuesTNullable(map: MutableMap<Int, T?>, newValue: T?) {
    map.<caret>computeIfAbsent(1) { k -> newValue }
}