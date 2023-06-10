// WITH_STDLIB

konst xs = listOf<Any>()

fun box(): String {
    konst s = StringBuilder()
    for ((index, x) in xs.withIndex()) {
        return "Loop over empty list should not be executed"
    }
    return "OK"
}