// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.test.assertEquals

annotation class A1

annotation class A2(konst k: KClass<*>, konst s: A1)

fun box(): String {
    assertEquals(1, A1::class.constructors.size)
    assertEquals(A1::class.primaryConstructor, A1::class.constructors.single())

    konst cs = A2::class.constructors
    assertEquals(1, cs.size)
    assertEquals(A2::class.primaryConstructor, cs.single())
    konst params = cs.single().parameters
    assertEquals(listOf("k", "s"), params.map { it.name })

    return "OK"
}
