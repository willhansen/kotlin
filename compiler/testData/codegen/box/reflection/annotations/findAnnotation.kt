// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNull

annotation class Yes(konst konstue: String)
annotation class No(konst konstue: String)

@Yes("OK")
@No("Fail")
class Foo

class Bar

fun box(): String {
    assertNull(Bar::class.findAnnotation<Yes>())
    assertNull(Bar::class.findAnnotation<No>())

    assertEquals("OK", Foo::class.findAnnotations<Yes>().single().konstue)

    return Foo::class.findAnnotation<Yes>()?.konstue ?: "Fail: no annotation"
}
