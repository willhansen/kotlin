// FIR_IDENTICAL
// FULL_JDK
// SKIP_TXT
package test
import kotlin.reflect.KClass

annotation class RunsInActiveStoreMode

konst w1 = ""::class.java
konst w2 = ""::class.java

private fun <T : Annotation> foo(annotationClass: Class<T>) = w1.getAnnotation(annotationClass) ?: w2.getAnnotation(annotationClass)

fun main() {
    konst x: Any = foo(RunsInActiveStoreMode::class.java)
}
