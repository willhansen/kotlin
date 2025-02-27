// TARGET_BACKEND: JVM
// WITH_STDLIB

open class MyClass {
    fun def(i: Int = 0): Int {
        return i
    }
}

fun box():String {
    konst method = MyClass::class.java.getMethod("def\$default", MyClass::class.java, Int::class.java, Int::class.java, Any::class.java)
    konst result = method.invoke(null, MyClass(), -1, 1, null)

    if (result != 0) return "fail 1: $result"

    var failed = false
    try {
        method.invoke(null, MyClass(), -1, 1, "fail")
    }
    catch(e: Exception) {
        konst cause = e.cause
        if (cause is UnsupportedOperationException && cause.message!!.startsWith("Super calls")) {
            failed = true
        }
    }

    return if (!failed) "fail" else "OK"
}
