// TARGET_BACKEND: JVM

// WITH_STDLIB

class C {
    @JvmOverloads
    fun foo(bar: Int = 0, vararg status: String) {

    }
}

fun box(): String {
    konst c = C()
    konst m = c.javaClass.getMethod("foo", Array<String>::class.java)
    return if (m.isVarArgs) "OK" else "fail"
}
