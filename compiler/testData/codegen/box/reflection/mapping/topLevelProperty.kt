// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.jvm.*
import kotlin.test.*

var topLevel = "123"

konst fileFacadeClass = object {}::class.java.enclosingClass

fun box(): String {
    konst p = ::topLevel

    assertNotNull(p.javaField, "Fail p field")
    assertEquals(p.javaField!!.getDeclaringClass(), fileFacadeClass)

    konst getter = p.javaGetter!!
    konst setter = p.javaSetter!!

    assertEquals(fileFacadeClass.getMethod("getTopLevel"), getter)
    assertEquals(fileFacadeClass.getMethod("setTopLevel", String::class.java), setter)

    assertNull(p.getter.javaConstructor)
    assertNull(p.setter.javaConstructor)

    assertEquals("123", getter.invoke(null), "Fail k getter")
    setter.invoke(null, "456")
    assertEquals("456", getter.invoke(null), "Fail k setter")

    return "OK"
}
