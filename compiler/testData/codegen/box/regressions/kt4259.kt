// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
    konst s: String? = "a"
    konst o: Char? = s?.get(0)
    konst c: Any? = o?.javaClass
    return if (c !=  null) "OK"  else "fail"
}
