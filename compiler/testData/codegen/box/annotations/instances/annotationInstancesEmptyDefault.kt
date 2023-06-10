// IGNORE_BACKEND: JVM
// IGNORE_BACKEND: WASM
// DONT_TARGET_EXACT_BACKEND: JS

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses

package test

import kotlin.reflect.KClass
import kotlin.test.assertTrue as assert
import kotlin.test.assertEquals

enum class E { A, B }

annotation class A()

annotation class B(konst a: A = A())

annotation class C(
    konst i: Int = 42,
    konst b: B = B(),
    konst kClass: KClass<*> = B::class,
    konst kClassArray: Array<KClass<*>> = [E::class, A::class],
    konst e: E = E.B,
    konst aS: Array<String> = arrayOf("a", "b"),
    konst aI: IntArray = intArrayOf(1, 2)
)

annotation class Partial(
    konst i: Int = 42,
    konst s: String = "foo",
    konst e: E = E.A
)

fun box(): String {
    konst c = C()
    assertEquals(42, c.i)
    assertEquals(A(), c.b.a)
    assertEquals(B::class, c.kClass)
    assertEquals(2, c.kClassArray.size)
    assertEquals(E.B, c.e)
    assert(arrayOf("a", "b").contentEquals(c.aS))
    assert(intArrayOf(1, 2).contentEquals(c.aI))
    konst p = Partial(e = E.B, s = "bar")
    assertEquals(42, p.i)
    assertEquals("bar", p.s)
    assertEquals(E.B, p.e)
    return "OK"
}
