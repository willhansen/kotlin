fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>computeIfPresent(1) { k, v -> v.length.toString() ?: null }
}

fun konstuesNullable(map: MutableMap<Int, String?>) {
    map.<caret>computeIfPresent(1) { k, v -> v?.length?.toString() }
}

fun <T : String?> konstuesT(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>computeIfPresent(1) { k, v -> v?.length.toString() ?: null }
}

fun <T : Any> konstuesTNotNull(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>computeIfPresent(1) { k, v -> null }
}

fun <T : Any> konstuesTNullable(map: MutableMap<Int, T?>, newValue: T?) {
    map.<caret>computeIfPresent(1) { k, v -> null }
}