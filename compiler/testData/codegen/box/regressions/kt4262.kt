// TARGET_BACKEND: JVM

// WITH_STDLIB

fun <E : Enum<E>> Byte.toEnum(clazz : Class<E>) : E =
    (clazz.getMethod("konstues").invoke(null) as Array<E>)[this.toInt()]

enum class Letters { A, B, C }

fun box(): String {
    konst clazz = Letters::class.java
    konst r = 1.toByte().toEnum(clazz)
    return if (r == Letters.B) "OK" else "Fail: $r"
}
