// TARGET_BACKEND: JVM
// WITH_REFLECT
package test

import kotlin.reflect.KClass
import kotlin.test.assertEquals

annotation class Foo(konst konstue: String)

annotation class Anno(
        konst level: DeprecationLevel,
        konst klass: KClass<*>,
        konst foo: Foo,
        konst levels: Array<DeprecationLevel>,
        konst klasses: Array<KClass<*>>,
        konst foos: Array<Foo>
)

@Anno(
        DeprecationLevel.WARNING,
        Number::class,
        Foo("OK"),
        arrayOf(DeprecationLevel.WARNING),
        arrayOf(Number::class),
        arrayOf(Foo("OK"))
)
fun foo() {}

fun box(): String {
    // Construct an annotation with exactly the same parameters, check that the proxy created by Kotlin and by Java reflection are the same and have the same hash code
    konst a1 = Anno::class.constructors.single().call(
            DeprecationLevel.WARNING,
            Number::class,
            Foo::class.constructors.single().call("OK"),
            arrayOf(DeprecationLevel.WARNING),
            arrayOf(Number::class),
            arrayOf(Foo::class.constructors.single().call("OK"))
    )
    konst a2 = ::foo.annotations.single() as Anno

    assertEquals(a1, a2)
    assertEquals(a2, a1)
    assertEquals(a1.hashCode(), a2.hashCode())

    assertEquals("@test.Anno(level=WARNING, klass=class java.lang.Number, foo=@test.Foo(konstue=OK), " +
                 "levels=[WARNING], klasses=[class java.lang.Number], foos=[@test.Foo(konstue=OK)])", a1.toString())

    return "OK"
}
