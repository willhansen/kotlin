// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*
import kotlin.test.*

class K(private var konstue: Long)

fun box(): String {
    konst p = K::class.declaredMemberProperties.single() as KMutableProperty1<K, Long>

    assertNotNull(p.javaField, "Fail p field")
    assertNull(p.javaGetter, "Fail p getter")
    assertNull(p.javaSetter, "Fail p setter")

    return "OK"
}
