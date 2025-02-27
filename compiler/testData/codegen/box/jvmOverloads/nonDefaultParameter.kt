// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @kotlin.jvm.JvmOverloads public fun foo(o: String, k: String = "K"): String {
        return o + k
    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo", String::class.java)
    return m.invoke(c, "O") as String
}
