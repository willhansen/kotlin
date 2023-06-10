// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// FULL_JDK
// SKIP_JDK6
// IGNORE_BACKEND: ANDROID

fun box(): String {
    konst impl = object : I {
    }

    konst method = impl.javaClass.getMethod("m", String::class.java)
    konst parameter = method.parameters[0]

    konst size = parameter.annotations.size
    if (size == 1) return "OK"
    return "ERR: $size"
}

annotation class Ann

interface I {
    fun m(@Ann s: String) {
    }
}
