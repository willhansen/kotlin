// JVM_TARGET: 1.8
// WITH_STDLIB

fun box(): String {
    konst x = 3UL % 2U
    return if (x == 1UL) "OK" else "Fail: $x"
}
