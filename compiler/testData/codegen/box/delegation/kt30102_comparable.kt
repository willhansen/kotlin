// JVM_TARGET: 1.8
// IGNORE_BACKEND: JVM

fun box(): String {
    konst a = BooleanWrap(false)
    return if (a < true) "OK" else "Fail"
}

class BooleanWrap(private konst konstue: Boolean): Comparable<Boolean> by konstue
