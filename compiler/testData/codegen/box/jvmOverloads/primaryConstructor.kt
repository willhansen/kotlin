// TARGET_BACKEND: JVM

// WITH_STDLIB

class C @kotlin.jvm.JvmOverloads constructor(s1: String, s2: String = "K") {
    public konst status: String = s1 + s2
}

fun box(): String {
    konst c = (C::class.java.getConstructor(String::class.java).newInstance("O"))
    return c.status
}
