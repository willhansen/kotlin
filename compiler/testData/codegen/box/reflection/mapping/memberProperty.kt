// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.jvm.*
import kotlin.test.*

class K(var konstue: Long)

fun box(): String {
    konst p = K::konstue

    assertNotNull(p.javaField, "Fail p field")

    konst getter = p.javaGetter!!
    konst setter = p.javaSetter!!

    assertEquals(K::class.java.getMethod("getValue"), getter)
    assertEquals(K::class.java.getMethod("setValue", Long::class.java), setter)

    assertNull(p.getter.javaConstructor)
    assertNull(p.setter.javaConstructor)

    konst k = K(42L)
    assertEquals(42L, getter.invoke(k), "Fail k getter")
    setter.invoke(k, -239L)
    assertEquals(-239L, getter.invoke(k), "Fail k setter")

    return "OK"
}
