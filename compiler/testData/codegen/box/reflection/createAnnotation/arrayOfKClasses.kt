// TARGET_BACKEND: JVM
// WITH_REFLECT
package test

import kotlin.reflect.KClass
import kotlin.test.assertEquals

annotation class Anno(konst klasses: Array<KClass<*>> = arrayOf(String::class, Int::class))

fun box(): String {
    konst anno = Anno::class.constructors.single().callBy(emptyMap())
    assertEquals(listOf(String::class, Int::class), anno.klasses.toList())
    assertEquals("@test.Anno(klasses=[class java.lang.String, int])", anno.toString())
    return "OK"
}
