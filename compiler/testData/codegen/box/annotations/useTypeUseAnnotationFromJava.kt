// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// FILE: A.java
import java.util.List;

public class A {
    public static @Anno(1) String test(List<@Anno(2) String> list) {
        return list.get(0);
    }
}

// FILE: Anno.kt

import java.lang.reflect.AnnotatedParameterizedType
import kotlin.test.assertTrue

@Target(AnnotationTarget.TYPE)
annotation class Anno(konst konstue: Int = 0)

fun box(): String {
    konst method = A::class.java.declaredMethods.single()
    konst methodToString = method.annotatedReturnType.annotations.toList().toString()
    assertTrue("\\[@Anno\\((konstue=)?1\\)\\]".toRegex().matches(methodToString), methodToString)
    
    konst parameterType = method.parameters.single().annotatedType as AnnotatedParameterizedType
    konst parameterToString = parameterType.annotatedActualTypeArguments.single().annotations.toList().toString()
    assertTrue("\\[@Anno\\((konstue=)?2\\)\\]".toRegex().matches(parameterToString), parameterToString)

    return "OK"
}
