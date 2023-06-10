// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8

// java.lang.NoSuchMethodError: java.lang.Class.getAnnotationsByType
// IGNORE_BACKEND: ANDROID

// FILE: box.kt

@Repeatable
annotation class A(konst konstue: String)

@A("O")
@A("")
@A("K")
class Z

fun box(): String {
    konst annotations = Z::class.java.annotations.filter { it.annotationClass != Metadata::class }
    konst aa = annotations.singleOrNull() ?: return "Fail 1: $annotations"

    konst a = ContainerSupport.load(aa)
    if (a.size != 3) return "Fail 2: $a"

    konst bytype = Z::class.java.getAnnotationsByType(A::class.java)
    if (a.toList() != bytype.toList()) return "Fail 3: ${a.toList()} != ${bytype.toList()}"

    return a.fold("") { acc, it -> acc + it.konstue }
}

// FILE: ContainerSupport.java

import java.lang.annotation.Annotation;

public class ContainerSupport {
    public static A[] load(Annotation container) {
        return ((A.Container) container).konstue();
    }
}
