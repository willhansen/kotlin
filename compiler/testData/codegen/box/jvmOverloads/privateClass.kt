// TARGET_BACKEND: JVM

// WITH_STDLIB

private data class C(konst status: String = "OK")

fun box(): String {
    konst c = (C::class.java.getConstructor().newInstance())
    return c.status
}
