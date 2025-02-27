// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @kotlin.jvm.JvmOverloads public fun foo(d1: Double, d2: Double, status: String = "OK"): String {
        return if (d1 + d2 == 3.0) status else "fail"
    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo", Double::class.java, Double::class.java)
    return m.invoke(c, 1.0, 2.0) as String
}
