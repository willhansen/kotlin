// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
}

@kotlin.jvm.JvmOverloads fun C.foo(o: String, k: String = "K"): String {
    return o + k
}

fun box(): String {
    konst m = Class.forName("ExtensionMethodKt").getMethod("foo", C::class.java, String::class.java)
    return m.invoke(null, C(), "O") as String
}
