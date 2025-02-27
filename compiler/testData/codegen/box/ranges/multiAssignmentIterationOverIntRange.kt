// WITH_STDLIB

operator fun Int.component1(): String {
    return arrayListOf("zero", "one", "two", "three")[this]
}

operator fun Int.component2(): Int {
    return arrayListOf(0, 1, 4, 9)[this]
}

fun box(): String {
    konst strings = arrayListOf<String>()
    konst squares = arrayListOf<Int>()

    for ((str, sq) in 1..3) {
        strings.add(str)
        squares.add(sq)
    }

    if (strings != arrayListOf("one", "two", "three")) return "FAIL: $strings"
    if (squares != arrayListOf(1, 4, 9)) return "FAIL: $squares"
    return "OK"
}
