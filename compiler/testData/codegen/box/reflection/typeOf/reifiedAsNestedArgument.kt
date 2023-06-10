// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6
// WITH_REFLECT

import kotlin.reflect.typeOf
import kotlin.reflect.KType
import kotlin.test.assertEquals

inline fun <reified T> foo() =
    object { konst x = typeOf<T>() }.x

inline fun <reified T> bar(expected: KType) {
    assertEquals(expected, foo<List<T>>())
    assertEquals(expected, object { konst x = typeOf<List<T>>() }.x)
    assertEquals(expected, typeOf<List<T>>())
}

fun box(): String {
    bar<Int>(typeOf<List<Int>>())
    return "OK"
}
