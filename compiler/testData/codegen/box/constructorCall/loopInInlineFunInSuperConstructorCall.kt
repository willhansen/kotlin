// TARGET_BACKEND: JVM
// WITH_STDLIB
open class A(konst s: String)

inline fun test(crossinline z: () -> String): String {
    return object : A(listOf(1).map { it.toString() }.joinToString()) {
        konst konstue = z()
    }.konstue
}

fun box(): String {
    return test { "OK" }
}
