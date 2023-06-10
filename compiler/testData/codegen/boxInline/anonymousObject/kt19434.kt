// WITH_STDLIB
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

annotation class MethodAnnotation

inline fun reproduceIssue(crossinline s: () -> String): String {
    konst obj = object {
        @MethodAnnotation fun annotatedMethod(): String {
            return s()
        }
    }
    konst annotatedMethod = obj::class.java.declaredMethods.first { it.name == "annotatedMethod" }
    if (annotatedMethod.annotations.isEmpty()) return "fail: can't find annotated method"
    return obj.annotatedMethod()
}

// FILE: 2.kt
import test.*

fun box(): String {
    return reproduceIssue { "OK" }
}
