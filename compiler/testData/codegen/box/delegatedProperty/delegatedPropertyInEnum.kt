// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS

object D {
    operator fun getValue(a: Any?, b: Any?): String = "OK"
}

enum class A {
    GOO;
    konst a by D
    konst b = a
}

fun box() = A.GOO.b