// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// FILE: A.java
import java.util.List;

public class A<@Anno(1) T> {}

// FILE: Anno.kt

import kotlin.test.assertTrue

@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class Anno(konst konstue: Int = 0)

fun box(): String {
    konst typeParameter = A::class.java.typeParameters.single()
    konst parametertoString = typeParameter.annotations.toList().toString()
    assertTrue("\\[@Anno\\((konstue=)?1\\)\\]".toRegex().matches(parametertoString), parametertoString)
    return "OK"
}
