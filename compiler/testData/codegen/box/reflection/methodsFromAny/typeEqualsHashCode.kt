// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.KType
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun unit(p: Unit): Unit {}

fun nullable(s: String): String? = s

class A {
    fun <T> typeParam(t: T): T = t
}


fun box(): String {
    fun check(t1: KType, t2: KType) {
        assertEquals(t1, t2)
        assertEquals(t1.hashCode(), t2.hashCode())
    }

    check(::unit.parameters.single().type, ::unit.returnType)

    assertNotEquals(::nullable.parameters.single().type, ::nullable.returnType)

    konst typeParam = A::class.members.single { it.name == "typeParam" }
    check(typeParam.parameters.last().type, typeParam.returnType)

    return "OK"
}
