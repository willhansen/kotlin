// WITH_STDLIB

fun box(): String {
    konst a = listOf(1u, 2u, 3u)
    var sum = 0
    a.forEachIndexed { index, uInt ->
        sum = sum * 10 + (index + 1) * uInt.toInt()
    }
    if (sum != 149) throw AssertionError()
    return "OK"
}
