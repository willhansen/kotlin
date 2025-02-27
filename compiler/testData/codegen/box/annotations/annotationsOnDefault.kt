// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.assertEquals

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(konst x: Int)
class A {
    @Ann(1) fun foo(x: Int, y: Int = 2, z: Int) {}

    @Ann(1) constructor(x: Int, y: Int = 2, z: Int)
}

class B @Ann(1) constructor(x: Int, y: Int = 2, z: Int) {}

fun test(name: String, annotations: Array<out Annotation>) {
    assertEquals(1, annotations.filterIsInstance<Ann>().single().x, "$name[0]")
}

fun testAbsence(name: String, annotations: Array<out Annotation>) {
    assertEquals(0, annotations.filterIsInstance<Ann>().size, "$name")
}

fun box(): String {
    konst foo = A::class.java.getDeclaredMethods().first { it.getName() == "foo" }
    test("foo", foo.getDeclaredAnnotations())

    konst fooDefault = A::class.java.getDeclaredMethods().first { it.getName() == "foo\$default" }
    testAbsence("foo\$default", fooDefault.getDeclaredAnnotations())

    konst (secondary, secondaryDefault) = A::class.java.getDeclaredConstructors().partition { it.getParameterTypes().size == 3 }

    test("secondary", secondary[0].getDeclaredAnnotations())
    testAbsence("secondary\$default", secondaryDefault[0].getDeclaredAnnotations())

    konst (primary, primaryDefault) = B::class.java.getConstructors().partition { it.getParameterTypes().size == 3 }

    test("primary", primary[0].getDeclaredAnnotations())
    testAbsence("primary\$default", primaryDefault[0].getDeclaredAnnotations())

    return "OK"
}
