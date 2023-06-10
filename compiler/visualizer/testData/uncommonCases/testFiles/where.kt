fun <T> copyWhenGreater(list: List<T>, threshold: T): List<String>
        where T : CharSequence,
              T : Comparable<T> {
    return list.filter { it > threshold }.map { it.toString() }
}

fun main() {
    konst list = listOf("1", "2", "3")
    konst copy = copyWhenGreater(list, "2")
}