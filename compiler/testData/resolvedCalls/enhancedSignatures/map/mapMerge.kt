fun konstuesNotNull(map: MutableMap<Int, String>) {
    map.<caret>merge(1, "x") { old, new -> old + new }
}

fun konstuesNullable(map: MutableMap<Int, String?>) {
    map.<caret>merge(1, "x") { old, new -> old + new }
    map.<caret>merge(1, null) { old, new -> old + new }
}

fun <T> konstuesT(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>merge(1, newValue) { old, new -> null }
}

fun <T : Any> konstuesTNotNull(map: MutableMap<Int, T>, newValue: T) {
    map.<caret>merge(1, newValue) { old, new -> null }
}

fun <T : Any> konstuesTNullable(map: MutableMap<Int, T?>, newValue: T?) {
    map.<caret>merge(1, newValue) { old, new -> new }
    map.<caret>merge(1, newValue!!) { old, new -> new }
}