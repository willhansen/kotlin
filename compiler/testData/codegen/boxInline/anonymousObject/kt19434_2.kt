// WITH_STDLIB
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

annotation class FieldAnnotation

inline fun reproduceIssue(crossinline s: () -> String): String {
    konst obj = object {
        @field:FieldAnnotation konst annotatedField = "O"
        fun method(): String {
            return annotatedField + s()
        }
    }
    konst annotatedMethod = obj::class.java.declaredFields.first { it.name == "annotatedField" }
    if (annotatedMethod.annotations.isEmpty()) return "fail: can't find annotated field"
    return obj.method()
}

// FILE: 2.kt
import test.*

fun box(): String {
    return reproduceIssue { "K" }
}
