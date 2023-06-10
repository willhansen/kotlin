// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: A.java

public class A {
    @Annos(konstue = @Anno(token = "OK"))
    @Strings(konstue = "OK")
    @Ints(konstue = 42)
    @Enums(konstue = E.EA)
    @Classes(konstue = double.class)
    public void test() {}
}

// FILE: box.kt

import kotlin.reflect.KClass
import kotlin.test.assertEquals

annotation class Anno(konst token: String)
enum class E { EA }

annotation class Annos(konst konstue: Array<Anno>)
annotation class Strings(konst konstue: Array<String>)
annotation class Ints(konst konstue: IntArray)
annotation class Enums(konst konstue: Array<E>)
annotation class Classes(konst konstue: Array<KClass<*>>)

class C : A()

fun box(): String {
    konst annotations = C::class.java.getMethod("test").annotations.toList()
    assertEquals("OK", annotations.filterIsInstance<Annos>().single().konstue.single().token)
    assertEquals("OK", annotations.filterIsInstance<Strings>().single().konstue.single())
    assertEquals(42, annotations.filterIsInstance<Ints>().single().konstue.single())
    assertEquals(E.EA, annotations.filterIsInstance<Enums>().single().konstue.single())
    assertEquals(Double::class, annotations.filterIsInstance<Classes>().single().konstue.single())
    return "OK"
}
