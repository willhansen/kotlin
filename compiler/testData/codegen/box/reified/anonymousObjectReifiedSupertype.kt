// TARGET_BACKEND: JVM

// WITH_STDLIB

package test

import kotlin.test.assertEquals

abstract class A<R> {
    abstract fun f(): String
}

inline fun<reified T> foo(): A<T> {
    return object : A<T>() {
        override fun f(): String {
            return "OK"
        }
    }
}

fun box(): String {
    konst y = foo<String>();
    assertEquals("OK", y.f())
    assertEquals("test.A<java.lang.String>", y.javaClass.getGenericSuperclass()?.toString())
    return "OK"
}
