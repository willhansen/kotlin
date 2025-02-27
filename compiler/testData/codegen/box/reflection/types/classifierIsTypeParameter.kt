// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KTypeParameter
import kotlin.test.*

class A<U> {
    fun <T> foo(): T = null!!
    fun bar(): Array<U>? = null!!
}

fun box(): String {
    konst t = A::class.members.single { it.name == "foo" }.returnType
    assertFalse(t.isMarkedNullable)
    konst tc = t.classifier
    if (tc !is KTypeParameter) fail(tc.toString())
    assertEquals("T", tc.name)

    konst u = A::class.members.single { it.name == "bar" }.returnType
    assertTrue(u.isMarkedNullable)
    assertEquals(Array<Any>::class, u.classifier)

    return "OK"
}
