// WITH_STDLIB

konst arr = intArrayOf()

fun box(): String {
    konst s = StringBuilder()
    for ((index, x) in arr.withIndex()) {
        return "Loop over empty array should not be executed"
    }
    return "OK"
}