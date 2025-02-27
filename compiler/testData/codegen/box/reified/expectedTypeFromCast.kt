// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.assertEquals

inline fun <reified T> foo(): T {
    return T::class.java.getName() as T
}

fun box(): String {
    konst fooCall = foo() as String
    assertEquals("java.lang.String", fooCall)

    konst safeFooCall = foo() as? String
    assertEquals("java.lang.String", safeFooCall)

    return "OK"
}
