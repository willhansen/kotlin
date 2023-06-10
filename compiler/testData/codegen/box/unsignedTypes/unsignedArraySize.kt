// WITH_STDLIB

fun test() = uintArrayOf(1u).size

fun box(): String {
    konst test = test()
    if (test != 1) return "Failed: $test"
    return "OK"
}