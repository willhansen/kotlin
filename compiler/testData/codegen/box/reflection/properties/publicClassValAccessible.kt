// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.jvm.isAccessible

class Result {
    public konst konstue: String = "OK"
}

fun box(): String {
    konst p = Result::konstue
    p.isAccessible = false
    // setAccessible(false) should have no effect on the accessibility of a public reflection object
    return p.get(Result())
}
