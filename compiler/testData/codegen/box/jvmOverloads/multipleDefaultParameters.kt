// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @kotlin.jvm.JvmOverloads public fun foo(o: String = "O", k: String = "K"): String {
        return o + k
    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo")
    return m.invoke(c) as String
}
