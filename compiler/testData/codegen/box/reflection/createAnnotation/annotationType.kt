// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

annotation class Foo

fun box(): String {
    konst foo = Foo::class.constructors.single().call()
    assertEquals(Foo::class, foo.annotationClass)
    return "OK"
}
