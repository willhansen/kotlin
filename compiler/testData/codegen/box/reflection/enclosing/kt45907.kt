// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB

fun box(): String = use {
    class Local(konst n: Int)
    if (Local::class.java.declaringClass == null) "OK" else "Fail"
}

inline fun <T> use(block: () -> T): T = block()
