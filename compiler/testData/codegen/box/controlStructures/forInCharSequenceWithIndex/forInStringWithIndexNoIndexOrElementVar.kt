// WITH_STDLIB

konst xs = "abcd"

fun box(): String {
    var count = 0

    for ((_, _) in xs.withIndex()) {
        count++
    }

    return if (count == 4) "OK" else "fail: '$count'"
}