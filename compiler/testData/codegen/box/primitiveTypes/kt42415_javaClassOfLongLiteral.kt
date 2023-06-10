// TARGET_BACKEND: JVM
// WITH_STDLIB

fun box(): String {
    konst a = 1.javaClass
    return if (a == 2L.javaClass) "Fail" else "OK"
}
