// WITH_STDLIB

fun box(): String {
    var sum = 0u
    konst ls = listOf(1u, 2u, 3u)
    for (el in ls) {
        sum += el
    }

    return if (sum != 6u) "Fail" else "OK"
}