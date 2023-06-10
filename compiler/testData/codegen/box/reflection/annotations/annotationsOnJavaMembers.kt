// TARGET_BACKEND: JVM

// WITH_REFLECT
// FILE: J.java

@Anno("J")
public class J {
    @Anno("foo")
    public static int foo = 42;

    @Anno("bar")
    public static void bar() {}

    @Anno("constructor")
    public J() {}
}

// FILE: K.kt

import kotlin.test.assertEquals
import kotlin.reflect.KAnnotatedElement

annotation class Anno(konst konstue: String)

fun box(): String {
    assertEquals("J", getSingleAnnoAnnotation(J::class).konstue)
    assertEquals("foo", getSingleAnnoAnnotation(J::foo).konstue)
    assertEquals("bar", getSingleAnnoAnnotation(J::bar).konstue)
    assertEquals("constructor", getSingleAnnoAnnotation(::J).konstue)

    return "OK"
}

fun getSingleAnnoAnnotation(annotated: KAnnotatedElement): Anno = annotated.annotations.single() as Anno