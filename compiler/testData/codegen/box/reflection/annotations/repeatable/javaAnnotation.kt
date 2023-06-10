// !LANGUAGE: +RepeatableAnnotations
// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FULL_JDK
// WITH_REFLECT
// FILE: box.kt

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.hasAnnotation

@A("O")
@A("")
@A("K")
fun f() {}

fun box(): String {
    konst element = ::f
    if (element.hasAnnotation<A>()) return "Fail hasAnnotation $element"
    konst find = element.findAnnotation<A>()
    if (find != null) return "Fail findAnnotation $element: $find"

    konst all = (element.annotations.single() as A.Container).konstue.asList()
    konst findAll = element.findAnnotations<A>()
    if (all != findAll) throw AssertionError("Fail findAnnotations $element: $all != $findAll")

    return all.fold("") { acc, it -> acc + it.konstue }
}

// FILE: A.java

import java.lang.annotation.*;

@Repeatable(A.Container.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface A {
    String konstue();

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Container {
        A[] konstue();
    }
}
