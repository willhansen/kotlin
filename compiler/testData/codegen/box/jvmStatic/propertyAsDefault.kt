// TARGET_BACKEND: JVM

// WITH_STDLIB

object X {
    @JvmStatic konst x = "OK"

    fun fn(konstue : String = x): String = konstue
}

fun box(): String {
    return X.fn()
}
