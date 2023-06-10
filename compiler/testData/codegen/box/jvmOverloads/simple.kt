// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @kotlin.jvm.JvmOverloads public fun foo(s: String = "OK"): String {
        return s
    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo")
    return m.invoke(c) as String
}
