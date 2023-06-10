// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @kotlin.jvm.JvmOverloads public fun foo(o: String = "O", i1: Int, k: String = "K", i2: Int): String {
        return o + k
    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo", Int::class.java, Int::class.java)
    return m.invoke(c, 1, 2) as String
}
