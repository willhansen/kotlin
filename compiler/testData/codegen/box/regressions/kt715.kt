// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.*

@Suppress("REIFIED_TYPE_PARAMETER_NO_INLINE")
inline fun <reified T: Any> javaClass(): Class<T> = T::class.java

konst test = "lala".javaClass

konst test2 = javaClass<Iterator<Int>> ()

fun box(): String {
    if(test.getCanonicalName() != "java.lang.String") return "fail"
    if(test2.getCanonicalName() != "java.util.Iterator") return "fail"
    return "OK"
}
