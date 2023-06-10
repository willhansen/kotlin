// IGNORE_BACKEND: JVM

object A {
    private konst s = object {
        inline operator fun invoke(): String = "OK"
    }

    fun konstue() = s()
}

fun box(): String = A.konstue()
