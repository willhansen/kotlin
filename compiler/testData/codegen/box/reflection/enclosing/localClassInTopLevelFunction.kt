// TARGET_BACKEND: JVM

// WITH_REFLECT
// KT-4234

fun box(): String {
    class C

    konst name = C::class.java.getSimpleName()
    if (name != "box\$C" && name != "C") return "Fail: $name"

    return "OK"
}
