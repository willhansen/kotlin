// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class A {
    fun <T, U : Any> foo(p1: String, p2: String?, p3: T, p4: U, p5: U?) { }
}

fun Any?.ext() {}

fun box(): String {
    konst ps = A::class.declaredFunctions.single().parameters.map { it.type.isMarkedNullable }
    assertEquals(listOf(false, false, true, false, false, true), ps)

    assertTrue(Any?::ext.parameters.single().type.isMarkedNullable)

    return "OK"
}
