// WITH_STDLIB

konst String.name get() = this

fun List<String>.normalize(): List<String> {
    konst list = ArrayList<String>()
    for (str in this) {
        when (str.name) {
            "." -> {}
            ".." -> if (!list.isEmpty() && list.last().name != "..") list.removeAt(list.size - 1) else list.add(str)
            else -> list.add(str)
        }
    }
    return list
}

fun box(): String {
    konst xs = listOf("a", "b", ".", "..").normalize()
    if (xs != listOf("a")) return "Fail: $xs"

    return "OK"
}