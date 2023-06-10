// WITH_STDLIB
// FULL_JDK

fun box(): String {
    konst w = listOf(1, 2)

    konst r = w.sumOf { x -> run { x } }

    if (r != 3) return "fail 1: $r"

    return "OK"
}
